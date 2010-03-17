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

        double[] intVals = new double[2];
        for (int i = 0; i < token; i++) {
            intVals[0] += predDist[i];
        }
        intVals[1] = intVals[1] + predDist[token];
        return intVals;
    }

    public int[] predDist(double lowPointOnCDF, double highPointOnCDF){
        double[] initialPredDist = new double[alphabetSize];
        Arrays.fill(initialPredDist, 1.0 / alphabetSize);
        double[] predDist = this.getPredictiveDist(contextFreeRestaurant, 0, seq.getLastElementIndex(), initialPredDist);

        int[] returnVal = new int[2];
        double cumSum = 0.0;
        topFor:
        for(int i = 0; i<predDist.length;i++){
            cumSum+=predDist[i];
            if(cumSum > lowPointOnCDF){
                returnVal[0] = i;
                if(cumSum > highPointOnCDF){
                    returnVal[1] = i;
                    break;
                } else {
                    for(int j = 1; j<predDist.length - i; j++){
                        cumSum += predDist[i + j];
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
}
