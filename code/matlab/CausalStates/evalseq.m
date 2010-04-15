function states = evalseq(seq,fsm)

states = ones(size(seq));
for i = 2:length(seq)
    states(i) = fsm(seq(i-1),states(i-1));
end