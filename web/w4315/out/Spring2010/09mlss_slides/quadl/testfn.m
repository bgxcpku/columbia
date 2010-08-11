function f = testfn(x)

persistent count;
if isempty(count)
    count = 0;
end

if exist('x', 'var');
    f = sqrt(1 - x.^2);
    %count = count + 1;
    count = count + numel(x);
else
    f = count;
    count = 0;
end;
