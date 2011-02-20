%%initialize the tensor
tensor = @(y) mvnpdf(y,[3 3 3],eye(3))*10;
L =    [   0.391239984177695        0.0301304770405857         0.199772584672808
        0.0301304770405857         0.541973567569023       -0.0626466117965437
         0.199772584672808       -0.0626466117965437         0.549731822500783];
    D = 3;
     % D = 20;
    % L = iwishrnd(eye(D),D);
     
tensor = @(y) mvnpdf(y,ones(1,D)*3,L)*10;


%% run the optimization

mixtureProductTensorParams.D = D; % dimensionality of data
mixtureProductTensorParams.k = 5; % number of terms in mixture product tensor approximation
mixtureProductTensorParams.d = 5; % cardinality of each tensor dimension (think discrete dist)

params = zeros(1,k+k*D*d)+.25;

params = rand(1,k+k*D*d)*.5;

A = -eye(k+k*D*d);
b = zeros(k+k*D*d,1);
options = optimset('MaxFunEvals',1e7,'MaxIter',100, 'Display', 'iter');

x = fmincon(@(x) tensorDiff(x,tensor,mixtureProductTensorParams),params,A,b,[],[],[],[],[],options)

%x = stochasticGradientDescent(@(x) tensorDiff(x,tensor,mixtureProductTensorParams),params)

[tensor(3*ones(1,D)) mixtureProductTensor(3*ones(1,D), x, mixtureProductTensorParams)]