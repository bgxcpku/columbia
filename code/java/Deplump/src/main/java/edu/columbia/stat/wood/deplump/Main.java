/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

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

        String path = "/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/";
        String file = "AliceInWonderland.txt";

        /*double p = 3.0 / (double) Integer.MAX_VALUE;

        double pp = p / (double) 256;

        double cuSum = 0.0;

        for(int i =0 ; i < 256; i++){
            cuSum += pp;
        }

        System.out.println(p);
        System.out.println(pp);
        System.out.println(cuSum);

*/
        
        Deplump.Deplump(path + file);
        //Plump.Plump(path + file + ".deplump");
    }
}