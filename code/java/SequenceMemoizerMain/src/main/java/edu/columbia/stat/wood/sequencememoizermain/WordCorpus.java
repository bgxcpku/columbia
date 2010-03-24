/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizermain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author fwood
 */
public class WordCorpus extends Corpus {

    //static String bc_directory_name = baseDir+"natural_language/brown/";
    
    
    /*public static void main(String[] args) {
        BrownCorpus bc = new BrownCorpus(new Dictionary());
        for(int w : bc.words) {
            if(w==0)
                System.out.println("0");
            else 
                System.out.print(w+" ");
        }
        for(String word : bc.dictionary.type_map)
            System.out.println(bc.dictionary.lookup(word)+" "+word);
        System.out.println("Brown Corpus # unique words "+bc.dictionary.size());
    }*/
    
    /*private void initialize() {
        File brown_corpus_directory = new File(bc_directory_name);
        String[] bc_files = brown_corpus_directory.list(new BrownCorpusFilenameFilter());
//        for (String s : bc_files) {
//            System.out.println(s);
//        }

        //BrownCorpus bc = new BrownCorpus(new Dictionary());
        for (String s : bc_files) {
            addFile(bc_directory_name + s);
        }
        
        
    }*/
    
    
    
    public WordCorpus(Dictionary dictionary) {
        this.dictionary = dictionary;
        //initialize();
    }
    
    /*private void addFile(String fn) {
        words.addAll(readInFile(fn));
    }*/
    

    
    public ArrayList<Integer> readInFile(String fn) {
        File f = new File(fn);
        FileInputStream fis = null;
        BufferedReader in = null;
        try {
            fis = new FileInputStream(f);
            in = new BufferedReader(new InputStreamReader(fis));
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        ArrayList<Integer> tc = new  ArrayList<Integer>();
        
        try {
            String line = in.readLine();
            while (line != null) {
                line = line.trim();
                String[] words_on_line = line.split(" ");
                if (words_on_line.length != 0 && !(words_on_line.length ==1 && words_on_line[0].equals(""))) {

                    for (String word : words_on_line) {
                        //String w = word.split("/")[0];
                        //w = w.toLowerCase();
                        //w = w.trim();
                        int c = dictionary.lookupOrAddIfNotFound(word);
                        tc.add(c);
                        //max_tokens++;
                        //System.out.print(w + " ");
                    }
                    //System.out.println("EOL");
                    int c = dictionary.lookupOrAddIfNotFound("EOL");
                    tc.add(c);
                    //max_tokens++;
                }
                line = in.readLine();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        tc.add(dictionary.lookupOrAddIfNotFound("EOF"));
        return tc;
    }
    
    public Dictionary getDictionary() {
        return dictionary;
    }
    
    
}
