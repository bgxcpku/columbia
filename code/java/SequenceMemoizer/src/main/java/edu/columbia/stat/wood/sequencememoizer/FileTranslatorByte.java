/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

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

    public int[][] translateFile(String path, String[] files, int maxFileLength) throws FileNotFoundException, IOException {
        int[][] returnVal = new int[files.length][] ;
        if (maxFileLength < 0) maxFileLength = 25000000;

        ArrayList<Integer> translatedStream = null;
        FileInputStream fileInputStream = null;
        int fileIndex = 0 ;
        for (String file : files) {
            translatedStream = new ArrayList<Integer>();
            try {
                fileInputStream = new FileInputStream(path + file);
                int b;
                int obs = 0;
                while ((b = fileInputStream.read()) != -1 && obs < maxFileLength) {
                    translatedStream.add(new Integer(b));
                    obs++;
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
            fileIndex++;
        }

        return returnVal;
    }
}
