/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.finitedepthhpyp;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
/*
        String path = "/Users/nicholasbartlett/Documents/np_bayes/data/generated_data/";
        String file = "order1Markov.txt";

        double ztz = 0.6;
        double zto = 0.4;
        double otz = 0.1;
        double oto = 0.9;

        int n = 100000;
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path + file));
        int obs = 0;
        double rawRandom;
        for(int i = 0; i< n-1; i++){
           rawRandom = Math.random();

            if(obs == 0){
                bos.write(obs);
                if(rawRandom < ztz){
                    obs = 0;
                } else {
                    obs = 1;
                }
            } else {
                bos.write(obs);
                if(rawRandom < otz){
                    obs = 0;
                } else {
                    obs = 1;
                }
            }
        }

        bos.write(obs);
        bos.close(); */

        String path = "/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/";
        String file = "pride_and_prejudice.txt";

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

        System.out.println("done seating");
        //System.out.println(pm.getLogLik());
        
        System.out.println(logLoss/length);
    }
}
