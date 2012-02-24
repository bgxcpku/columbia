function texture = loadbackgroundtexture()
persistent A;

if isempty(A)
    A = imread('bbroom.jpg');
    A = imresize(A,.2);
    
end

texture = A;

return 

