%% set up paths
addpath ../hmm/HMM/
addpath ../hmm/KPMstats/
addpath ../hmm/KPMtools
addpath ../hmm/netlab3.3

%% load in data
dna = readMouseDNA();
training_data = dna(1:150000);
test_data = dna(150001:194173);

%% set up random parameters
O = 4;
Q = 12;
prior1 = normalise(rand(Q,1));
transmat1 = mk_stochastic(rand(Q,Q));
obsmat1 = mk_stochastic(rand(Q,O));

%% run EM
[LL, prior2, transmat2, obsmat2] = dhmm_em(training_data, prior1, transmat1, obsmat1, 'max_iter', 50);

%% calculate log likelihood of training data
loglik = dhmm_logprob(test_data, prior2, transmat2, obsmat2);

ave_log_loss = 2^(-loglik*log2(exp(1))/length(test_data))