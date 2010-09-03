load data.mat
figure(1)
imagesc(img)
figure(2)
imagesc(noisy_img)

h = .2;
beta = .5;
eta = .75;

clean_img = noisy_img;

figure(3)

max_loops = 10;

for l = 1:max_loops
for r = 2:(size(noisy_img,1) -1)
    for c = 2:(size(noisy_img,2) -1)
        loc_pot_minus_1 = h -beta* (-1 * clean_img(r+1,c) -1 * clean_img(r-1,c) -1 * clean_img(r,c-1) -1 * clean_img(r,c+1)) -eta * (-1) * clean_img(r,c);
        loc_pot_plus_1 = -h -beta* (1 * clean_img(r+1,c) +1 * clean_img(r-1,c) +1 * clean_img(r,c-1) +1 * clean_img(r,c+1)) -eta * (1) * clean_img(r,c);
        
        %if (r == 51 && c == 26)
        %    disp('first nonzero')
        %end
            
        %d = exp(-[loc_pot_plus_1 loc_pot_minus_1]);
        %d = d/sum(d)
        %[loc_pot_plus_1 loc_pot_minus_1]
        
        if loc_pot_minus_1 <= loc_pot_plus_1
            clean_img(r,c) = -1;
        else
            clean_img(r,c) = 1;
        end
    end
end
imagesc(clean_img)
drawnow

end

disp([ 'Restoration: pixels wrong : ' num2str(num_pixels_wrong(img, clean_img)) '/' num2str(prod(size(img)))]);


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