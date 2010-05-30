function PlotSamplerTrace(name,plottitle)

numstates = dlmread([name '.numstates']);
numstates = numstates(:,1:10); % I am stupid and put an extra column in
scores = dlmread([name '.scores']);
modelscores = dlmread([name '.modelscores']);
totalscores = scores(:,1:10) + modelscores(:,1:10);

nsamples = 0:10:10*(size(numstates,1)-1);

subplot(2,1,1); plot(nsamples,totalscores); xlabel('Number of samples'); ylabel('Joint Log Likelihood of Model and Traning Data'); title(plottitle)
subplot(2,1,2); stairs(nsamples,numstates); xlabel('Number of samples'); ylabel('Number of States')