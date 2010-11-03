%Main file for variational gaussian mixture modeling
clear
clf

load fisheriris
group(1:50,1) = 1;
group(51:100,1) = 2;
group(101:150,1) = 3;

figure(1)
plot_d_dimensional_mixture_data(meas,group)

global m_0 b_0 a_0 W_0 nu_0

[N D]  = size(meas);
K = 15;

figure(2)
assignments = k_means(K,meas);
r = (ones(N,K) / K) * .1;
for k = 1 : K
    r(:,k) = r(:,k) + .9 * (assignments == k);
end
disp(expected_pairwise_error_rate(meas,r,group))

%r = gamrnd(1,1,N,K);
%r = r ./ repmat(sum(r,2),1,K);

m_0 = zeros(D,1);
b_0 = 1;
a_0 = 1;
nu_0 = 3;
W_0 = 10 * eye(4) / nu_0;

for i = 1 : 50
    [alpha,m,W,nu,beta] = get_other_parameters(r, meas);
    r = get_r(alpha,m,W,nu,beta,meas);
    disp(expected_pairwise_error_rate(meas,r,group))

    figure(3)
    [mx grp] = max(r,[],2);
    plot_d_dimensional_mixture_data(meas,grp)
end
