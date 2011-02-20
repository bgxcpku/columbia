% import the sequence memoizer code
cd 
import edu.columbia.stat.wood.pub.sequencememoizer.*;

file = fopen('/Users/fwood/Data/pride_and_prejudice/pride_and_prejudice.txt');


gettysburg_address = fread(file);


int_gettysburg_address = uint8(gettysburg_address);
vocab_size = max(int_gettysburg_address)+1; %sequence memoizer is 0-based, not 1-based

% initialize sequence memoizer
ismp = IntSequenceMemoizerParameters(vocab_size);
sm = IntSequenceMemoizer(ismp);

% add corpus
sm.continueSequence(int_gettysburg_address);

% sample a bit to learn model parameters
num_samples = 100;
sm.sample(num_samples);

% what would lincoln have said if the address were to have continued
forwardSample = sm.generateSequence(int_gettysburg_address,100);

forwardSampleText = char(forwardSample);
disp(forwardSampleText');
% this probably looks stupid but this is mostly because the gettysburg 
% address is so short; there is very little training data
