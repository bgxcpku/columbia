cd ~/Documents/np_bayes/shared/experiments/DCC_2010/

%radix = [1 2 4];
radix = 1;
stream_length = 10.^(3:11);
depth = [16 1024 1048576 1073741824];
size_of_tree = 10.^(3:7);
size_of_tree = [size_of_tree 30000000];

fid  = fopen('wiki.submit','w');
fid2 = fopen('wiki_bigmem.submit','w'); 

fprintf(fid,'universe = java \n');
fprintf(fid,'executable = ../../code/java/DCC_2010/target/dcc2010-1.0-jar-with-dependencies.jar \n');
fprintf(fid,'jar_files = ../../code/java/DCC_2010/target/dcc2010-1.0-jar-with-dependencies.jar \n');
fprintf(fid,'java_vm_args = -Xmx2500m -Xss10m \n \n');

fprintf(fid2,'universe = java \n');
fprintf(fid2,'executable = ../../code/java/DCC_2010/target/dcc2010-1.0-jar-with-dependencies.jar \n');
fprintf(fid2,'jar_files = ../../code/java/DCC_2010/target/dcc2010-1.0-jar-with-dependencies.jar \n');
fprintf(fid2,'Requirements = BigMem == TRUE \n');
fprintf(fid2,'java_vm_args = -Xmx7000m -Xss10m \n \n');

ind = 0;
for r = radix
    for sl = stream_length
        for d = depth
            for sot = size_of_tree
                
                line = ['arguments=edu.columbia.stat.wood.dcc2010.Main /hpc/scratch/stats/users/nsb2130/data/wikipedia/enwik.xml.gz ' ...
                     num2str(sot) ' ' num2str(d) ' ' num2str(sl) ' ' num2str(r) '\n'];
                
                if sot >= 10000000 && sl > 1000000
                    fprintf(fid2,line);
                    
                    fprintf(fid2,['output = w_' num2str(ind) '.out \n']);
                    fprintf(fid2,['error = w_' num2str(ind) '.err \n']);
                    fprintf(fid2,'queue \n \n');
                else
                    fprintf(fid,line);
                    
                    fprintf(fid,['output = w_' num2str(ind) '.out \n']);
                    fprintf(fid,['error = w_' num2str(ind) '.err \n']);
                    fprintf(fid,'queue \n \n');
                end 
                
                ind = ind + 1;
            end
        end
    end 
end

radix = [2 4];
stream_length = 10.^(3:11);
depth = 1048576;
size_of_tree = 1000000;
fprintf(fid,'Requirements =  \n'); 
for r = radix
    for sl = stream_length
        for d = depth
            for sot = size_of_tree
                line = ['arguments=edu.columbia.stat.wood.dcc2010.Main /hpc/scratch/stats/users/nsb2130/data/wikipedia/enwik.xml.gz ' ...
                     num2str(sot) ' ' num2str(d) ' ' num2str(sl) ' ' num2str(r) '\n'];
                fprintf(fid,line);
                fprintf(fid,['output = w_' num2str(ind) '.out \n']);
                fprintf(fid,['error = w_' num2str(ind) '.err \n']);
                fprintf(fid,'queue \n \n');
                
                ind = ind + 1;
            end
        end
    end
end


fclose(fid2);
fclose(fid);