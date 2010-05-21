# Trains a pdfa on dna data

f = open('/hpc/scratch/stats/users/dbp2112/PDFAInduction/data/mouse_dna.txt','r')
dna = f.readlines()
f.close()
dna = dna[1:] # strip the header line
dna = [x.strip() for x in dna] # strip the endline
dna = ''.join(dna)
print 'Loaded', len(dna), 'mouse DNA base pairs.'
alphabet = {'A':0,'T':1,'G':2,'C':3}
train = [alphabet[x] for x in dna[:150000]]
test = [alphabet[x] for x in dna[150000:]]

import pdfa
from math import log

bigram = pdfa.ngram(train,2,1,1,1)
results = pdfa.mh_sample(train,100,1,1,1,bigram)
test_bpw = -pdfa.avgscore(results[0][5:],test,100,[x.count(train) for x in results[0][5:]],[x.state(train) for x in results[0][5:]])/log(2)/len(test)

print held_out_bits_per_word, 'bits per base pair on held out data'

import pickle
import sys

f = open('/hpc/scratch/stats/users/dbp2112/PDFAInduction/results/dna.mh.100.' + sys.argv[1],'w')
pickle.dump(results,f)
pickle.dump(train,f)
pickle.dump(test,f)
pickle.dump(test_bpw,f)
f.close()

ctr = 100
while True:
    results = pdfa.mh_sample(train,100,1,1,1,results[0][-1])
    test_bpw = -pdfa.avgscore(results[0],test,100,[x.count(train) for x in results[0]],[x.state(train) for x in results[0]])/log(2)/len(test)
    ctr += 100
    f = open('/hpc/scratch/states/users/dbp2112/PDFAInduction/results/dna.mh.' + str(ctr) + '.' + sys.argv[1],'w')
    pickle.dump(results,f)
    pickle.dump(train,f)
    pickle.dump(test,f)
    pickle.dump(test_bpw,f)
    f.close()
