/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.rangeencoder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */
public class Plump {

    public static void Plump(String path, String file) throws FileNotFoundException, IOException {

        PlumpStream pStream = null;
        FileOutputStream fileOutputStream = null;
        
        try {
            pStream = new PlumpStream(new FileInputStream(path + file));
            fileOutputStream = new FileOutputStream(path + file + ".decoded");

            int total = 0;
            int b = pStream.read();
            while(b != -1){
                fileOutputStream.write(b+128);
                b = pStream.read();
                total++;
            }
        } finally {
            if (pStream != null) {
                pStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }
}