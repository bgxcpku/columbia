/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.rangeencoder;

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

        //String path = "/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/";
        //String file = "pride_and_prejudice.txt";
        String path = "/Users/nicholasbartlett/Desktop/";
        String file = "letterToFrank.txt";


        Deplump.Deplump(path, file);
        //Plump.Plump(path, file + ".dplmp2");
    }
}