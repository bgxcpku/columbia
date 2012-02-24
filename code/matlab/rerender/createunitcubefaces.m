function [faces colors] = createunitcubefaces()

% front face
xf = [0 1; 0 1];
yf = [0 0; 1 1];
zf = [0 0; 0 0];

% back face
xb = [0 1; 0 1];
yb = [0 0; 1 1];
zb = [1 1; 1 1];

% left face
xl = [0 0; 0 0];
yl = [0 0; 1 1];
zl = [0 1; 0 1];

% right face
xr = [1 1; 1 1];
yr = [0 0; 1 1];
zr = [0 1; 0 1];

% top face
xt = [0 1; 0 1];
yt = [1 1; 1 1];
zt = [0 0; 1 1];

% bottom face
xbo = [0 1; 0 1];
ybo = [0 0; 0 0];
zbo = [0 0; 1 1];

faces = [
    xf(:) yf(:) zf(:); 
    xb(:) yb(:) zb(:); 
    xl(:) yl(:) zl(:); 
    xr(:) yr(:) zr(:); 
    xt(:) yt(:) zt(:); 
    xbo(:) ybo(:) zbo(:); 
    ];

faces = [faces ones(size(faces,1),1)]';
colors = [1 0 0; 0 1 0; 0 0 1; 1 0 1; 0 1 1; 0 0 0]';
