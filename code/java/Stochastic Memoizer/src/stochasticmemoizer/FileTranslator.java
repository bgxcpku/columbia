/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stochasticmemoizer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;




/**
 *
 * @author nicholasbartlett
 */
public class FileTranslator {
    public FileTranslator(){}
    
    public Pair<int[],Integer> translateFile(String path, String file) throws FileNotFoundException, IOException{
        HashMap<Character, Integer> translator = new HashMap<Character, Integer>() ;
        ArrayList<Integer> translatedStream = new ArrayList<Integer>() ;

        FileReader inputStream = null ;
        FileWriter outputStream = null ;
        try{
            inputStream = new FileReader(path + file) ;
            outputStream = new FileWriter(path + "Translated_" + file) ;
            int c ;
            int index = 0 ;
            Character nextChar ;
            Integer nextInteger ;
            while((c = inputStream.read()) != -1){
                nextChar = new Character((char) c) ;

                if((nextInteger = translator.get(nextChar)) != null){
                    translatedStream.add(nextInteger) ;
                } else {
                    nextInteger = new Integer(translator.size()) ;
                    translatedStream.add(nextInteger) ;
                    translator.put(nextChar, nextInteger) ;
                }

                outputStream.write(nextInteger.toString());
                outputStream.write(" ");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        int[] returnVal1 = new int[translatedStream.size()] ;
        
        for(int j = 0; j<returnVal1.length; j++){
            returnVal1[j] = translatedStream.get(j).intValue() ;
        }

        Pair returnVal = new Pair(returnVal1,new Integer(translator.size())) ;
       
        return returnVal ;
    }


}
