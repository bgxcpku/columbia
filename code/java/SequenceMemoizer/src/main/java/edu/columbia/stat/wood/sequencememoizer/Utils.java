/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.util.Random;

/**
 *
 * @author nicholasbartlett
 */
public class Utils {
    public Random RNG;

    public Utils(long seed){
        RNG = new Random(seed);
    }

    public void printArray(double[] arrayToPrint){
        for(int i = 0; i<arrayToPrint.length; i++){
            if(i == 0){
                System.out.print("[" + arrayToPrint[i]);
            } else {
                System.out.print(", " + arrayToPrint[i]);
            }
        }
        System.out.println("]");
    }
}
