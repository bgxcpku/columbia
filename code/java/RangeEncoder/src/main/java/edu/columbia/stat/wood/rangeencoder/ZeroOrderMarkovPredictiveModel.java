/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.rangeencoder;

import java.util.Arrays;

/**
 *
 * @author nicholasbartlett
 */
public class ZeroOrderMarkovPredictiveModel implements PredictiveModel{

    private int[] predictiveCounts;
    private int totalCount;
    private double[] p;
    private int alphabetSize;

    public ZeroOrderMarkovPredictiveModel(int alphabetSize){
        this.alphabetSize = alphabetSize;
        predictiveCounts = new int[alphabetSize + 1];
        p = new double[alphabetSize + 1];

        Arrays.fill(predictiveCounts, 1);
        Arrays.fill(p, 1.0/(alphabetSize+1));
        totalCount = this.alphabetSize + 1;
    }

    public double continueSequence(int observation){
        double returnVal = Math.log(p[observation]);
        totalCount++;
        predictiveCounts[observation]++;
        this.fixP();
        return returnVal;
    }
    
    private void  fixP(){
        for(int i = 0; i<p.length; i++){
            p[i] = (double) predictiveCounts[i]/totalCount;
        }
    }

    public int inverseCDF(double pointOnCDF){
        long longOnCDF = (long) (pointOnCDF * totalCount);
        
        long cumSum = 0;
        for(int i = 0; i< p.length; i++){
            cumSum += predictiveCounts[i];
            if(cumSum > longOnCDF){
                return i;
            }
        }

        return 256;
    }

    public double[] cumulativeDistributionInterval(int token){
        double low = 0.0;
        for(int i = 0; i< token; i++){
            low += p[i];
        }

        return new double[]{low, low + p[token]};
    }

}