a = 'In three days your hard drive will crash'
b= dec2bin(a,8)
reshape(b',32,prod(size(b))/32)'