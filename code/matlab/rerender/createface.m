function [face ] = createface()

% front face
xf = [0 1; 0 1];
yf = [0 0; 1 1];
zf = [0 0; 0 0];

face = [
    xf(:) yf(:) zf(:); 
    ];

face = [face ones(size(face,1),1)]';
