d = [0 .33 .66 .99];
c = 0;

samples = 100;
depth = 1000;

for i = 1:length(d)
    subplot(2,2,i)
    pik = zeros(samples,depth);
    
    pik(:,1) = betarnd(1-d(i), c+1*d(i), samples,1);
    rprod = 1-pik(:,1);
    for k = 2:depth
        beta_k = betarnd(1-d(i), c+k*d(i), samples,1);
        pik(:,k) = (rprod) .* beta_k;
        rprod = rprod .* (1-beta_k);
    end
    
    Epik = sum(pik);
    
    plot(Epik,'.')
end