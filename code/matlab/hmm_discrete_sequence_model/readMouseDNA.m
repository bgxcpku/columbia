function dna = readMouseDNA(filename)

if nargin < 1
filename = '../../../data/mouse_dna.txt';
end

 fid = fopen(filename, 'r');
 fgets(fid); % read header line and discard
   dna = fread(fid, inf, 'uint8=>int')';
   dna = dna(dna~=10); % remove new-lines
   % a = 65, c = 67, g = 71, t = 84
   dna(dna==65) = 1;
   dna(dna==67) = 2;
   dna(dna==71) = 3;
   dna(dna==84) = 4;