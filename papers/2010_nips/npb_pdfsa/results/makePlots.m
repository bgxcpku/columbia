figure(1); PlotSamplerTrace('aiw.mh','Alice in Wonderland')
figure(2); PlotSamplerTrace('aiw.small.mh','Alice in Wonderland, Small Corpus')
figure(3); PlotSamplerTrace('dna.mh','Mouse DNA')

dna_log_losses = zeros(1,150);
aiw_log_losses = zeros(1,150);
aiw_small_log_losses = zeros(1,80);

for i = 1:80
    load(['dna_baseline_ave_log_loss_' int2str(i)]);
    dna_log_losses(i) = ave_log_loss;
    
    load(['aiw_baseline_ave_log_loss_' int2str(i)]);
    aiw_log_losses(i) = ave_log_loss;
    
    load(['aiw_small_baseline_ave_log_loss_' int2str(i)]); 
    aiw_small_log_losses(i) = ave_log_loss; 
end

for i = 81:150
    load(['dna_baseline_ave_log_loss_' int2str(i)]);
    dna_log_losses(i) = ave_log_loss;
    
    load(['aiw_baseline_ave_log_loss_' int2str(i)]);
    aiw_log_losses(i) = ave_log_loss;
end

figure(4); plot(dna_log_losses); title('Log Loss per Character: DNA')
figure(5); plot(aiw_log_losses); title('Log Loss per Character: Alice in Wonderland')
figure(6); plot(aiw_small_log_losses); title('Log Loss per Character: Alice in Wonderland, Small Corpus')

save hmm_baseline dna_log_losses aiw_log_losses aiw_small_log_losses