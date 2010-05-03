import pdfa

f = open('/Users/davidpfau/Documents/Wood Group/aiw','r')
aiw = f.read()
f.close()
alphabet = {'P':0}
seq = []
for i in aiw:
    if i not in alphabet:
        alphabet[i] = max(alphabet.values())+1
    seq.append(alphabet[i])
trigram = pdfa.ngram(seq[0:20000],3,1,1,1)
print 'Created trigram model with',trigram.numstates(),'states'
results = pdfa.mh_sample(seq[0:20000],100,1,1,1,trigram)
