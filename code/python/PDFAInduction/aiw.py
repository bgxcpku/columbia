import pdfa
import pickle
import sys

f = open('/hpc/scratch/stats/users/dbp2112/PDFAInduction/data/aiw_sent','r')
aiw_sent = pickle.load(f)
f.close()

alphabet = {' ':0,'a':1,'b':2,'c':3,'d':4,'e':5,'f':6,'g':7,'h':8,'i':9,'j':10,'k':11,'l':12,'m':13,'n':14,'o':15,'p':16,'q':17,'r':18,'s':19,'t':20,'u':21,'v':22,'w':23,'x':24,'y':25,'z':26}
aiw = [[alphabet[x] for x in y if x in alphabet] for y in aiw_sent]  # turns characters into digits
aiw = [x for x in aiw if len(x) != 0] # strip the empty string, wherever it is (my guess is it's just numeric, which we cut out)

train = aiw[:1200]
test = aiw[1200:]

results = pdfa.mh_sample(train,100,1,1,1,pdfa.ngram(train,3,1,1,1))

from math import log
test_bpw = -pdfa.avgscore(results[0][6:],test,100,[x.count(train) for x in results[0][5:]])/log(2)/sum(len(x) for x in test)

f = open('/hpc/scratch/stats/users/dbp2112/PDFAInduction/results/aiw.mh.100.' + sys.argv[1],'w')
pickle.dump(results,f)
pickle.dump(train,f)
pickle.dump(test,f)
pickle.dump(test_bpw,f)
f.close()

ctr = 100
while True:
    results = pdfa.mh_sample(train,100,1,1,1,results[0][-1])
    test_bpw = -pdfa.avgscore(results[0],test,100,[x.count(train) for x in results[0]])/log(2)/sum(len(x) for x in test)
    ctr += 100
    f = open('/hpc/scratch/stats/users/dbp2112/PDFAInduction/aiw.mh.' + str(ctr) + '.' + sys.argv[1],'w')
    pickle.dump(results,f)
    pickle.dump(train,f)
    pickle.dump(test,f)
    pickle.dump(test_bpw,f)
    f.close()
