#!/bin/bash
cat target_* | awk '{for (i = 1; i <= NF; i++) freq[$i]++} END { for (word in freq) printf "%s\t%d\n", word, freq[word]}' > dictionary.txt
cat dictionary.txt | cut -f 1 > wordsOnly.txt
