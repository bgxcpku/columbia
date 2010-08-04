/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.io.BufferedInputStream;
import java.io.File;
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

        /*
        OnlineSequenceMemoizer sm;
        sm = new OnlineSequenceMemoizer(new SMParameters(256, 15, 1));
        */

        FiniteAlphabetSequenceMemoizer sm;
        sm = new FiniteAlphabetSequenceMemoizer();
        
        BufferedInputStream bis = null;
        File f, g;

        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/pride_and_prejudice.txt");
        f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/AliceInWonderland.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/nyt/lmdata-nyt.1-10000");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/wikipedia/enwik8");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/calgary_corpus/geo");

        double logLik = 0.0;

        try{
            bis = new BufferedInputStream(new FileInputStream(f));

            int b, ind;
            ind = 0;
            while((b = bis.read()) > -1 ) {
                if(ind++ % 100000 == 0){
                    System.out.println(ind++-1);
                }
                logLik += sm.continueSequence(b);
            }
        } finally {
            if (bis != null){
                bis.close();
            }
        }

        System.out.println(-logLik / Math.log(2) / f.length());
        //System.out.println(OnlineRestaurant.count);
        System.out.println(FiniteAlphabetRestaurant.count);
    }
    
    public static void printVector(int[] vect){
        System.out.print("[" + vect[0]);
        for(int i = 1; i<vect.length; i++){
            System.out.print(", " + vect[i]);
        }
        System.out.println("]");
    }
    
}
