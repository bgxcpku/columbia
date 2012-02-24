function p = l(x,theta)

n = numel(x);

p = -theta*n + log(theta)*sum(x) + sum(log(x));