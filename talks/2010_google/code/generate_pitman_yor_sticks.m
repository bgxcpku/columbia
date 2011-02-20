d = [0 .33 .66 .99];
c = [.1 1 10 100];
colours = {'c.','r.','g.','b.'};

samples = 10000;
depth = 1100;

for i = 1:length(d)
    subplot(2,2,i)
    cla
    
    jhnds = [];
    for j = 1:length(c)
        pik = zeros(samples,depth);
        
        pik(:,1) = betarnd(1-d(i), c(j)+1*d(i), samples,1);
        rprod = 1-pik(:,1);
        for k = 2:depth
            beta_k = betarnd(1-d(i), c(j)+k*d(i), samples,1);
            pik(:,k) = (rprod) .* beta_k;
            rprod = rprod .* (1-beta_k);
        end
        
        Epik = sum(pik)./samples;
        
        jhnds(j) = loglog(1:length(Epik),Epik,colours{j});
        if j>1
            hold on
        end
    end
    hold off
    axis tight
    if i==1
    legend(jhnds,[' c = ' num2str(c(1))], [' c = ' num2str(c(2))],[' c = ' num2str(c(3))],[' c = ' num2str(c(4))]);
    end
    title(['d = ' num2str(d(i))])
    if i > 2
        xlabel('Stick rank');
    end
    if mod(i,2)~=0
        ylabel('Expected Stick Length');
    end
end

%title('Pitman Yor Expected Stick Length vs. Rank')