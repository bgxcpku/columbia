% G ~ pyp(d_0, c_0, U)
% G_0 ~ pyp(d_1, c_1, G)
% G_1 ~ pyp(d_1, c_1, G)
% G_00 ~ pyp(d_2, c_2, G_0)
% G_01 ~ pyp(d_2, c_2, G_1)
% G_10 ~ pyp(d_2, c_2, G_0)
% G_11 ~ pyp(d_2, c_2, G_1)

U = .5;

c_0 = 0;
c_1 = 0;
c_2 = 0;

d_0 = .99;
d_1 = .8;
d_2 = .7;

num_samples = 100;

G_samples = zeros(num_samples,1);
G_0_samples = zeros(num_samples,1);
G_1_samples = zeros(num_samples,1);
G_00_samples = zeros(num_samples,1);
G_01_samples = zeros(num_samples,1);
G_11_samples = zeros(num_samples,1);
G_10_samples = zeros(num_samples,1);

G_kldiv_samples = zeros(num_samples,1);
G_0_kldiv_samples = zeros(num_samples,1);
G_1_kldiv_samples = zeros(num_samples,1);
G_00_kldiv_samples = zeros(num_samples,1);
G_01_kldiv_samples = zeros(num_samples,1);
G_11_kldiv_samples = zeros(num_samples,1);
G_10_kldiv_samples = zeros(num_samples,1);

for s = 1:num_samples
    
    % generate from the model
    G = binary_pyp_sample(d_0,c_0,U);
    G_0 = binary_pyp_sample(d_1,c_1,G);
    G_1 = binary_pyp_sample(d_1,c_1,G);
    G_00 = binary_pyp_sample(d_2,c_2,G_0);
    G_10 = binary_pyp_sample(d_2,c_2,G_0);
    G_01 = binary_pyp_sample(d_2,c_2,G_1);
    G_11 = binary_pyp_sample(d_2,c_2,G_1);

    G_samples(s) = G;
    G_0_samples(s) = G_0;
    G_1_samples(s) = G_1;
    G_00_samples(s) = G_00;
    G_01_samples(s) = G_01;
    G_11_samples(s) = G_11;
    G_10_samples(s) = G_10;    
    
    G_kldiv_samples(s) = kldiv(U,G);
    G_0_kldiv_samples(s) = kldiv(G,G_0);
    G_1_kldiv_samples(s) = kldiv(G,G_1);
    G_00_kldiv_samples(s) = kldiv(G_0,G_00);
    G_01_kldiv_samples(s) = kldiv(G_1, G_01);
    G_11_kldiv_samples(s) = kldiv(G_1,G_11);
    G_10_kldiv_samples(s) = kldiv(G_0,G_10);
    
    figure(1)
    subplot(3,4,2)
    hist(G_samples(1:s));
    xlim([0 1]);
    subplot(3,4,6)
    hist(G_0_samples(1:s));
    xlim([0 1])
    subplot(3,4,7)
    hist(G_1_samples(1:s));
    xlim([0 1]);
    subplot(3,4,9)
    hist(G_00_samples(1:s));
    xlim([0 1]);
    subplot(3,4,10)
    hist(G_10_samples(1:s));
    xlim([0 1]);
    subplot(3,4,11)
    hist(G_01_samples(1:s));
    xlim([0 1]);
    subplot(3,4,12)
    hist(G_11_samples(1:s));
    xlim([0 1]);
    
    figure(2)
    subplot(3,4,2)
    hist(G_kldiv_samples(1:s));
    %xlim([0 1]);
    subplot(3,4,6)
    hist(G_0_kldiv_samples(1:s));
    %xlim([0 1])
    subplot(3,4,7)
    hist(G_1_kldiv_samples(1:s));
    %xlim([0 1]);
    subplot(3,4,9)
    hist(G_00_kldiv_samples(1:s));
    %xlim([0 1]);
    subplot(3,4,10)
    hist(G_10_kldiv_samples(1:s));
    %xlim([0 1]);
    subplot(3,4,11)
    hist(G_01_kldiv_samples(1:s));
    %xlim([0 1]);
    subplot(3,4,12)
    hist(G_11_kldiv_samples(1:s));
    %xlim([0 1]);
    drawnow
    
end