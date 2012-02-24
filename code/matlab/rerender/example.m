clf
% create the black wall and window
[faces colors] = createwallandwindow();
hold on
plotfaces(faces, colors);
hold off


% create a big background image at depth -10
face = createface();
texture = loadbackgroundtexture();

% maintain aspect ratio when scaling the texture
scalebackground = makehgtform('scale',[40*size(texture,2)/size(texture,1),40,1]);
translatebackground = makehgtform('translate',[-20*size(texture,2)/size(texture,1) -20 -10]);
face = translatebackground*scalebackground*face;
x = reshape(face(1,1:4),2,2);
y = reshape(face(2,1:4),2,2);
z = reshape(face(3,1:4),2,2);
w = reshape(face(4,1:4),2,2);
x = x./w;
y = y./w;
z = z./w;
hold on
surf(x,y,z,flipdim(texture,1),'FaceColor','texturemap','EdgeColor','none');
hold off

% create a grey "foreground" cube
[faces colors] = createunitcubefaces();
translatecube = makehgtform('translate',[-1.5 -1.5 -2.5]);
faces = translatecube*faces;
colors(:) = .75;
hold on
plotfaces(faces, colors,[0 0 0]);
hold off

% create a green "background" cube
[faces colors] = createunitcubefaces();
scalecube = makehgtform('scale',[3 .5 5]);
translatecube = makehgtform('translate',[-1.5 -1.5 -9]);
faces = translatecube*scalecube*faces;
colors(1,:) = 0;
colors(2,:) = 1;
colors(3,:) = 0;
hold on
plotfaces(faces, colors,[0 .2 0]);
hold off

% if you see cyan you know that you're peering into the impossible
set(gca,'Color',[0 1 1])

% set up matlab rendering environment
axis image
axis vis3d

cam_z_pos = 2;
window_width = 3;

% camera control commands 
% set view angle to something like looking straight through the window
camva(asin((window_width/2)/sqrt(cam_z_pos^2 + (window_width/2)^2))*180/2) 
camup([ 0 1 0]) % set camera up vector to y axis
camtarget([0 0 -3]) % set look at spot to 0 0 -3
% THIS COULD BE DIFFERENT !!! AND CAN CHANGE!!! THIS SHOULD BE INFERRED !!!
camproj('perspective') % set up the perspective transform

% set up orbit around to get a sense of things
angles = linspace(0,2*pi);
x = cos(angles);
y = sin(angles);
x(x==0) = 10e-5; % get around matlab bug
y(y==0) = 10e-5; % get around matlab bug

% start looking straight on
campos([0 10e-5 cam_z_pos]) %
drawnow
pause(1)
%then orbit
for i = 1:length(x)
    campos([x(i) y(i) cam_z_pos])
    drawnow

    pause(.1)
end

% now we need to actually calculate the perspective transform and do some z-buffering
% start looking straight on
pause(2)
%%
% OK this is what the scene looks like from the top of the monitor in the
% other room (something like this)
campos([0 1-10e-5 -.01]) % this is the top of the monitor
camtarget([0 .9 -2]) % fix look at something different than the other persons look at spot
camva(90)
% the angle of view will almost certainly be different
drawnow

% now we need to get the depth and color information from everything in
% this perspective, i.e. we need to get grab depth and color information
% for every "pixel" whatever that might be and then render those pixels in 
% whatever way possible


