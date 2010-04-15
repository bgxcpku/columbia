% evaluates the likelihood of a sequence given a finite state machine with
% prior probability of an emission given by a draw from Dirichlet(alpha/A),
% where A is the size of the alphabet (starting in state 1)

function score = scoredirichlet(seq,fsm,alpha,beta)

count = zeros(size(fsm)); % the number of times an emission is observed from a state
follow = zeros(size(fsm)); % the number of times a state is observed following an emission
A = size(fsm,1); % the size of the alphabet
k = size(fsm,2); % the number of states'
T = length(seq);

state = 1;
for i = 1:T
    count(seq(i),state) = count(seq(i),state) + 1;
    state = fsm(seq(i),state);
end

for i = 1:A
    for j = 1:k
        follow(i,j) = sum(fsm(i,:) == j);
    end
end

statecount = sum(count); % the number of times each state is visited
emissioncount = sum(follow,2); % the number of times each emission is seen

score = sum(sum( gammaln(count + alpha/A) - gammaln(alpha/A) )) - sum( gammaln(statecount + alpha) - gammaln(alpha) ) ...
        + sum(sum( gammaln(follow + beta/k ) - gammaln(beta/k) )) - sum( gammaln(emissioncount + beta) - gammaln(beta) );