import pdfa
seq = [pdfa.gen(pdfa.reber()) for x in range(1000)]
results = pdfa.mh_sample(seq,100,1,1,1)
