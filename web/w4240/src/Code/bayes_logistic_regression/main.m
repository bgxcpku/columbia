[X Y] = get_data;

a = 1;
b = .1;
mu = zeros(8,1);

beta = unifrnd(-.2,.2,8,1);
sigma = 10;

iters = 100000;
beta_samp = zeros(iters,8);
sigma_samp = zeros(iters,1);
ll = zeros(iters,1);
for i = 1:iters
    sigma = sample_sigma(beta, a, b);
    beta = sample_beta(beta,sigma, X,Y,a,b,mu);
    
    ll(i) = logistic_log_likelihood(beta,X,Y) + log_likelihood_parameters(beta,sigma,a,b,mu);
    beta_samp(i,:) = beta;
    sigma_samp(i) = sigma;
end

figure(1)
clf
subplot(3,3,1)
plot(sigma_samp);
for i = 1:8
    subplot(3,3,i + 1);
    plot(beta_samp(:,i))
end

