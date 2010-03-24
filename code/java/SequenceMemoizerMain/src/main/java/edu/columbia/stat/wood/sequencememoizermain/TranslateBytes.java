/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizermain;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author nicholasbartlett
 */
public class TranslateBytes {
    int wordLength;
    FileInputStream fileInputStream = null;
    byte[] nextWord;

    public TranslateBytes(String file, int wordLength) throws FileNotFoundException{
        this.wordLength = wordLength;
        this.fileInputStream = new FileInputStream(file);
        nextWord = new byte[wordLength];
    }

    public int read() throws IOException{
        int b = fileInputStream.read(nextWord);
        if(b == wordLength){
            return TranslateBytes.getInt(nextWord);
        } else {
            return - 1;
        }
    }

    public static int getUnsigned(byte b){
        return 128 + b;
    }

    public static int[] getUnsigned(byte[] b){
        int[] retVal = new int[b.length];
        int i = 0;
        while(i<b.length){
            retVal[i] = 128 + b[i++];
        }
        return retVal;
    }

    public static byte getSigned(int b){
        return (byte) (b - 128);
    }
    
    public static byte[] getSigned(int[] b){
        byte[] retVal = new byte[b.length];
        int i = 0;
        while(i<b.length){
            retVal[i] = (byte) (b[i++] - 128);
        }
        return retVal;
    }

    public static int getInt(byte[] b){
        int[] unsignedB = TranslateBytes.getUnsigned(b);
        int retVal = 0;
        for(int i = 0; i< b.length; i++){
            retVal += unsignedB[b.length-1-i]*Math.pow(256,i);
        }
        return retVal;
    }

    public static byte[] getByteArray(int b, int length){
        ArrayList<Integer> retVal = new ArrayList<Integer>(10);
        while(b > 0){
            retVal.add(b%256);
            b /= 256;
        }
        
        int[] retValInt = new int[length];
        for(int i = 0; i< retValInt.length; i++){
            retValInt[retValInt.length-1-i] = retVal.get(i);
        }
        return TranslateBytes.getSigned(retValInt);
    }
}
