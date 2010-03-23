/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.rangeencoder;

import edu.columbia.stat.wood.sequencememoizer.*;
import java.util.Arrays;

/**
 *
 * @author nicholasbartlett
 */
public class SMPredictiveModel extends SMTree implements PredictiveModel {

    public SMPredictiveModel(){
        super(257, 10, -1, SeatingStyle.SIMPLE,0);
    }

    public double[] cumulativeDistributionInterval(int token) {
        double[] initialPredDist = new double[alphabetSize];
        Arrays.fill(initialPredDist, 1.0 / alphabetSize);
        double[] predDist = getPredictiveDist(contextFreeRestaurant, 0, seq.getLastElementIndex(), initialPredDist);

        int i = 0;
        while(i < alphabetSize){
            predDist[i] *= 99.0/100;
            predDist[i++] += (1.0/100)*(1.0/alphabetSize);
        }

        double[] intVals = new double[2];
        for (int j = 0; j < token; j++) {
            intVals[0] += predDist[j];
        }
        intVals[1] = intVals[1] + predDist[token];
        return intVals;
    }

    public int[] predDist(double lowPointOnCDF, double highPointOnCDF){
        double[] initialPredDist = new double[alphabetSize];
        Arrays.fill(initialPredDist, 1.0 / alphabetSize);
        double[] predDist = this.getPredictiveDist(contextFreeRestaurant, 0, seq.getLastElementIndex(), initialPredDist);

        int i = 0;
        while(i < alphabetSize){
            predDist[i] *= 99.0/100;
            predDist[i++] += (1.0/100)*(1.0/alphabetSize);
        }

        int[] returnVal = new int[2];
        double cumSum = 0.0;
        topFor:
        for(int k = 0; k<predDist.length;k++){
            cumSum+=predDist[k];
            if(cumSum > lowPointOnCDF){
                returnVal[0] = k;
                if(cumSum > highPointOnCDF){
                    returnVal[1] = k;
                    break;
                } else {
                    for(int j = 1; j<predDist.length - k; j++){
                        cumSum += predDist[k + j];
                        if(cumSum > highPointOnCDF){
                            returnVal[1] = k+j;
                            break topFor;
                        }
                    }
                }

            }
        }
        return returnVal;
    }
}
