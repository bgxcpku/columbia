function [choice loc_pot_minus_1, loc_pot_plus_1] = local_potential(clean_img, noisy_img, row, column, h, beta, eta)

loc_energy_minus_1 = h * (-1) - eta * (-1) * noisy_img(row,column);
loc_energy_plus_1 = h - eta * noisy_img(row, column);

if row > 1
    loc_energy_minus_1 = loc_energy_minus_1 - beta * (-1) * clean_img(row-1,column);
    loc_energy_plus_1 = loc_energy_plus_1 - beta * clean_img(row-1,column);
end

if row < size(clean_img,1)
    loc_energy_minus_1 = loc_energy_minus_1 - beta * (-1) * clean_img(row+1,column);
    loc_energy_plus_1 = loc_energy_plus_1 - beta * clean_img(row+1,column);
end

if column > 1
    loc_energy_minus_1 = loc_energy_minus_1 - beta * (-1) * clean_img(row,column-1);
    loc_energy_plus_1 = loc_energy_plus_1 - beta * clean_img(row,column-1);
end

if column < size(clean_img,2)
    loc_energy_minus_1 = loc_energy_minus_1 - beta * (-1) * clean_img(row,column + 1);
    loc_energy_plus_1 = loc_energy_plus_1 - beta * clean_img(row,column + 1);
end

loc_pot_minus_1 = exp(-loc_energy_minus_1);
loc_pot_plus_1 = exp(-loc_energy_plus_1);

if loc_pot_minus_1 > loc_pot_plus_1
    choice = -1;
else 
    choice = 1;
end
    