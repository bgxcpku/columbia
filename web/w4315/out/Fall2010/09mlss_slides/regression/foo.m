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

% width and height of oranges. First set has largest ones taken out
oranges_subset=data([manderin_idx;seconds_idx;turkey_idx],[3 4]);
oranges=[oranges_subset;data([huge_idx],[3 4]);

plotnum=1;
plot(oranges_subset(:,1),oranges_subset(:,2),'r*');

axis([5,11,3,11],'square');
%title('\\setfont{cmss17}{17.00} Oranges data set')
xlabel('\\setfont{cmss12}{12.00} width/cm')
ylabel('\\setfont{cmss12}{12.00} height/cm')
tics('x',4:2:10);
tics('y',4:2:10);

filename=sprintf('mpout/kmeanplot%d.mp',plotnum)
__gnuplot_raw__('set size 0.7\n');
__gnuplot_raw__('set term mp color solid\n')
__gnuplot_raw__(['set output "',filename,'"\n'])
__gnuplot_raw__('replot\n')
__gnuplot_raw__('set term x11\n')
__gnuplot_raw__('set output\n')
%plotnum=plotnum+1;
