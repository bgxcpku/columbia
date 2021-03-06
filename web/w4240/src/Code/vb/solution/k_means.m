function assignments = k_means(k,data)
% This function returns one of k assignment variables for each data point
% in a given data matrix using the k means algorithm.
%
% @param k    : scalar value indicating the number of groups
% @param data : n x d matrix of data
%
% @return assignments : n x 1 matrix of cluster assignments in 1:k

[n d] = size(data);

mu = data(randsample(n,k),:);

for i = 1:9
    mu = update_cluster_centers(mu,data);
end
[~, assignments] = update_cluster_centers(mu,data);
