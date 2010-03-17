/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.finitedepthhpyp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        String path = "/Users/nicholasbartlett/Documents/np_bayes/data/calgary_corpus/";
        String file = "geo";

        HPYTree pm = new HPYTree(256,15,0);
        double logLoss = 0.0;
        int length = 1;

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(path + file);
            int b = fileInputStream.read();
            while (b>=0) {
                length++;
                logLoss -= pm.continueSequence(b)/Math.log(2);
                b = fileInputStream.read();
            }
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }

        System.out.println(logLoss/length);
    }
}
