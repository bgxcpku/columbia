
load data.mat
figure(1)
imagesc(img)
figure(2)
imagesc(noisy_img)

h = .2;
beta = 1;
eta = 1;

clean_img = noisy_img;

figure(3)

max_loops = 5;

for l = 1:max_loops
    for row = 1 : size(noisy_img,1)
        for col = 1 : size(noisy_img,2)
            [loc_pot_minus_1, loc_pot_plus_1] = local_potential(clean_img, noisy_img,row,col,h,beta,eta);
            
            if loc_pot_minus_1 >= loc_pot_plus_1
                clean_img(row,col) = -1;
            else
                clean_img(row,col) = 1;
            end
        end
    end
    disp(['loop = ' num2str(l)]);
    imagesc(clean_img)
    drawnow
end

imagesc(clean_img)
drawnow
disp([ 'Restoration: pixels wrong : ' num2str(num_pixels_wrong(img, clean_img)) '/' num2str(prod(size(img)))]);
%%
out_img = clean_img;
out_img(out_img==-1) = 0;
s = '';
s = [s '{'];
for r=1:size(out_img,1)
    s = [s '{'];
    for c=1:size(out_img,2)
        if c ~=size(out_img,2)
        s = [s num2str(out_img(r,c)) ', '];
        else 
            s = [s num2str(out_img(r,c)) ];
        end
    end
    if r~=size(out_img,1)
    s = [s sprintf('},\n')];
    else
        s = [s sprintf('}};')];
    end
end
s