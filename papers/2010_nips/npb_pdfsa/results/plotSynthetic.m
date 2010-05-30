means = dlmread('synthetic.mean');
stds = dlmread('synthetic.std');

even_mean = means(1:3:end);
reber_mean = means(2:3:end);
seven_mean = means(3:3:end);

even_std = stds(1:3:end);
reber_std = stds(2:3:end);
seven_std = stds(3:3:end);

x = [100 500 1000 2000 5000 10000 20000];

errorbar(x,even_mean,even_std); hold on
errorbar(x,reber_mean,reber_std,'r')
errorbar(x,seven_mean,seven_std,'g')

legend('Even Process','Reber Grammar','Feldman Grammar')

title('Performance of PDIA on Artificial Grammars')
xlabel('Length of Training Sequence')
ylabel('Inferred Number of States')