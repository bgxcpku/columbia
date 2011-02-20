% location of Psych review data in David's format
load PsychReviewPreprocessed.mat

% location of output directory

output_dir = '/Users/fwood/Projects/columbia/data/psychreview_mallett/train/';

fileids = unique(ds0);

file_names = map(fileids,'int2str');

for fileidindex = 1:length(fileids)
    fileid = fileids(fileidindex);
    
    wordids = ws0(ds0==fileid);
    
    document = wo{wordids(1)};
    
    for i = 2:length(wordids)
        if(wordids(i) == 0)
            document = [document '  EOL'];
        else
            document = [document ' ' wo{wordids(i)}];
        end
    end
    
    
    
    fid = fopen([output_dir file_names{fileidindex}],'w');
    fprintf(fid,'%s',document);
    fclose(fid);
end

output_dir = '/Users/fwood/Projects/columbia/data/psychreview_mallett/test/';

fileids = unique(ds1);

file_names = map(fileids,'int2str');

for fileidindex = 1:length(fileids)
    fileid = fileids(fileidindex);
    
    wordids = ws1(ds1==fileid);
    
    document = wo{wordids(1)};
    
    for i = 2:length(wordids)
        if(wordids(i) == 0)
            document = [document '  EOL'];
        else
            document = [document ' ' wo{wordids(i)}];
        end
    end
    
    
    
    fid = fopen([output_dir file_names{fileidindex}],'w');
    fprintf(fid,'%s',document);
    fclose(fid);
end