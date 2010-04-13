function seq = sevenstate(n)

seq = zeros(n,1);
follow = [2 3 4 4 2 2 2;
          5 6 7 1 5 5 5];
prob = [3 9 9 3 15 4 12]./16;

%state = mod(floor(rand*1000),7)+1;
state = 1;
for i = 1:n
    if rand < prob(state)
        seq(i) = 0;
        state = follow(1,state);
    else
        seq(i) = 1;
        state = follow(2,state);
    end
end