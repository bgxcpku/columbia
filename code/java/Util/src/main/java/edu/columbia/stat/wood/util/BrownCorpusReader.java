/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Utility class for reading the brown corpus reader.  Requires the location of the
 * brown corpus be hard coded and the file is the translated table provided by Professor
 * Frank Wood.
 * @author nicholasbartlett
 */
public class BrownCorpusReader {

    private int[] file;
    private int index = 0;
    private int length;

    /**
     * Initializes the object and reads in the entire file.  Object works like an
     * iterator.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public BrownCorpusReader() throws FileNotFoundException, IOException{
        BufferedReader br = null;
        String[] split;
        try {
            br = new BufferedReader(new FileReader(new File("/Users/nicholasbartlett/Documents/np_bayes/data/brown/brown_corpus")));
            String corp = br.readLine();
            split = corp.split(" ");
        } finally {
            br.close();
        }

        length = split.length;
        file = new int[length];

        for(int word = 0; word < length; word++){
            file[word] = Integer.parseInt(split[word]);
        }
    }

    /**
     * @return true if there is a next token, else false
     */
    public boolean hasNext(){
        return index < length;
    }

    /**
     * Gets the next token if there is one.
     * @return next token
     */
    public int next(){
        if(index < length){
            return file[index++];
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Gets then number of tokens returned thus far.
     * @return number of tokens returned so far.
     */
    public int returned(){
        return index;
    }
}
