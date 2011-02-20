
%%
state_labels = primes(10000);
unk = -1;

delta_true = [  unk state_labels(2) unk ; 
                unk state_labels(2) state_labels(3);
                unk state_labels(3) state_labels(4) ] ;
            
            
x = even(100);            

