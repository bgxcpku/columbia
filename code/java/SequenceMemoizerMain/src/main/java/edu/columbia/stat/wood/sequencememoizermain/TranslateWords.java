/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizermain;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */
public class TranslateWords {
    
    public static HashMap<String, Integer> dictionary = new HashMap<String, Integer>(100000);
    public static HashMap<Integer, String> reverseDictionary = new HashMap<Integer, String>(100000);

    private FileInputStream fileInputStream = null;

    public TranslateWords(String file) throws FileNotFoundException{
        this.fileInputStream = new FileInputStream(file);
    }

   public int read() throws IOException{
        StringBuilder word = new StringBuilder("");
        int b = fileInputStream.read();
        while(b > -1){
            word.append((char) b);
            if((char) b == ' ' || (char) b == '.'){
                break;
            }
            b = fileInputStream.read();
        }

        if(b == -1){
            return -1;
        }

        int retVal;
        if(dictionary.containsKey(word.toString())){
            retVal = dictionary.get(word.toString());
        } else {
            retVal = dictionary.size();
            dictionary.put(word.toString(), retVal);
            reverseDictionary.put(retVal, word.toString());
        }
        return retVal;
    }
}
