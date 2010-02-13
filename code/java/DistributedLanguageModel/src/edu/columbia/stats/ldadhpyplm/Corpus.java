/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.ldadhpyplm;

import edu.columbia.stats.ldadhpyplm.Dictionary;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author davidpfau
 */
public class Corpus {
    private Dictionary dict;
    private ArrayList<Document> docs;
    
    private int contextLength;
    private int numDomains;
    
    public Corpus(String path, String format, int contextLength, int numDomains) {
        this.contextLength = contextLength;
        this.numDomains = numDomains;
        if(format.equals("AMI")) {
            dict = readAMIDictionary(path + "ami.dict");
            docs = readAMIDocs(path + "ami_corpus");
        } else {
            dict = new Dictionary();
            docs = new ArrayList<Document>();
        }
    }
    
    public Corpus(Dictionary dict, ArrayList<Document> docs, int contextLength, int numDomains) {
        this.dict = dict;
        this.docs = docs;
        this.contextLength = contextLength;
        this.numDomains = numDomains;
    }
    
    private Dictionary readAMIDictionary(String path){
        File f = new File(path);
        FileInputStream fis = null;
        BufferedReader in = null;
        try {
            fis = new FileInputStream(f);
            in = new BufferedReader(new InputStreamReader(fis));
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File "+path+" not found");
            //e.printStackTrace();
            System.exit(-1);
        }
        
        Dictionary ret = new Dictionary();
        
        try {
            String line = in.readLine();
            while(line != null) {
                line = line.trim();
                String[] numAndWord = line.split(" ");
                if(numAndWord.length == 1){
                    ret.addWord(numAndWord[0]); // add a dummy word to keep things in order
                } else {
                    ret.addWord(numAndWord[1]);
                }
                line = in.readLine();
            }
        } catch(java.io.IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return ret;
    }
    
    private ArrayList<Document> readAMIDocs(String path){
        File f = new File(path);
        FileInputStream fis = null;
        BufferedReader in = null;
        try {
            fis = new FileInputStream(f);
            in = new BufferedReader(new InputStreamReader(fis));
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File "+path+" not found");
            //e.printStackTrace();
            System.exit(-1);
        }
        
        try {
            String line = in.readLine();
            String[] tokenStrings = line.split(" ");
            ArrayList<Integer> tokens = new ArrayList<Integer>(tokenStrings.length);
            int eof = dict.lookup("EOL"); //Since AMI is just one big file, each "document" is one line
            int numWordsSoFar = 0;
            LinkedList<Integer> numWordsInDoc = new LinkedList();
            for(String s : tokenStrings) {
                int token = Integer.parseInt(s);
                tokens.add(token);
                if(token == eof) {
                    numWordsInDoc.add(numWordsSoFar);
                    numWordsSoFar = 0;
                } else {
                    numWordsSoFar++;
                }
            }
            System.gc();
            
            ArrayList<Document> ret = new ArrayList<Document>(numWordsInDoc.size());
            ArrayList<Integer> words = new ArrayList<Integer>(numWordsInDoc.removeFirst());
            for(Integer i : tokens) {
                if(i==eof) {
                    words.trimToSize();
                    ret.add(new Document(words,contextLength,numDomains));
                    //System.out.println("Created document with " + Integer.toString(words.size()) + " tokens");
                    words.clear();
                    if(numWordsInDoc.size() != 0) {
                        words.ensureCapacity(numWordsInDoc.removeFirst());
                    }
                } else {
                    words.add(i);
                }
            }
            System.out.println("Created " + Integer.toString(ret.size()) + " documents");
            
            return ret;
        } catch(java.io.IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        return new ArrayList<Document>();
    }
    
    public Dictionary getDictionary() {
        return dict;
    }
    
    public String getType(int i) {
        return dict.lookup(i);
    }
    
    public ArrayList<Document> getDocuments() {
        return docs;
    }
    
    public Document getDocument(int i) {
        return docs.get(i);
    }
}
