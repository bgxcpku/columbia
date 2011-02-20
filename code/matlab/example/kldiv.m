function kl = kldiv(p,q)

kl = p * log2(p/q) + (1-p) * log2((1-p)/(1-q))