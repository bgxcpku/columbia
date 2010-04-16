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
        super(257, 5, -1, SeatingStyle.SIMPLE,0);
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

    public int inverseCDF(double pointOnCDF){
        double[] initialPredDist = new double[alphabetSize];
        Arrays.fill(initialPredDist, 1.0 / alphabetSize);
        double[] predDist = this.getPredictiveDist(contextFreeRestaurant, 0, seq.getLastElementIndex(), initialPredDist);

        int i = 0;
        while(i < alphabetSize){
            predDist[i] *= 99.0/100;
            predDist[i++] += (1.0/100)*(1.0/alphabetSize);
        }

        double cumSum = 0.0;
        for(int k = 0; k<predDist.length; k++){
            cumSum += predDist[k];
            if(cumSum > pointOnCDF){
                return k;
            }
        }

        return 256;
    }
}
