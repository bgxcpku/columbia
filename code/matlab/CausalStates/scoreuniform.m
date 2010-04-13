% evaluates the likelihood of a sequence given a finite state machine with
% prior probability of an emission given by a draw from Dirichlet(alpha/A),
% where A is the size of the alphabet (starting in state 1)

function [score follow] = scoreuniform(seq,fsm,alpha)

count = zeros(size(fsm)); % the number of times an emission is observed from a state
A = size(fsm,1); % the size of the alphabet
T = length(seq);
follow = zeros(size(fsm));

state = 1;
for i = 1:T
    count(seq(i),state) = count(seq(i),state) + 1;
    state = fsm(seq(i),state);
    follow(seq(i),state) = follow(seq(i),state) + 1;
end

totalcount = sum(count); % the number of times each state is visited

score = sum(sum( gammaln(count + alpha/A) - gammaln(alpha/A) )) - sum( gammaln(totalcount + alpha) - gammaln(alpha) );