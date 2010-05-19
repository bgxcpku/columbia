import pdfa
seq = [[0],[1,0,0,0,1,0],[1,0,0,1]]
pta = pdfa.pta(seq)
state1 = pta.next(0,1)
state2 = pta.next(state1,0)
pta.merge(state1,state2)
pta.printtransition()
