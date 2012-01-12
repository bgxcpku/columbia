stochastic_process_type = 'sticky_1st_order_markov';
% choices are iid, fixed_1st_order_markov, sticky_1st_order_markov
show_samples = false;

trials = 10000;
K = 100;
N = 1000;

first_K_mean_estimator = zeros(trials,1);
last_K_mean_estimator = zeros(trials,1);
random_forgetting_mean_estimator = zeros(trials,1);


for trial=1:trials
    
    
    
    switch stochastic_process_type
        case 'iid'
            D = 5;
            
            data = randsample(1:D,N,true);
            true_mean = sum(1:D)/D;
            true_var = sum(((1:D)-true_mean).^2)/D;
            if(show_samples)
                figure(1)
                subplot(3,1,2)
                plot(data)
                subplot(3,1,3)
                hist(data)
                drawnow
            end
        case 'fixed_1st_order_markov'
            D = 5;
            %T = rand(1,D);
            %for j = 2:D
            %    T = [T; rand(1,D)];
            %end
            %T = T./repmat(sum(T,2),1,D);
            T  = [0.0488238069687154 0.2180746819711 0.282983318874933 0.0133199136184829 0.436798278566769;0.0272381383035919 0.0803569651824885 0.292464617492399 0.322443024428958 0.277497254592563;0.166174348693888 0.371577201626393 0.287517831283352 0.135347686012073 0.039382932384294;0.0460644295417081 0.131558131931956 0.124058784811276 0.561785775852784 0.136532877862276;0.331738265561182 0.0861491147818464 0.291671275654074 0.260216332911964 0.0302250110909339]';
            
            [Vec,Ds] = eig(T);
            true_mean = (1:D)*Vec(:,1)/(sum(Vec(:,1)));
            true_var = (((1:D)-true_mean).^2)*Vec(:,1)/(sum(Vec(:,1)));
            
            data = zeros(N,1);
            current_state = zeros(D,1);
            current_state(ceil(rand*D)) =1;
            
            for i = 1:N
                data(i) = randsample(1:D,1,true,T*current_state);
                current_state(:) = 0;
                current_state(data(i)) =1;
            end
            
            
            if(show_samples)
                figure(1)
                subplot(3,1,2)
                plot(data)
                subplot(3,1,3)
                hist(data)
                drawnow
            end
        case 'sticky_1st_order_markov'
            D = 5;
            T = rand(1,D);
            epsilon = .05;
            T = [.25 .25 .25 .25 0; .25 .25 .25 .25 0; .25 .25 .25 .25 0; .25-epsilon/4 .25-epsilon/4 .25-epsilon/4 .25-epsilon/4 epsilon; 0 0 0 epsilon 1-epsilon]';
            
            [Vec,Ds] = eig(T);
            true_mean = (1:D)*Vec(:,1)/(sum(Vec(:,1)));
            true_var = (((1:D)-true_mean).^2)*Vec(:,1)/(sum(Vec(:,1)));
            
            data = zeros(N,1);
            current_state = zeros(D,1);
            current_state(ceil(rand*D)) =1;
            
            for i = 1:N
                data(i) = randsample(1:D,1,true,T*current_state);
                current_state(:) = 0;
                current_state(data(i)) =1;
            end
            
            if(show_samples)
                figure(1)
                subplot(3,1,2)
                plot(data)
                subplot(3,1,3)
                hist(data)
                drawnow
            end
    end
    
    first_K_mean_estimator(trial) = sum(data(1:K))/K;
    t_minus_k_sums = conv(data, ones(K,1)/K,'valid');
    
    last_K_mean_estimator(trial) = sum(t_minus_k_sums)/length(t_minus_k_sums);
    
    random_forgetting_mean_estimator(trial) = 0;
    
    memory = data(1:K);
    for n =(K+1):N
        memory(ceil(rand*K)) = data(n);
        random_forgetting_mean_estimator(trial) = random_forgetting_mean_estimator(trial) + sum(memory);
    end
    random_forgetting_mean_estimator(trial) = random_forgetting_mean_estimator(trial)/(K*(N-K));
    
end

disp(['True mean ' num2str(true_mean) ' True Var ' num2str(true_var)]);

figure(1)
subplot(3,1,1)
hist(first_K_mean_estimator,100)
title(['First K, \sigma^2\{E(X)\} = ' num2str(var(first_K_mean_estimator))]);
subplot(3,1,2)
hist(last_K_mean_estimator,100)
title(['Last K, \sigma^2\{E(X)\} = ' num2str(var(last_K_mean_estimator))]);
subplot(3,1,3)
hist(random_forgetting_mean_estimator,100)
title(['Forgetting, \sigma^2\{E(X)\} = ' num2str(var(random_forgetting_mean_estimator))]);
