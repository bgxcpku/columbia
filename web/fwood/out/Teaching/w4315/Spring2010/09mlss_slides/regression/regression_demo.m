rand('state',3);
% 2 Mandarin
% 7 Morrisons "selected seconds" orange
% 8 Turkey Navel class 1 cal 6 orange

% Include these later
% 6 Lane Late Spanish jumbo orange

data=load('data');
manderin_idx=find(data(:,1)==2);
seconds_idx=find(data(:,1)==7);
turkey_idx=find(data(:,1)==8);
huge_idx=find(data(:,1)==6);

% width and height of oranges. First set has smallest ones taken out
oranges_subset=data([huge_idx;seconds_idx;turkey_idx],[3 4]);
oranges=[oranges_subset;data([manderin_idx],[3 4])];

plotnum=1;
hold on;
plot([5 11], [7 5],'c');
plot([5 11], [5.9 10.5],'b');
plot([5 10.5], [4.7 11],'m');
plot(oranges_subset(:,1),oranges_subset(:,2),'r*');
hold off;

axis([5,11,3,11],'square');
%title('\\setfont{cmss17}{17.00} Oranges data set')
%xlabel('\\setfont{cmss12}{12.00} width/cm')
%ylabel('\\setfont{cmss12}{12.00} height/cm')
xlabel('\\setfont{cmss10}{10.00} $w$, width/cm')
ylabel('\\setfont{cmss10}{10.00} $h$, height/cm')
tics('x',4:2:10);
tics('y',4:2:10);

dump_image(plotnum);

plotnum=plotnum+1;
posterior_sample_demo;
plot(oranges(:,1),oranges(:,2),'r*');
hold off;
dump_image(plotnum);

