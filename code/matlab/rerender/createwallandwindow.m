function [faces colors] = createwallandwindow()
% create a wall at 0,0,0 and a 3x2 window
facet = createface();
faceb = createface();
facel = createface();
facer = createface();

% make top and bottom parts of wall
s = makehgtform('scale',[1000,1000,1]);
t = makehgtform('translate',[-500 1 0]);
facet = t*s*facet;
t = makehgtform('translate',[-500 -1001 0]);
faceb = t*s*faceb;

% make left and right parts
 s = makehgtform('scale',[498.5,2,1]);
 t = makehgtform('translate',[-500 -1 0]);
 facel = t*s*facel;
 t = makehgtform('translate',[1.5 -1 0]);
 facer = t*s*facer;
 

faces = [facet faceb facel facer];
colors = zeros(3,4);