function r = Hypergeometric1F1(a,b,z,n)

if nargin < 4
    n=100;
end

r = 1;
a_part_prod=a;
b_part_prod=b;
for i=1:n
    r = r+ a_part_prod/b_part_prod*(z^i)/factorial(i);
    a_part_prod = a_part_prod*(a+i);
    b_part_prod = b_part_prod*(b+i);
end