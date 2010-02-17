/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author nicholasbartlett
 */
public class FileTranslatorByte {

    public FileTranslatorByte() {
    }

    public int[][] translateFile(String path, String[] files, int maxFileLength) throws FileNotFoundException, IOException {
        int[][] returnVal = new int[files.length][] ;
        if (maxFileLength < 0) maxFileLength = 25000000;

        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        int[] translatedStreamArray = new int[maxFileLength];
        int fileIndex = 0 ;
        for (String file : files) {
            int obs = 0;
            try {
                fileInputStream = new FileInputStream(path + file);
                int b;
                while ((b = fileInputStream.read()) != -1 && obs < maxFileLength) {
                    translatedStreamArray[obs++] = b;
                }
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }

            returnVal[fileIndex] = new int[obs] ;
            System.arraycopy(translatedStreamArray, 0, returnVal[fileIndex], 0, obs);
            
            fileIndex++;
        }

        return returnVal;
    }
}
