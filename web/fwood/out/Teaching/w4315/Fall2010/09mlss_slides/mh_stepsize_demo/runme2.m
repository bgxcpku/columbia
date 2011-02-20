function runme()

rand('state', 0);
randn('state', 0);

sigma = @(sigma) dumb_metropolis(0, @(x) -0.5*x*x, 1e3, sigma);
plotit = @(samples) plot(samples, 'LineWidth', 2);

samples = sigma(0.01);
plotit(samples);
1 - mean(diff(samples) == 0)
% save_figure('sigma001');

samples = sigma(0.1);
plotit(samples);
1 - mean(diff(samples) == 0)

samples = sigma(1);
plotit(samples);
1 - mean(diff(samples) == 0)

samples = sigma(100);
plotit(samples);
1 - mean(diff(samples) == 0)

function save_figure(name)
ax = axis;
ax(3:4) = [-4 4];
axis(ax);
set(gcf, 'PaperPosition', [0 0 9 1.2])
filename = [name '.eps'];
print(gcf, '-deps', filename);
system(['epstopdf ' filename]);

