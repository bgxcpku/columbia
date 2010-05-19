import pdfa
from math import log, sqrt

def run(seq,a):
    if type(seq[0]) != list:
        return run([seq],a)
    current = pdfa.pta(seq)
    print current.numstates()
    accept = True
    while accept == True:
        accept = False
        states = current.m.keys()
        states.append(0)
        counts = current.count(seq)
        state_counts = current.state_counts(counts)
        for i in range(1,len(states)): # to do: make merge order such that only the best is accepted each sweep
            for j in range(i):
                if accept == False:
                    if compatible(current,states[i],states[j],counts,state_counts,a):
                        nxt = current.deepcopy()
                        nxt.merge(states[i],states[j])
                        counts = nxt.count(seq)
                        state_counts = nxt.state_counts(counts)
                        print nxt.numstates()
                        accept = True
                        for k in nxt.t:
                            if k not in counts:
                                test = current.deepcopy()
                                import pdb; pdb.set_trace()
                                test.merge(states[i],states[j])
                        current = nxt
    return current

def compatible(pdfa,state1,state2,counts,state_counts,a):
    if state1 not in state_counts or state2 not in state_counts:
        return True
    bound = sqrt(0.5*log(2/a))*(1/sqrt(float(state_counts[state1])) + 1/sqrt(float(state_counts[state2])))
    for i in range(pdfa.S):
        if ((state1,i) in pdfa.t and (state1,i) not in counts) or ((state2,i) in pdfa.t and (state2,i) not in counts):
            print 'shit...'
        if (state1,i) in pdfa.t:
            if (state2,i) in pdfa.t:
                if not compatible(pdfa,pdfa.next(state1,i),pdfa.next(state2,i),counts,state_counts,a): # hmm...this might cause an infinite loop
                    return False
                diff = counts[(state1,i)]/state_counts[state1] - counts[(state2,i)]/state_counts[state2]
                if diff < 0:
                    diff = -diff
            else:
                diff = counts[(state1,i)]/state_counts[state1]
        elif (state2,i) in pdfa.t:
            diff = counts[(state2,i)]/state_counts[state2]
        else:
            diff = 0
        if diff > bound:
            return False
    return True
        
    
