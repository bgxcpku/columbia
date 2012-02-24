% create view transform

% taken from http://knol.google.com/k/perspective-transformation#

% size of view screen -- camera is regular here (fixed aspect ratio)
h_near = 2; 
w_near = 2;

% where in space we want the screen to be
near = -.1; % front clipping plane
far = -100; % back clipping plane

phi = 57.9*2/180 * pi; %field of view of the camera 140 deg.

%aspect ratio 
ar = w_near/h_near;


% right-handed transform, done correctly (web site is wrong)

 P = [1/(ar*tan(phi/2)) 0 0 0;
     0 1/tan(phi/2) 0 0;
     0 0 far/(far-near) -1;
     0 0  -near*far/(far-near) 0]';
 

% need a translate (from camera position to ideal viewpoint at 0,0,0)
% rotate (same)
% shear (to account for view different)
% and project (to get spacing on screen)

% real camera position in other persons room
T = makehgtform('translate',-[0 1 -.1]); 

% real looking direction (rotation back from) camera position in other
% persons room
R = makehgtform('xrotate',atan2(.1,1.99));

figure(2)
clf
axis vis3d

% This is the bit that draws the entire scene 
% create a big background image at depth -10
face = createface();
texture = loadbackgroundtexture();

% maintain aspect ratio when scaling the texture
scalebackground = makehgtform('scale',[40*size(texture,2)/size(texture,1),40,1]);
translatebackground = makehgtform('translate',[-20*size(texture,2)/size(texture,1) -20 -10]);
face = P*R*T*translatebackground*scalebackground*face;
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
faces = P*R*T*translatecube*faces;
faces = [faces(1:3,:)./repmat(faces(end,:),3,1); ones(1,size(faces,2))];%zeros(1,size(faces,2))];
colors(:) = .75;
hold on
plotfaces(faces, colors,[0 0 0]);
hold off
% 
% create a green "background" cube
[faces colors] = createunitcubefaces();
scalecube = makehgtform('scale',[3 .5 5]);
translatecube = makehgtform('translate',[-1.5 -1.5 -9]);
faces = P*R*T*translatecube*scalecube*faces;
faces = [faces(1:3,:)./repmat(faces(end,:),3,1);  ones(1,size(faces,2))];%zeros(1,size(faces,2))];
colors(1,:) = 0;
colors(2,:) = 1;
colors(3,:) = 0;
hold on
plotfaces(faces, colors,[0 .2 0]);
hold off

set(gca,'XLim',[-1 1])
set(gca,'YLim',[-1 1])
camup([ 0 1 0]) % set camera up vector to y axis
camtarget([0 0 0]) % set look at spot to 0 0 -3
axis fill
axis on

% now you have the the color and depths of the objects on the screen, now
% all that one needs to do is "render" the scene, create a mesh, then
% render the scene again from a different angle.  the plot stuff is just
% debugging