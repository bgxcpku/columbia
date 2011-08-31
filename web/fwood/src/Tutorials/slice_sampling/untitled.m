% slice sample from a unit Gaussian
T = 10000;

x = zeros(1,T);
u = zeros(1,T);
u_t_minus_1 = .1;
for t = 1:T
    % sample x conditional on u
    ll = norminv(u_t_minus_1,0,2);
    ul = norminv(1-u_t_minus_1,0,2);
    
    x(t) = rand*(ul-ll)+ll;
    u(t) = rand*normpdf(x(t),0,2);
    u_t_minus_1 = u(t);
    
    hist(x(1:t),100);
    drawnow
end