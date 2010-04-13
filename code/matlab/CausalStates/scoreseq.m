function [trainscore testscore] = scoreseq(seq,fsms,alpha,trainend)

A = size(fsms{1},1); % alphabet size
R = length(fsms); % number of sampled state machines used to estimate likelihood of sequence

counts = fsms;
totalcounts = fsms;
for j = 1:R
    counts{j} = zeros(size(fsms{j}));
    totalcounts{j} = zeros(1,size(fsms{j},2));
end
trainscore = 0;
testscore = 0;

if (nargin < 4)
    trainend = length(seq);
end

state = ones(R,1);
for i = 1:trainend
    score = 0;
    for j = 1:R
        score = score + ( counts{j}(seq(i),state(j)) + alpha/A ) / ( totalcounts{j}(state(j)) + alpha );
        counts{j}(seq(i),state(j)) = counts{j}(seq(i),state(j)) + 1;
        totalcounts{j}(state(j)) = totalcounts{j}(state(j)) + 1;
        state(j) = fsms{j}(seq(i),state(j));
    end
    trainscore = trainscore + log(score) - log(R);
end

for i = trainend+1:length(seq)
    score = 0;
    for j = 1:R
        score = score + ( counts{j}(seq(i),state(j)) + alpha/A ) / ( totalcounts{j}(state(j)) + alpha );
        counts{j}(seq(i),state(j)) = counts{j}(seq(i),state(j)) + 1;
        totalcounts{j}(state(j)) = totalcounts{j}(state(j)) + 1;
        state(j) = fsms{j}(seq(i),state(j));
    end
    testscore = testscore + log(score) - log(R);
end