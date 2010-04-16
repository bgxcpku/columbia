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

    public double[] cumulativeDistributionInterval(int token){
        double[] predDist = super.getPredDist();
        double low = 0.0;
        for(int i = 0; i<token; i++){
            low += predDist[i];
        }
        return new double[]{low, low + predDist[token]};
    }

    public int inverseCDF(double pointOnCDF) {
        double[] predDist = this.getPredDist();

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
