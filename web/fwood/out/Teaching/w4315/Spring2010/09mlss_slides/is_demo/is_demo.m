rand('state',0);
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

fixed_noise_std=1;
slope_mu=0;
slope_std=1;
center_x=8;
center_y=8;
shift_std=1;

minx=5;
miny=3;
maxx=11;
maxy=11;

T=10;
w=zeros(T,1);
for i=1:T
    clf;
    hold on;

    % Perhaps I should use a cauchy?
    slope=randn*slope_std+slope_mu;
    q_slope=normal_pdf(slope,slope_mu,slope_std^2);
    shift_mu=center_y-slope*center_x;
    shift=randn*shift_std+shift_mu;
    q_shift=normal_pdf(shift,shift_mu,shift_std^2);

    Q=q_slope*q_shift;
    pred=oranges_subset(:,1)*slope+shift;
    log_P_star=sum(-0.5*(pred-oranges_subset(:,2)).^2/fixed_noise_std^2);
    w(i)=exp(log_P_star)/Q;

    % Using h = mw + c set end points to be on edge of plot box
    x1=minx;
    y1=slope*x1 + shift;
    if (y1<miny)
        y1=miny;
        x1=(y1-shift)/slope;
    end
    if (y1>maxy)
        y1=maxy;
        x1=(y1-shift)/slope;
    end
    x2=maxx;
    y2=slope*x2 + shift;
    if (y2<miny)
        y2=miny;
        x2=(y2-shift)/slope;
    end
    if (y2>maxy)
        y2=maxy;
        x2=(y2-shift)/slope;
    end
    plot([x1 x2],[y1 y2],'c');

    plot(oranges_subset(:,1),oranges_subset(:,2),'r*');
    hold off;
    axis([minx,maxy,miny,maxy],'square');
    %xlabel('\\setfont{cmss10}{10.00} $w$, width/cm')
    %ylabel('\\setfont{cmss10}{10.00} $h$, height/cm')
    %tics('x',4:2:10);
    %tics('y',4:2:10);
    tics('x',[]);
    tics('y',[]);
    dump_image(plotnum);
    plotnum=plotnum+1;
end

% Blank plot for setup slide
clf;
plot(oranges_subset(:,1),oranges_subset(:,2),'r*');
axis([minx,maxy,miny,maxy],'square');
xlabel('\\setfont{cmss10}{10.00} $w$, width/cm')
ylabel('\\setfont{cmss10}{10.00} $h$, height/cm')
tics('x',4:2:10);
tics('y',4:2:10);
dump_image(plotnum);
plotnum=plotnum+1;

fid=fopen('includeme.tex','w');
for i=1:T
    fprintf(fid,'\\begin{minipage}{0.19\\linewidth}');
    fprintf(fid,'\\includegraphics[width=\\linewidth]{figures/is_demo%02d}\n',i);
    fprintf(fid,'\\small \\centerline{$w=$\\texttt{%1.3g}}\\\\\n',w(i));
    %fprintf(fid,'$w^{(s)}/\\sum_{s^\\prime} w^{(s^\\prime)}=%1.3g$\\\\\n',w(i)/sum(w));
    fprintf(fid,'\\end{minipage}\n');
    if i==5
        fprintf(fid,'\n\\vfill\n\n');
    end
end
fclose(fid);

