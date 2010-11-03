%Main file for variational gaussian mixture modeling
%
%You will need to fill in values for the parameters of all the priors.
%They are m_0, b_0, a_0, W_0, and nu_0.  The parameter names correspond
%to the notation used int the book.  You should also set K, the number of
%mixture components

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
K = ?;

figure(2)
assignments = k_means(K,meas);
r = (ones(N,K) / K) * .1;
for k = 1 : K
    r(:,k) = r(:,k) + .9 * (assignments == k);
end
disp(expected_rand_index(meas,r,group))

m_0 = ? ;
b_0 = ? ;
a_0 = ? ;
W_0 = ? ;
nu_0 = ?;

for i = 1 : 50
    [alpha,m,W,nu,beta] = get_other_parameters(r, meas);
    r = get_r(alpha,m,W,nu,beta,meas);
    disp(expected_rand_index(meas,r,group))
    
    [mx grp] = max(r,[],2);
    plot_d_dimensional_mixture_data(meas,grp)
end

