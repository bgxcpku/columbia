FS = factorial(1:10);

n = 4

M = zeros(2^n,n);

for i =0:(2^n-1)
    s = dec2base(i,2);
    y = str2num(s(:))';
    M(i+1,end-length(y)+1:end) = y;
end

S = 0;

for i=1:(2^n)
    num_0s = sum(M(i,:)==0);
    num_1s = sum(M(i,:)==1);
    
    S = S+factorial(num_0s)*factorial(num_1s);
end

FS(n)
S