/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizermain;

import edu.columbia.stat.wood.sequencememoizer.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        SeatingStyle seatingStyle = SeatingStyle.SIMPLE;
        int depth = 1000;
        int seed = 0;
        int maxNumberRestaurants = -1;

        String path = "/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/";
        String file = "AliceInWonderland.txt";

        for (int w = 10; w < 100; w++) {
            int wordLength = w;

            int nextWord = 0;
            HashMap<IntegerArray, Integer> dict = new HashMap<IntegerArray, Integer>(100000);
            int[] translatedFile = new int[1340232];

            BitReader bitReader = null;
            int fileIndex;
            try {
                bitReader = new BitReader(path + file);

                fileIndex = 0;
                topWhile:
                while (true) {
                    int[] nextWordArray = new int[wordLength];
                    int b;
                    for (int i = 0; i < wordLength; i++) {
                        b = bitReader.read();
                        if (b > -1) {
                            nextWordArray[i] = b;
                        } else {
                            break topWhile;
                        }
                    }

                    IntegerArray nWord = new IntegerArray(nextWordArray);
                    if (dict.containsKey(nWord)) {
                        translatedFile[fileIndex++] = dict.get(nWord);
                    } else {
                        translatedFile[fileIndex++] = nextWord;
                        dict.put(nWord, nextWord++);
                    }
                }
            } finally {
                if (bitReader != null) {
                    bitReader.close();
                }
            }

            //System.out.print("wordSize = " + w + ", ");
            //now have translated file
            SMTree sm = new SMTree((int) Math.pow(2, wordLength), depth, maxNumberRestaurants, seatingStyle, seed);
            double logLoss = 0.0;
            for (int i = 0; i < fileIndex; i++) {
                logLoss -= sm.continueSequence(translatedFile[i]) / Math.log(2);
                //System.out.println(logLoss);
            }

            System.out.println((logLoss / fileIndex) * (8.0 / wordLength));
            //System.out.println(dict.size());
            //System.out.println(fileIndex);
        }
    }
}
