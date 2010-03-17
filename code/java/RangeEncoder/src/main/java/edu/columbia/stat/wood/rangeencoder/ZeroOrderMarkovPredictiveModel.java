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

    public int[] predDist(double lowPointOnCDF, double highPointOnCDF){
        int[] returnVal = new int[2];

        double cumSum = 0.0;
        topFor:
        for(int i = 0; i<p.length;i++){
            cumSum+=p[i];
            if(cumSum > lowPointOnCDF){
                returnVal[0] = i;
                if(cumSum > highPointOnCDF){
                    returnVal[1] = i;
                    break;
                } else {
                    for(int j = 1; j<p.length - i; j++){
                        cumSum += p[i + j];
                        if(cumSum > highPointOnCDF){
                            returnVal[1] = i+j;
                            break topFor;
                        }
                    }
                }

            }
        }
        return returnVal;
    }

    //assumes token 0-(alphabetSize-1)
    public double[] cumulativeDistributionInterval(int token){
        double[] lu = new double[2];
        for(int i = 0; i< token; i++){
            lu[0]+= p[i];
        }
        lu[1] = lu[0] + p[token];
        return lu;
    }

}