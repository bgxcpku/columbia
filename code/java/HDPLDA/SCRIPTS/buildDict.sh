#!/bin/bash
cat $1/target_* | awk '{for (i = 1; i <= NF; i++) freq[$i]++} END { for (word in freq) printf "%s\t%d\n", word, freq[word]}' > $1/dictionary.txt
cat $1/dictionary.txt | cut -f 1 > $1/wordsOnly.txt
