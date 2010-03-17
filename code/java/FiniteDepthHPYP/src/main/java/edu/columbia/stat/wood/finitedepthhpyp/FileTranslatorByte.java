/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.finitedepthhpyp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author nicholasbartlett
 */
public class FileTranslatorByte {

    public FileTranslatorByte() {
    }

    public int[][] translateFile(String path, String[] files) throws FileNotFoundException, IOException {
        int[][] returnVal = new int[files.length][] ;

        ArrayList<Integer> translatedStream = null;
        FileInputStream fileInputStream = null;
        int fileIndex = 0 ;
        for (String file : files) {
            translatedStream = new ArrayList<Integer>();
            try {
                fileInputStream = new FileInputStream(path + file);
                int b;
                while ((b = fileInputStream.read()) != -1) {
                    translatedStream.add(new Integer(b));
                }
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }

            returnVal[fileIndex] = new int[translatedStream.size()] ;
            for (int j = 0; j < translatedStream.size(); j++) {
                returnVal[fileIndex][j] = translatedStream.get(j).intValue();
            }
            fileIndex++ ;
        }


        
        return returnVal;
    }
}
