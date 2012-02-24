function plotfaces(faces, colors, edge_color)

if nargin < 3
    edge_color = 'none';
end

if mod(size(faces,2),4) ~= 0
    error('Need four vertices per face')
end

if size(faces,2)/4 ~= size(colors,2);
    error('Need one color per face')
end

held = 0;
%clf
if ~ishold
    held = 1;
    hold on
end

face = 1;
for i =1:4:size(faces,2);
    x = reshape(faces(1,i:i+3),2,2);
    y = reshape(faces(2,i:i+3),2,2);
    z = reshape(faces(3,i:i+3),2,2);
    c = colors(:,face);
    
    face = face+1;

    surf(x,y,z,'FaceColor',c,'EdgeColor',edge_color)
end

if held
    hold off
end