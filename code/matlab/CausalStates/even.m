function seq = even(n)

seq = zeros(n,1);
state = rand > 2/3;
for i = 1:n
    if state == 0
        if rand > 1/2
            seq(i) = 0;
            state = 1;
        else
            seq(i) = 1;
            state = 0;
        end
    else
        seq(i) = 0;
        state = 0;
    end
end