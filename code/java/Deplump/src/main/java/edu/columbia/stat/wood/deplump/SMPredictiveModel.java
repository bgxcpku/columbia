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
 *
 * @author nicholasbartlett
 */
public class SMPredictiveModel extends SamplingSequenceMemoizer implements PredictiveModel {

    public SMPredictiveModel(){
        super(new SMParameters(256, -1, 0));
    }

    public void cumulativeDistributionInterval(int token, double[] interval) {
        double[] cdf;
        double cuSum;
        
        cdf = continueSequenceCdf(token);

        cuSum = 0.0;
        for(int i = 0; i < token; i++){
            cuSum += cdf[i];
        }

        interval[0] = cuSum;
        interval[1] = cuSum + cdf[token];
    }
    
    public int inverseCDF(double pointOnCDF, double [] interval){
        double[] cdf;
        int token;
        Pair<MutableInteger, double[]> ret;
        double cuSum;

        ret = this.continueSequencePointOnCdf(pointOnCDF);
        token = ret.first().intVal();
        cdf = ret.second();

        cuSum = 0.0;
        for(int i = 0; i < token; i++){
            cuSum += cdf[i];
        }

        interval[0] = cuSum;
        interval[1] = cuSum + cdf[token];

        return token;
    }
}
