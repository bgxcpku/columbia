package edu.columbia.stat.wood.rangeencoder;

import edu.columbia.stat.wood.finitedepthhpyp.HPYTree;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nicholasbartlett
 */
public class FiniteDepthHPYPPredictiveModel extends HPYTree implements PredictiveModel {

    public FiniteDepthHPYPPredictiveModel(){
        super(257,10,0);
    }

    private double[] predDist = new double[257];
    public double[] cumulativeDistributionInterval(int token){
        this.getPredDist(predDist);
        double low = 0.0;
        for(int i = 0; i<token; i++){
            low += predDist[i];
        }
        return new double[]{low, low + predDist[token]};
    }

    public int inverseCDF(double pointOnCDF) {
        this.getPredDist(predDist);

        double cumSum = 0.0;
        for(int i = 0; i<257; i++){
            cumSum += predDist[i];
            if(cumSum > pointOnCDF){
                return i;
            }
        }

        return 256;
    }
}
