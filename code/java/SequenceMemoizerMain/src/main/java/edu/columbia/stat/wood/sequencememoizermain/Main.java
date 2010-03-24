/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizermain;

import edu.columbia.stat.wood.sequencememoizer.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author nicholasbartlett
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        Dictionary dict = new Dictionary();
        WordCorpus nytimes = new WordCorpus(dict);

        ArrayList<Integer> words = nytimes.readInFile("/Users/fwood/Data/natural_language/lmdata-nyt.1-10000");

        SeatingStyle seatingStyle = SeatingStyle.RANDOM_DELETION;
        int depth = 1000;
        int seed = 0;
        int maxNumberRestaurants = 6000000;

        String path = "/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/";
        String file = "AliceInWonderland.txt";
        int wordLength = 1;

        SMTree sm = new SMTree((int) Math.pow(256, wordLength), depth, maxNumberRestaurants, seatingStyle, seed);
        double logLoss = 0.0;
        TranslateBytes byteReader = new TranslateBytes(path + file, wordLength);
        int b = byteReader.read();
        int l = 1;
        while (b > -1 && l < 3000000) {
            logLoss -= sm.continueSequence(b) / Math.log(2);
            b = byteReader.read();
            l++;
            if (l % 100000 == 0) {
                System.out.println(l);
            }
        }

        System.out.println(logLoss / (wordLength * l));

        String initSeq = "";
        for (int k = 0; k < 10; k++) {
            int ll = 600;
            int[] someText = sm.generateSequence(TranslateBytes.getUnsigned(initSeq.getBytes()), ll);
            System.out.println(initSeq);
            for (int i = 0; i < ll; i++) {
                byte[] nextText = TranslateBytes.getByteArray(someText[i], wordLength);
                for (int j = 0; j < wordLength; j++) {
                    System.out.print((char) nextText[j]);
                }
            }
            System.out.println();
            System.out.println();
        }

        System.out.println();
        System.out.println();

        initSeq = "said the Cat";
        for (int k = 0; k < 10; k++) {
            int ll = 600;
            int[] someText = sm.generateSequence(TranslateBytes.getUnsigned(initSeq.getBytes()), ll);
            System.out.println(initSeq);
            for (int i = 0; i < ll; i++) {
                byte[] nextText = TranslateBytes.getByteArray(someText[i], wordLength);
                for (int j = 0; j < wordLength; j++) {
                    System.out.print((char) nextText[j]);
                }
            }
            System.out.println();
            System.out.println();
        }

        System.out.println();
        System.out.println();

        initSeq = "the Hatter";
        for (int k = 0; k < 10; k++) {
            int ll = 600;
            int[] someText = sm.generateSequence(TranslateBytes.getUnsigned(initSeq.getBytes()), ll);
            System.out.println(initSeq);
            for (int i = 0; i < ll; i++) {
                byte[] nextText = TranslateBytes.getByteArray(someText[i], wordLength);
                for (int j = 0; j < wordLength; j++) {
                    System.out.print((char) nextText[j]);
                }
            }
            System.out.println();
            System.out.println();
        }

        /*


        System.out.println();
        initSeq = "The New York Yankees";
        System.out.print(initSeq);

        someText = sm.generateSequence(TranslateBytes.getUnsigned(initSeq.getBytes()),ll);
        for(int i = 0; i<ll; i++){
        byte[] nextText = TranslateBytes.getByteArray(someText[i], wordLength);
        for(int j = 0; j<wordLength; j++){
        System.out.print((char) nextText[j]);
        }
        }

        System.out.println();
        initSeq = "The fastest way to make money is";
        System.out.print(initSeq);

        someText = sm.generateSequence(TranslateBytes.getUnsigned(initSeq.getBytes()),ll);
        for(int i = 0; i<ll; i++){
        byte[] nextText = TranslateBytes.getByteArray(someText[i], wordLength);
        for(int j = 0; j<wordLength; j++){
        System.out.print((char) nextText[j]);
        }
        }*/

        /*
        for (int w = 33; w < 257; w++) {
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
        }*/
    }
}
