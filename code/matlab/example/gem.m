function phi = gem(discount, concentration, precision)

if discount < 0 | discount >= 1
    error ('Discount must be 0 <= d < 1');
end

if concentration < -discount
    error ('Concentration must be > -discount');
end

if nargin < 3
    precision = 10^-4;
end


s = 0.0;
k=1;
phi = [];
remaining_stick = 1;

while s < 1 - 2*precision
    w_k = betarnd(1-discount, concentration + k*discount);
    phi_k = w_k*remaining_stick;
    
    k = k+1;
    
    remaining_stick = (1 - w_k)*remaining_stick;
    phi = [phi phi_k];
    
    s = s + phi_k;
    
    
    if k > 10000
        warning(['Truncating at precision ' num2str(1-s)]);
        phi = phi/sum(phi);
        break;
    end
end