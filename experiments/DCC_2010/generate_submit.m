cd ~/Documents/np_bayes/shared/experiments/DCC_2010/

radix = [1 2 4];
stream_length = 10.^(3:11);
depth = [16 1024 1048576 1073741824];
size_of_tree = 10.^(3:7);
size_of_tree = [size_of_tree 30000000];

fid = fopen('wikipedia.submit','w');

fprintf(fid,'universe = java \n');
fprintf(fid,'executable = ../../code/java/DCC_2010/target/dcc2010-1.0-jar-with-dependencies.jar \n');
fprintf(fid,'jar_files = ../../code/java/DCC_2010/target/dcc2010-1.0-jar-with-dependencies.jar \n');
%fprintf(fid,'java_vm_args = -Xms4044m -Xmx4044m \n \n \n');

ind = 0;
for r = radix
    for sl = stream_length
        for d = depth
            for sot = size_of_tree
                if r > 1 && sot > 9000000 && sl > 1000000
                    fprintf(fid,'Requirements = BigMem == TRUE \n');
                    fprintf(fid,'java_vm_args = -Xms7680m -Xmx7680m \n \n');
                elseif sot > 10000000 && sl > 1000000
                    fprintf(fid,'Requirements = BigMem == TRUE \n');
                    fprintf(fid,'java_vm_args = -Xms7680m -Xmx7680m \n \n');
                else 
                    fprintf(fid,'Requirements =  \n');
                    fprintf(fid,'java_vm_args = -Xms2500m -Xmx2500m \n \n');
                end
                    
                
                line = ['arguments=edu.columbia.stat.wood.dcc2010.Main ../../../../../data/wikipedia/enwik.xml.gz ' ...
                     num2str(sot) ' ' num2str(d) ' ' num2str(sl) ' ' num2str(r) '\n'];
                fprintf(fid,line);
                fprintf(fid,['output = _' num2str(ind) '.out \n']);
                fprintf(fid,['error = _' num2str(ind) '.err \n']);
                fprintf(fid,'queue \n \n');
                
                ind = ind + 1;
            end
        end
    end 
end
    
fclose(fid);