/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizermain;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author fwood
 */
public class Corpus implements Serializable {

    public static String baseDir = "/Users/fwood/Data/";

    //int[] corpus;
    int starting_limit = 0;
    int ending_limit = Integer.MAX_VALUE;

 
//    
    public void setLimits(int sl, int el) {
        if(el>words.size())
            el=words.size();
        this.ending_limit = el;
        this.starting_limit = sl;
        assert sl >=0 && sl < words.size();
        assert el <= words.size() && el >= 0;
    }
    ArrayList<Integer> words = new ArrayList<Integer>();
    Dictionary dictionary = new Dictionary();

    
    public Corpus(ArrayList<Integer> words, Dictionary dictionary ) {
        this.words = words;
        this.dictionary = dictionary;
        this.starting_limit = 0;
        this.ending_limit = words.size();
    }
    
    public Corpus() {
        
    }
    
    public void compact() {
        words = null;
    }
    public Corpus(Corpus c) {
        this.words = c.words;
        this.dictionary = c.dictionary;
        this.starting_limit = c.starting_limit;
        this.ending_limit = c.ending_limit;
        
    }
    
    public int maxSize() {
        if (words != null) {
            return words.size();
        } else {
            return 0;
        }

    }

    
    public int maxTokens() {
        if (words != null) {
            return words.size();
        } else {
            return 0;
        }

    }
    
    public int limitedTokens() {
        if(starting_limit!=0 || ending_limit!=Integer.MAX_VALUE)
            return (ending_limit - starting_limit);
        return maxTokens();
    }
    
    public int[] getTokens() {
        if(starting_limit!=0 || ending_limit!=Integer.MAX_VALUE)
            return getTokens(starting_limit,ending_limit);
        else
            return getTokens(0, words.size());
    }

    public Corpus(Corpus c1, Corpus c2) {
        int[] c1_tokens = c1.getTokens();
        int[] c2_tokens  = c2.getTokens();
        
        ArrayList<String> c1_string_tokens = c1.dictionary.lookup(c1_tokens);
        ArrayList<String> c2_string_tokens = c2.dictionary.lookup(c2_tokens);
        
        this.dictionary.addWords(c1_string_tokens);
        this.dictionary.addWords(c2_string_tokens);
        
        for(String w : c1_string_tokens) {
            words.add(this.dictionary.lookup(w));
        }
        for(String w : c2_string_tokens) {
            words.add(this.dictionary.lookup(w));
        }
    }
    
    public int[] getTokens(int sl, int el) {
        int[] ret_tokens = new int[(el - sl)];
        int j = 0;
        for (int i = sl; i < el; i++) {
            ret_tokens[j++] = words.get(i);
        }

        return ret_tokens;
    }
    /*public static Corpus deserializeFromFile(String filename) {
    try {
    FileInputStream fis = new FileInputStream(filename);
    ObjectInputStream ois = new ObjectInputStream(fis);
    Object return_object = ois.readObject();
    ois.close();
    if(return_object instanceof Corpus) {
    return (Corpus)return_object;
    } else {
    System.err.println("Object in "+filename+" not of type Corpus.");
    }
    } catch (Exception e) {
    System.err.println("Error reading "+filename);
    e.printStackTrace();
    }
    return null;
    }
    public void serializeToFile(String file_name) {
    OutputStream ost = null;
    try {
    ost = new FileOutputStream(file_name);
    } catch (java.io.FileNotFoundException e) {
    System.err.println("File "+file_name+" not found.");
    e.printStackTrace(System.err);
    }
    try {
    ObjectOutputStream oos = new ObjectOutputStream(ost);
    oos.writeObject(this);
    oos.flush();
    oos.close();
    } catch (java.io.IOException e) {
    System.err.println("Could not write to file "+file_name);
    e.printStackTrace(System.err);
    }
    }*/
}

        