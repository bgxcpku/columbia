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
public class Deplump {

    public static int inputBufferLength = 10;

    public static void Deplump(String path, String file) throws FileNotFoundException, IOException {
        
        FileInputStream fileInputStream = null;
        DeplumpStream dpStream = null;
        byte[] inputBuffer = new byte[inputBufferLength];

        try{
            fileInputStream = new FileInputStream(path + file);
            dpStream = new DeplumpStream(new FileOutputStream(path + file + ".dplmp2"));
            int b ;
            do {
                b = fileInputStream.read(inputBuffer);
                dpStream.write(inputBuffer,0,b);
            } while (b == inputBufferLength);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (dpStream != null) {
                dpStream.close();
            }
        }
    }
}
