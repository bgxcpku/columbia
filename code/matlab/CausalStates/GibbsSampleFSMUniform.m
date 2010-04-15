function [fsms scores] = GibbsSampleFSMUniform(seq,A,k,alpha,nsamples)

scores = zeros(nsamples,1);

fsm = repmat([2:k 1],A,1); % initialize as a loop
fsms = repmat({[]},floor(nsamples/10),1);

for t = 1:nsamples
    for a = 1:A
        for i = 1:k
            prob = zeros(k,1);
            for j = 1:k
                fsm(a,i) = j;
                prob(j) = scoreuniform(seq,fsm,alpha);
            end
            cdf = cumsum(exp( prob - max(prob) ))/sum(exp( prob - max(prob) ));
            fsm(a,i) = find(cdf >= rand,1);
            scores(t) = prob(fsm(a,i));
        end
    end
    if mod(t,10) == 0
        fsms{t/10} = fsm;
    end
    disp(['Sample ' int2str(t) ' of ' int2str(nsamples)]);
end