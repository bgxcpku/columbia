function params = stochasticGradientDescent(func,params)

gradient = zeros(size(params));

step = .00001;
learning_rate = .0000001;

for s = 1:100

for i = 1:length(gradient)
    param_plus_eps = params;
    param_plus_eps(i) = param_plus_eps(i) + step;
    gradient(i) = (func(param_plus_eps) - func(params))/step;
end
    params = params - learning_rate*gradient;
    disp(['Current objective function value ' num2str(func(params))]);
end