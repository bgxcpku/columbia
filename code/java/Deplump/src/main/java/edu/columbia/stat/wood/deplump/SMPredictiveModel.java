/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.deplump;

import edu.columbia.stat.wood.sequencememoizer.MutableInteger;
import edu.columbia.stat.wood.sequencememoizer.Pair;
import edu.columbia.stat.wood.sequencememoizer.SMParameters;
import edu.columbia.stat.wood.sequencememoizer.SamplingSequenceMemoizer;

/**
 * Sequence memoizer predictive model for compression.
 * 
 * @author nicholasbartlett
 */
public class SMPredictiveModel extends SamplingSequenceMemoizer implements PredictiveModel {

    private static double MIN_SYMBOL_PROB = 5.01 / (double) (Integer.MAX_VALUE);

    /**
     * Initializes this as a Sequence Memoizer object with an alphabet size of
     * 257, inifinite depth, and a random seed of 0.  Discount parameters and the
     * alpha parameter are set to the default values.
     */
    public SMPredictiveModel(){
        super(new SMParameters(257, -1, 0));
    }

    /**
     * Populates interval with CDF(token - 1) and CDF(token) and incorporates
     * the token into the predictive model.
     *
     * @param token token to encode / incorporate
     * @param interval container for CDF values
     */
    public void cumulativeDistributionInterval(int token, double[] interval) {
        double[] cdf;
        double cuSum, eofAdjustment;
        
        cdf = continueSequenceCDF(token);
        eofAdjustment =  1.0 + MIN_SYMBOL_PROB * (double) cdf.length ;

        cuSum = 0.0;
        for(int i = 0; i < token; i++){
            cuSum += (cdf[i] + MIN_SYMBOL_PROB) / eofAdjustment;
        }

        interval[0] = cuSum;
        interval[1] = cuSum + (cdf[token] + MIN_SYMBOL_PROB) / eofAdjustment;
    }

    /**
     * Finds the token such that CDF(token - 1) is less than pointOnCDF and
     * CDF(token) is greater than pointOnCDF.  Populates interval with CDF(token - 1)
     * and CDF(token) and incorporates the token into the predictive model.
     *
     * @param pointOnCDF point on CDF
     * @param interval container for CDF values
     * @return token such that CDF(token -1) < pointOnCDF and CDF(token) > pointOnCDF
     */
    public int inverseCDF(double pointOnCDF, double [] interval){
        double[] cdf;
        int token;
        Pair<MutableInteger, double[]> ret;
        double cuSum, eofAdjustment;
        
        ret = continueSequencePointOnCDF(pointOnCDF);
        token = ret.first().intVal();
        cdf = ret.second();

        eofAdjustment =  1.0 + MIN_SYMBOL_PROB * (double) cdf.length ;

        cuSum = 0.0;
        for(int i = 0; i < token; i++){
            cuSum += (cdf[i] + MIN_SYMBOL_PROB) / eofAdjustment;
        }

        interval[0] = cuSum;
        interval[1] = cuSum + (cdf[token] + MIN_SYMBOL_PROB) / eofAdjustment;

        return token;
    }
}
