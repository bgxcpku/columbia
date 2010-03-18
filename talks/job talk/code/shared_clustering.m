d1 = gaussian([1 1],eye(2)*.02);
d2 = gaussian([2 2],eye(2)*.03);
d3 = gaussian([3 3],eye(2)*.5);
d4 = gaussian([1 3],eye(2)*.02);

n = [100 50 20 10];

data = [sample(d1,n(1)); sample(d2,n(2)); sample(d3,n(3)); sample(d4,n(4))];
labels = [ones(n(1),1); ones(n(2),1)*2; ones(n(3),1)*3; ones(n(4),1)*4];
scatter(data(:,1), data(:,2), [], labels)

subset = ceil(rand(sum(n),1)*4);

for k=1:4
subplot(2,2,k)
lhs = scatter(data(subset==k,1), data(subset==k,2), [], labels(subset==k))
for i=1:length(lhs)
    set(lhs,'MarkerFaceColor','flat')
end
set(gca,'XTick',[])
set(gca,'YTick',[])
set(gca,'Box','on')
set(gca,'XLim', [ min(data(:,1))-.1 max(data(:,1))+.1]);
set(gca,'YLim', [ min(data(:,2))-.1 max(data(:,2))+.1]);
end