/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads the Brown Dictionary file provided by Professor Frank Wood.
 *
 * @author nicholasbartlett
 */
public class BrownDictionaryReader {

    /**
     * Int to String dictionary for Brown corpus.
     */
    public TIntObjectHashMap<String> dictionary;

    /**
     * String to Integer map, the inverse of dictionary.
     */
    public THashMap<String, Integer> inverseDictionary;

    /**
     * Creates the dictionary and inverseDictionary by reading in the file
     * from hard coded location.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public BrownDictionaryReader() throws FileNotFoundException, IOException{

        dictionary = new TIntObjectHashMap<String>();
        inverseDictionary = new THashMap<String,Integer>();

        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(new File("/Users/nicholasbartlett/Documents/np_bayes/data/brown/brown_corpus.dict")));

            String line;
            String[] lineSplit;
            String word;
            int i;
            while((line = br.readLine()) != null){
                lineSplit = line.split(" ");

                i = Integer.parseInt(lineSplit[0]);

                if(lineSplit.length == 1){
                    dictionary.put(i, " ");
                    inverseDictionary.put(" ", i);
                    continue;
                }

                word = lineSplit[1];

                dictionary.put(i, word);
                inverseDictionary.put(word, i);
            }
        } finally {
            br.close();
        }
    }
}
