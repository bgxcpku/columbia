function r = Hypergeometric1F1Regularized(a,b,z,n)

if nargin < 4
    n=100;
end

if ((b-round(b))==0 && sign(b)==-1) || b==0

    si = abs(b);

    a_part_prod=a;
    for i=1:si
        a_part_prod = a_part_prod*(a+i);
    end

    r = 0;
    for i= (si+1):n
        r = r+ a_part_prod/gamma(b+i)*(z^i)/factorial(i);
        a_part_prod = a_part_prod*(a+i);
    end

else

    if nargin < 4
        r = Hypergeometric1F1(a,b,z)/gamma(b);
    else
        r = Hypergeometric1F1(a,b,z,n)/gamma(b);
    end

end