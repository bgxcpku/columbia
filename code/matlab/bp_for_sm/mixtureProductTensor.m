function output = mixtureProductTensor(input, params, mixtureProductTensorParams)

k = mixtureProductTensorParams.k; % number of terms in mixture product tensor approximation
D = mixtureProductTensorParams.D; % dimension of input
d = mixtureProductTensorParams.d; % cardinality of each tensor dimension (think discrete dist)

lambda = params(1:k);
params = params((k+1):end);

p = reshape(params,k,D,d);

output = 0;
for i=1:k
    output = output + lambda(i) * p(i,1,input(1)) *  p(i,2,input(2))*  p(i,3,input(3));
end