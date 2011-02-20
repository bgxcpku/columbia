function p = binary_pyp_sample(discount,concentration,base_measure_p, precision)
% function p = binary_pyp_sample(discount,concentration,base_measure_p)
%
% by convention p(0) == p or rather base_measure_p = p(0)

if nargin < 4
    precision = 10^-4;
end

stick_breaking_construction=0;

if stick_breaking_construction

if discount == 1
    p =  base_measure_p;
    
else

    % generate stick lengths
    pi = gem(discount,concentration);

    % generate atoms by drawing from base measure
    phi = rand(length(pi),1)>base_measure_p;

    % collapse infinite representation
    p = sum(pi(phi==0));
end

else % crp sample until p converges
    
    table_counts = zeros(2,1); % zero tables, one tables
    customer_counts = zeros(2,1); % zero customers, one customers
    
    n = 0;
    
    while p - p_old > precision
        
        counts = [ table_customer_counts - discount  concentration + discount * sum(table_counts) ];
        
        
    end
end