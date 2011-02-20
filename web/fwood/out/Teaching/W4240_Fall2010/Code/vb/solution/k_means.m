function assignments = k_means(k,data);

d = size(data,2);

mn = min(data);
mx = max(data);

mu = zeros(d,k);
for i = 1:k
    mu(:,i) = unifrnd(mn,mx);
end

for i = 1:9
    mu = update_cluster_centers(mu,data);
end
[mu assignments] = update_cluster_centers(mu,data);
