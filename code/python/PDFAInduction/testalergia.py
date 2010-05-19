import pdfa
import alergia

seq = [pdfa.gen(pdfa.reber(0)) for x in range(1000)]
results = alergia.run(seq,0.1)
