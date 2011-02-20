function printmenow()
persistent num;
if isempty(num)
    num=0;
end

print(['sw',num2str(num,'%02d'),'.eps'],'-depsc');
num=num+1;
