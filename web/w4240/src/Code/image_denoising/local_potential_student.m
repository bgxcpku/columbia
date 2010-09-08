function [loc_pot_minus_1, loc_pot_plus_1] = local_potential_student(clean_img, noisy_img, row, column, h, beta, eta)
%The logic here must compute the local contribution to the potential
%function for the possible pixel values of -1 and 1.