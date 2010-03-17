/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.rangeencoder;

import java.util.Random;

/**
 *
 * @author nicholasbartlett
 */
public class Util {

    Random ran = new Random(0);

    public int[] genData(int n,double[] cdf){
        int[] data = new int[n];
        for(int i = 0; i < n; i++){
            data[i] = genMultinomial(cdf);
        }
        return data;
    }

    public int genMultinomial(double[] cdf){
        double rawRandom = ran.nextDouble();
        double cumSum = 0.0;
        for(int i = 0; i<cdf.length; i++){
            cumSum += cdf[i];
            if(cumSum > rawRandom){
                return i;
            }
        }
        throw new RuntimeException("holy hell");
    }

    public int[] tabulateData(int[] data){
        int maxType = -1;
        for(int i = 0; i<data.length; i++){
            if(data[i] > maxType){
                maxType++;
            }
        }

        int[] counts = new int[maxType+1];

        for(int i = 0; i<data.length; i++){
            counts[data[i]]++;
        }
        return counts;
    }

    public double[] lowerUpper(int obs, double[] cdf){
        double[] lu = new double[2];
        for(int i = 0; i< obs; i++){
            lu[0]+= cdf[i];
        }
        lu[1] = lu[0] + cdf[obs];
        return lu;
    }


    public double getLogLoss(int[] data, double[] cdf){
        int[] counts = this.tabulateData(data);
        double logLoss = 0.0;
        for(int i = 0; i< counts.length; i++){
            logLoss += counts[i]*Math.log(cdf[i]);
        }
        return -logLoss/Math.log(2);
    }

    public void printData(int[] data){
        System.out.print("[" + data[0]);
        for(int i = 1; i < data.length; i++){
            System.out.print(", " + data[i]);
        }
        System.out.println("]");
    }
}
