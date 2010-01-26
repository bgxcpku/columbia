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
                int obs = 0;
                int counter = 0;
                while ((b = fileInputStream.read()) != -1 && obs < 5000000) {
                    translatedStream.add(new Integer(b));
                    obs++;
                    if((obs - counter) >= 100000){
                        System.out.println(obs);
                        counter = obs;
                    }
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
