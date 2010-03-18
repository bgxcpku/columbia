a = 'In three days time your hard drive is going to crash'
b= dec2bin(a,8)
reshape(b',32,prod(size(b))/32)'