function runme()

rand('state', 0);
randn('state', 0);

run_sigma = @(sigma) plot(dumb_metropolis(0, @(x) -0.5*x*x, 1e3, sigma), 'LineWidth', 2);

run_sigma(0.01);
save_figure('sigma001');

run_sigma(0.1);
save_figure('sigma01');

run_sigma(1);
save_figure('sigma1');

run_sigma(100);
save_figure('sigma100');

function save_figure(name)
ax = axis;
ax(3:4) = [-4 4];
axis(ax);
set(gcf, 'PaperPosition', [0 0 9 1.2])
filename = [name '.eps'];
print(gcf, '-deps', filename);
system(['epstopdf ' filename]);

