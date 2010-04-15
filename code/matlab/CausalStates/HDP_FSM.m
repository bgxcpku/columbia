% A hierarchical nonparametric Bayesian algorithm for learning finite state
% machines from sequences.
%
% David Pfau, 2010

function [fsms scores numstates] = HDP_FSM(seq,A,alpha,alpha_0,beta,nsamples)

scores = zeros(nsamples,1);
numstates = zeros(nsamples,1);

% There is one restaurant for every emission, along with a top-level
% restaurant tying them together.  We initialize with a single "dish".
top = A;  % the top level restaurant, initialized to one customer from every low level restaurant
          % The "dish" at each table is just the index of that table
restaurants = repmat({[1 1]},A,1); % the low level restaurants. 
                                    % The first column is the "dish" served at that table. 
                                    % The second column is the number of customers at that table.

fsm = repmat(1,A,1); % the state which follows any given state/emission pair
tables = repmat(1,A,1); % the table at which each state/emission pair seats the following state
fsms = repmat({[]},floor(nsamples/10),1); % store samples from the markov chain

for t = 1:nsamples
    % since sampling the state of the machine can lead to states being 
    % shuffled or even removed, we need to track which states we've sampled 
    % and which we haven't.  Hence, a for loop won't cut it.
    i = 1;
    while i <= length(top)
        for a = 1:A
            table = tables(a,i); % the index of the table at which the current state/emission pair is seated
            restaurants{a}(table,2) = restaurants{a}(table,2) - 1; % lower the count at that table by one
            if restaurants{a}(table,2) == 0 % if the table was a singleton, remove observation from top level restaurant
                dish = restaurants{a}(table,1);
                top(dish) = top(dish) - 1; % remove customer from top level restaurant
                restaurants{a} = restaurants{a}([1:table-1 table+1:end],:); % remove table from lower level restaurant
                tables(a, tables(a,:) > table) = tables(a, tables(a,:) > table) - 1; % decrement indices of tables assigned to state/emission pairs
                if top(dish) == 0 % if state is a singleton, remove it from the finite state machine
                    top = top([1:dish-1 dish+1:end]);
                    for b = 1:A
                        if ~isempty(restaurants{b})
                            restaurants{b}(restaurants{b}(:,1)>dish,1) = restaurants{b}(restaurants{b}(:,1)>dish,1) - 1;
                        end
                    end
                    tables = tables(:,[1:dish-1 dish+1:end]);
                    fsm = fsm(:,[1:dish-1 dish+1:end]);
                    fsm(fsm>dish) = fsm(fsm>dish) - 1;
                    if i >= dish 
                        i = i-1;
                        if i+1 == dish
                            break; % since this state has been removed from the machine, nothing left to do in this loop
                        end
                    end
                end
            end
            tables(a,i) = 0;
            % sweet mother of god, all of that was just to remove one state/emission pair from one restaurant
            
            k = size(restaurants{a},1);
            m = length(top);
            prob = zeros(k + m + 1,1);
            for j = 1:k % the log probability of seating this customer at an existing table
                fsm(a,i) = restaurants{a}(j,1);
                prob(j) = scoremachine(seq,fsm,beta) + log(restaurants{a}(j,2));
            end
            
            for j = 1:m % the log probability of seating this customer at a new table with an existing dish
                fsm(a,i) = j;
                prob(k + j) = scoremachine(seq,fsm,beta) + log(top(j)) + log(alpha);
            end
            
            top = [top; 1]; % and find the log probability of creating a new state (can probably improve mixing by doing this multiple times, a la Neal Algorithm 8)
            restaurants{a} = [restaurants{a}; length(top) 1];
            tables(a,i) = size(restaurants{a},1);
            fsm(a,i) = length(top);
            [new_tables restaurants top] = addstate(restaurants,top,alpha,alpha_0);
            new_states = size(new_tables,2);
            tables = [tables new_tables];
            new_fsm = zeros(size(new_tables));
            for b = 1:A
                for j = 1:new_states
                    new_fsm(b,j) = restaurants{b}(new_tables(b,j),1);
                end
            end
            fsm = [fsm new_fsm];
            prob(end) = scoremachine(seq,fsm,beta) + log(alpha_0) + log(alpha);
            
            cdf = cumsum(exp( prob - max(prob) ))/sum(exp( prob - max(prob) ));
            idx = find(cdf >= rand,1);
            scores(t) = prob(idx);

            if idx <= k + m % clear the new states from the machine and restaurants
                fsm = fsm(:,1:end-new_states);
                restaurants{a}(tables(a,i),2) = restaurants{a}(tables(a,i),2) - 1;
                for b = 1:A
                    for j = 1:new_states
                        restaurants{b}(tables(b,length(top)-j+1),2) = restaurants{b}(tables(b,length(top)-j+1),2) - 1;
                    end
                    
                    for j = 1:size(restaurants{b},1)
                        if restaurants{b}(j,2) == 0
                            top(restaurants{b}(j,1)) = top(restaurants{b}(j,1)) - 1;
                        end
                    end
                    restaurants{b} = restaurants{b}(restaurants{b}(:,2) ~= 0,:); % since all zeros should be at the end, it's safe to just cut out the empty tables without re-indexing the occupied ones
                end
                top = top(1:end-new_states);
                tables = tables(:,1:end-new_states);
                if idx <= k % seat the state/emission pair at an existing table
                    restaurants{a}(idx,2) = restaurants{a}(idx,2) + 1;
                    tables(a,i) = idx;
                    fsm(a,i) = restaurants{a}(idx,1);
                else % seat the state/emission pair at a new table with existing dish
                    restaurants{a} = [restaurants{a}; idx - k, 1];
                    top(idx - k) = top(idx - k) + 1;
                    tables(a,i) = size(restaurants{a},1);
                    fsm(a,i) = idx - k;
                end
            end
            
            for j = 1:size(restaurants{a},1)
                if sum(tables(a,:) == j) ~= restaurants{a}(j,2)
                    disp('Yer breakin my balls here')
                end
            end
            
            numstates(t) = length(top);
        end
        i = i+1;
    end
    if mod(t,10) == 0
        fsms{t/10} = fsm;
    end
    disp(['Sample ' int2str(t) ' of ' int2str(nsamples)]);
