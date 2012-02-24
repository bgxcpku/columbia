%% set up poisson and population parameters

theta = 5; % rate
n = 10;     % sample size

% generate a sample set
x = poissrnd(theta,n,1);

% compute maximum likelihood estimate of theta
theta_hat = sum(x)/n;

% pick confidence level
alpha = .05;

% find the pivotal quantity 
c = norminv(1-alpha/2);

disp(['--- theta = ' num2str(theta) ' 1-alpha = ' num2str(1-alpha)])

% estimate and rescale (FI est.) confidence interval to problem units 
ci_top_by_information_estimate = theta_hat + c/sqrt(J(x,theta_hat))
ci_bottom_by_information_estimate = theta_hat - c/sqrt(J(x,theta_hat))

% estimate and rescale (assumed sampling dist.) confidence interval to problem units
ci_top_by_sampling_dist = theta_hat + c*sqrt(theta/n)
ci_bottom_by_sampling_dist = theta_hat - c*sqrt(theta/n)

% plot FI est. and assumed sampling dist. of estimator
figure(1)
tp = linspace(theta-theta/n*4,theta+theta/n*4,100);
bh = plot(tp,normpdf(tp,theta,1/(J(x,theta_hat))));
hold on
rh = plot(tp,normpdf(tp,theta,theta/n),'r');
hold off
xlabel('hat \theta')
ylabel('P(hat \theta)')
legend([rh bh],'sampling','FI');
drawnow
    
%% plot FI est. and assumed sampling dist. of estimator for many different
% datasets to see what the variability of the FI est. looks like
tp = linspace(theta-theta/n*4,theta+theta/n*4,100);
y = normpdf(tp,theta,theta/n);
for i = 1:100
    x = poissrnd(theta,n,1);
    theta_hat = sum(x)/n;
    
    bh = plot(tp,normpdf(tp,theta,1/(J(x,theta_hat))));
    hold on
    rh = plot(tp,y,'r');
    hold off
    set(gca,'XLim',[min(tp) max(tp)])
    set(gca,'YLim',[0 1.5*max(y)])
    xlabel('hat \theta')
    ylabel('P(hat \theta)')
    legend([rh bh],'sampling','FI');
    drawnow
    
    pause(.01)
end

%% What happens in n->\infty limit?

% n_max is as far towards infinity as we're going to look
n_max = 75;

% set number of samples in Monte Carlo estimate
num_samples = 1000;

% pick a small number epsilon (e)
e = .5;

fraction_in_which_mle_deviates_from_truth_by_more_than_e = zeros(n_max,1);
fraction_of_time_true_parameter_lives_in_FI_ci = zeros(n_max,1);

approx_KL_div_btwn_true_sampling_dist_and_FI_estimate_of_same = zeros(n_max,1);


% for growing data set size
for n = 1:n_max

    % do a monte carlo estimate of the various expectations
    for s = 1:num_samples
        % generate a sample
        x = poissrnd(theta,n,1);
        % compute maximum likelihood estimate of theta
        theta_hat = sum(x)/n;
        
        % check convergence of ML estimate
        if abs(theta_hat-theta) > e
            fraction_in_which_mle_deviates_from_truth_by_more_than_e(n) = fraction_in_which_mle_deviates_from_truth_by_more_than_e(n)+1;
        end
        
        % check convergence of ML estimator sampling dists
        ci_top_by_information_estimate = theta_hat + c/sqrt(J(x,theta_hat));
        ci_bottom_by_information_estimate = theta_hat - c/sqrt(J(x,theta_hat));
        
        if ci_top_by_information_estimate > theta && ci_bottom_by_information_estimate < theta
            fraction_of_time_true_parameter_lives_in_FI_ci(n)  = fraction_of_time_true_parameter_lives_in_FI_ci(n)+1;
        end
        
        % check KL divergence between theoretical and FI sampling
        sigma_1 = sqrt(theta/n);
        sigma_2 = sqrt(1/(J(x,theta_hat)));
        mu_1 = theta;
        mu_2 = theta;
        approx_KL_div_btwn_true_sampling_dist_and_FI_estimate_of_same(n) = ...
        approx_KL_div_btwn_true_sampling_dist_and_FI_estimate_of_same(n) + log(sigma_2/sigma_1) + (sigma_1^2 + (mu_1-mu_2)^2)/(2*sigma_2^2) - .5;
    end
    fraction_in_which_mle_deviates_from_truth_by_more_than_e(n) = fraction_in_which_mle_deviates_from_truth_by_more_than_e(n)/num_samples;
    approx_KL_div_btwn_true_sampling_dist_and_FI_estimate_of_same(n) = approx_KL_div_btwn_true_sampling_dist_and_FI_estimate_of_same(n)/num_samples;
    fraction_of_time_true_parameter_lives_in_FI_ci(n) = fraction_of_time_true_parameter_lives_in_FI_ci(n)/num_samples;
    
    figure(2)
    subplot(3,1,1)
    plot(1:n,fraction_in_which_mle_deviates_from_truth_by_more_than_e(1:n));
    xlabel('n');
    set(gca,'XLim',[0 n_max]);
    ylabel('\approx P(| \theta_{hat} - \theta| > \epsilon)')
    subplot(3,1,2)
    plot(1:n,fraction_of_time_true_parameter_lives_in_FI_ci(1:n));
    xlabel('n');
    set(gca,'XLim',[0 n_max]);
    ylabel('% time \theta \in FI 1 - \alpha c.i. ')
    subplot(3,1,3)
    plot(1:n,approx_KL_div_btwn_true_sampling_dist_and_FI_estimate_of_same(1:n));
    xlabel('n');
    ylabel('\approx KL btwn FI and sampling dist.')
    set(gca,'XLim',[0 n_max]);

end
    
