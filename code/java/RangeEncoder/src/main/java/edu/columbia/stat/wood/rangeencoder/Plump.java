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

    public static void main(String[] args) throws FileNotFoundException, IOException {

        //String path = "/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/";
        //String file = "pride_and_prejudice.txt";
        //String path = "/Users/fwood/Downloads/";
        //String file = "letterToFrank.txt";

        //File file = new File(args[0]);

        //file.

        Plump.Plump(args[0]);
        //Plump.Plump(args[0]);
    }


    public static void Plump(String file) throws FileNotFoundException, IOException {

        PlumpStream pStream = null;
        FileOutputStream fileOutputStream = null;
        
        try {
            pStream = new PlumpStream(new FileInputStream(file));

            file = file.substring(0, file.lastIndexOf('.'));

            fileOutputStream = new FileOutputStream(file);

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