function distance = tensorDiff(params, fullTensor,mixtureProductTensorParams)

d = mixtureProductTensorParams.d; % cardinality of each tensor dimension (think discrete dist)
D = mixtureProductTensorParams.D;

distance = 0;
numSamples = 500;
for t = 1:numSamples
    index = ceil(rand(1,D)*d);
    distance =distance +(fullTensor(index) - ...
        mixtureProductTensor(index, params, mixtureProductTensorParams))^2;
end
%distance = distance / numSamples;
