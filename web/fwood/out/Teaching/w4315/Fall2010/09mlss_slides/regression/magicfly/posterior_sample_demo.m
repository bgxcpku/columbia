
% Matrix convention:
% 	K_... matrix formed using covariance function without noise
% 	M_... K_... but with any noise and/or jitter added to diagonal
% 	L_... lower diagonal Cholesky decomposition of matrix with noise

% inputs and params
offset=7;
scale=1;
bandwidth=3;
jitter=1e-6;
noise=10*(0.3/scale)^2;
train_inputs=oranges(:,1);
train_outputs=(oranges(:,2)-train_inputs)/scale;
K_train=gauss_Knm(train_inputs,train_inputs,bandwidth);
M_train=plus_diag(K_train,jitter+noise);
L_train=chol(M_train)';

%% Generate from posterior on test_inputs given train_inputs and train_targets

% inputs
test_inputs=(5:.05:11)';
K_train_test=gauss_Knm(train_inputs,test_inputs,bandwidth);

% mean
alpha_weights=L_train'\(L_train\train_outputs);
posterior_mean=K_train_test'*alpha_weights;

% sample prior
K_prior=gauss_Knm(test_inputs,test_inputs,bandwidth);
M_prior=plus_diag(K_prior,jitter);
L_prior=chol(M_prior)';
%prior_samples=scale*(L_prior*randn(size(test_inputs,1),1))+test_inputs;
prior_samples=3*scale*(L_prior*randn(size(test_inputs,1),1))+offset-2; % yes this is fudged
hold off;
plot(test_inputs,prior_samples,'c');
hold on;

% Sample posterior
K_test=gauss_Knm(test_inputs,test_inputs,bandwidth);
M_test=plus_diag(K_test,jitter);
tmp=L_train\K_train_test;
posterior_covariance=M_test-tmp'*tmp;
L_posterior=chol(posterior_covariance)';

posterior_samples=scale*(L_posterior*randn(size(test_inputs,1),1)+posterior_mean)+test_inputs;
plot(test_inputs,posterior_samples,'b');

% Hack a different looking solution
bandwidth=bandwidth;
noise=noise/400;
jitter=jitter*100;
K_train=gauss_Knm(train_inputs,train_inputs,bandwidth);
M_train=plus_diag(K_train,jitter+noise);
L_train=chol(M_train)';
K_train_test=gauss_Knm(train_inputs,test_inputs,bandwidth);
alpha_weights=L_train'\(L_train\train_outputs);
posterior_mean=K_train_test'*alpha_weights;
K_test=gauss_Knm(test_inputs,test_inputs,bandwidth);
M_test=plus_diag(K_test,jitter);
posterior_covariance=M_test-tmp'*tmp;
L_posterior=chol(posterior_covariance)';

posterior_samples=scale*(L_posterior*randn(size(test_inputs,1),1)+posterior_mean)+test_inputs;
plot(test_inputs,posterior_samples,'m');

