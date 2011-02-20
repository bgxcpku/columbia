function ret = map(vec, op)

ret = cell(length(vec),1);

for i = 1:length(vec)
    %disp(['ret{' int2str(i) '} =' op '( ' int2str(vec(i)) ' );']);
    eval(['ret{' int2str(i) '} =' op '( ' int2str(vec(i)) ' );']);
end