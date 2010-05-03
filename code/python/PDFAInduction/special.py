# A collection of special functions I can't find anywhere else
# David Pfau, 2010

from math import log

def lgamma(x): # log gamma of x, from Numerical Recipes in C section 6.1
    cof = [76.18009172947146,-86.50532032941677,24.01409824083091,-1.231739572450155,0.1208650973866179e-2,-0.5395239384953e-5]
    tmp = x+5.5
    tmp -= (x+0.5)*log(tmp)
    ser = 1.000000000190015
    y = x
    for j in range(len(cof)):
        y += 1
        ser += cof[j]/y
    return -tmp+log(2.5066282746310005*ser/x)
