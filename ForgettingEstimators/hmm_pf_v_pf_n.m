%% generate hmm
show_samples = 1;
epsilon = .25;
%sticky transition matrix Psi
Psi = [1-epsilon epsilon/3 epsilon/3 epsilon/3; epsilon/3 1-epsilon epsilon/3 epsilon/3; epsilon/3 epsilon/3 1-epsilon epsilon/3; epsilon/3 epsilon/3 epsilon/3 1-epsilon]';
D = size(Psi,1);
N = 100;

%trivial output distribution
% output dim by input dim
delta = .01;
Theta = [1-delta delta/3 delta/3 delta/3; delta/3 1-delta delta/3 delta/3; delta/3 delta/3 1-delta delta/3; delta/3 delta/3 delta/3 1-delta]';
K = size(Theta,1);

[Vec,Ds] = eig(Psi);
true_mean = (1:D)*Vec(:,1)/(sum(Vec(:,1)));
true_var = (((1:D)-true_mean).^2)*Vec(:,1)/(sum(Vec(:,1)));

z = zeros(N,D);
x = zeros(N,K);
current_state = zeros(D,1);
z(1,ceil(rand*D)) =1;
x(1,randsample(1:D,1,true,Theta * (z(1,:)'))) = 1;

for i = 2:N
    z(i,randsample(1:D,1,true,Psi*z(i-1,:)')) = 1;
    x(i,randsample(1:K,1,true, Theta*z(i,:)')) = 1;
end

if(show_samples)
    figure(1)
    subplot(2,1,1)
    imagesc(z')
    subplot(2,1,2)
    imagesc(x')
end

% set dirichlet parameter \alpha
alpha = 1;

%% do inference in a number of ways
numParticles = 100;

testResults = zeros(100,1);

for test = 1:100

% n iid particle PF
totalTransCount = zeros(D,D);

for particle = 1:numParticles
    
    % 1 particle PF
    % to, from
    transCount = zeros(D,D);
    z_est = zeros(size(z));
    z_est(1,:) = z(1,:);
    for i = 2:N
        z_prev = z_est(i-1,:);
        count_from_z_t_minus_1_to_z_t = transCount(:,logical(z_est(i-1,:)));
        z_t_est = (count_from_z_t_minus_1_to_z_t+alpha)/(sum(count_from_z_t_minus_1_to_z_t)+D*alpha);
        p_x_t_given_z_t = Theta(logical(x(i,:)),:);
        p_z_t_prop = p_x_t_given_z_t.*z_t_est';
        p_z_t = p_z_t_prop/sum(p_z_t_prop(:));
        z_est(i,:) = 0;
        z_est(i,randsample(1:D,1,true,p_z_t)) = 1;
        transCount(logical(z_est(i,:)),logical(z_prev)) = transCount(logical(z_est(i,:)),logical(z_prev))+1;
        totalTransCount(logical(z_est(i,:)),logical(z_prev)) = totalTransCount(logical(z_est(i,:)),logical(z_prev))+1;
        
    end
    
%     if(show_samples)
%         figure(1)
%         subplot(2,1,1)
%         imagesc(z')
%         subplot(2,1,2)
%         imagesc(z_est')
%     end
%     drawnow
    
    Psi_est = transCount ./repmat(sum(transCount),K,1);
    mse_1_p_pf_Psi_est(particle) = sum(sum((Psi_est-Psi).^2))/(D*D);
    
end

%figure(2)
%hist(mse_1_p_pf_Psi_est)
Psi_est = totalTransCount ./repmat(sum(totalTransCount),K,1);
mse_n_p_pf_Psi_est = sum(sum((Psi_est-Psi).^2))/(D*D);


%% do forgetting estimation

% run once through the data like usual
transCount = zeros(D,D);
z_est = zeros(size(z));
z_est(1,:) = z(1,:);
for i = 2:N
    z_prev = z_est(i-1,:);
    count_from_z_t_minus_1_to_z_t = transCount(:,logical(z_est(i-1,:)));
    z_t_est = (count_from_z_t_minus_1_to_z_t+alpha)/(sum(count_from_z_t_minus_1_to_z_t)+D*alpha);
    p_x_t_given_z_t = Theta(logical(x(i,:)),:);
    p_z_t_prop = p_x_t_given_z_t.*z_t_est';
    p_z_t = p_z_t_prop/sum(p_z_t_prop(:));
    z_est(i,:) = 0;
    z_est(i,randsample(1:D,1,true,p_z_t)) = 1;
    transCount(logical(z_est(i,:)),logical(z_prev)) = transCount(logical(z_est(i,:)),logical(z_prev))+1;
    totalTransCount(logical(z_est(i,:)),logical(z_prev)) = totalTransCount(logical(z_est(i,:)),logical(z_prev))+1;
    
end

unifD = ones(1,D)/D;

% then start the forgetting stuff
for sweep = 2:numParticles
    for i = 2:N
        z_prev = z_est(i-1,:);
        
        %randomDeletion = randsample(1:D,1,true,transCount(:,logical(z_est(i-1,:))));
        %randomDeletion = find(z_est(i,:)==1);
        %transCount(randomDeletion,logical(z_est(i-1,:))) = transCount(randomDeletion,logical(z_est(i-1,:)))-1;
        
        count_from_z_t_minus_1_to_z_t = transCount(:,logical(z_est(i-1,:)));
        z_t_est = (count_from_z_t_minus_1_to_z_t+alpha)/(sum(count_from_z_t_minus_1_to_z_t)+D*alpha);
        p_x_t_given_z_t = Theta(logical(x(i,:)),:);
        p_z_t_prop = p_x_t_given_z_t.*z_t_est';
        p_z_t = p_z_t_prop/sum(p_z_t_prop(:));
        z_est(i,:) = 0;
        z_est(i,randsample(1:D,1,true,p_z_t)) = 1;
        transCount(logical(z_est(i,:)),logical(z_prev)) = transCount(logical(z_est(i,:)),logical(z_prev))+1;
        totalTransCount(logical(z_est(i,:)),logical(z_prev)) = totalTransCount(logical(z_est(i,:)),logical(z_prev))+1;
        
    end
    
%     figure(1)
%      if(show_samples)
%         figure(1)
%         subplot(2,1,1)
%         imagesc(z')
%         subplot(2,1,2)
%         imagesc(z_est')
%     end
%     drawnow
end

Psi_est = transCount ./repmat(sum(transCount),K,1);
mse_n_sweeps_1_p_pf_Psi_est = sum(sum((Psi_est-Psi).^2))/(D*D);

testResults(test) = mse_n_sweeps_1_p_pf_Psi_est-mse_n_p_pf_Psi_est;
disp(num2str(testResults(1:test)));
end

hist(testResults)