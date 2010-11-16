function beta = sample_beta(beta, sigma, X, Y, a, b, mu)

scaling = 1.5;
proposal_cov{1} = scaling * [1.4096   -0.3011   -0.1182   -0.0253
                            -0.3011    1.2560   -0.7064    0.0044
                            -0.1182   -0.7064    1.3486   -0.0009
                            -0.0253    0.0044   -0.0009    0.0006];
proposal_cov{2} = scaling * [ 0.3047    0.0029   -0.0009   -0.0021
                              0.0029    0.0006   -0.0002   -0.0003
                             -0.0009   -0.0002    0.0010   -0.0007
                             -0.0021   -0.0003   -0.0007    0.0011]; 

ll = logistic_log_likelihood(beta, X, Y) + log_likelihood_parameters(beta, sigma, a, b, mu);

beta_prop = mvnrnd(beta(1:4),proposal_cov{1});
ll_prop = logistic_log_likelihood([beta_prop' ; beta(5:8)], X, Y) + log_likelihood_parameters([beta_prop' ; beta(5:8)], sigma, a, b, mu);

r = exp(ll_prop - ll);
if rand < r
    beta(1:4) = beta_prop;
    ll = ll_prop;
end

beta_prop = mvnrnd(beta(5:8),proposal_cov{2});
ll_prop = logistic_log_likelihood([beta(1:4) ; beta_prop'], X, Y) + log_likelihood_parameters([beta(1:4) ; beta_prop'], sigma, a, b, mu);

r = exp(ll_prop - ll);
if rand < r
    beta(5:8) = beta_prop;
end

end