end

function [tables new_restaurants new_top] = addstate(restaurants,top,alpha,alpha_0)

A = length(restaurants);
tables = zeros(A,1);
new_restaurants = restaurants;
new_top = top;

for a = 1:A
    topprob = [new_top; alpha_0];
    topprob = topprob/sum(topprob); % normalize...
    prob = [new_restaurants{a}(:,2); alpha*topprob];
    prob = prob/sum(prob); % and normalize again...
    idx = find(cumsum(prob) >= rand,1);
    k = size(new_restaurants{a},1); % the number of tables in restaurant a
    if idx <= k % seat customer at existing table
        tables(a,1) = idx;
        new_restaurants{a}(idx,2) = new_restaurants{a}(idx,2) + 1; % add customer to that table
    elseif idx < length(prob) % seat customer at new table, and seat new table at existing top-level table
        tables(a,1) = k+1;
        new_restaurants{a} = [new_restaurants{a}; idx - k, 1];
        new_top(idx - k) = new_top(idx - k) + 1;
    else % now the tricky part...add a new table in the top-level restaurant
        tables(a,1) = k+1;
        new_restaurants{a} = [new_restaurants{a}; length(new_top) + 1, 1];
        new_top = [top; 1];
        [new_tables new_restaurants new_top] = addstate(new_restaurants,new_top,alpha,alpha_0); 
        % Now fill in the state/emission pairs for the *next* new state.  
        % Thanks to exchangeability of the CRP, it shouldn't matter what order we do this in.
        tables = [tables new_tables];
    end
end

function pass = checktop(top, restaurants)
% for debugging purposes, checks to make sure that the number of customers
% in the top level restaurant equals the total number of tables in all the
% lower level restaurants

checktop = zeros(size(top));
for b = 1:A
    for j = 1:length(top)
        checktop(j) = checktop(j) + sum(restaurants{b}(:,1) == j);
    end
end
pass = checktop ~= top