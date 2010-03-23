/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.rangeencoder;

import java.io.File;
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

    public static void main(String[] args) throws FileNotFoundException, IOException {

        //String path = "/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/";
        //String file = "pride_and_prejudice.txt";
        //String path = "/Users/fwood/Downloads/";
        //String file = "letterToFrank.txt";

        //File file = new File(args[0]);

        //file.

        Deplump.Deplump(args[0]);
        //Plump.Plump(args[0]);
    }

    public static void Deplump(String filename) throws FileNotFoundException, IOException {
        
        FileInputStream fileInputStream = null;
        DeplumpStream dpStream = null;
        byte[] inputBuffer = new byte[inputBufferLength];

        try{
            //File file = new File(filename);

            fileInputStream = new FileInputStream(filename);

            dpStream = new DeplumpStream(new FileOutputStream(filename + ".deplump"));
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
