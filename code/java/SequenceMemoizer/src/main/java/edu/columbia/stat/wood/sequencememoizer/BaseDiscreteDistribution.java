/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 * Implements some methods of Discrete Distribution.
 * 
 * @author nicholasbartlett
 */
public abstract class BaseDiscreteDistribution implements DiscreteDistribution {

    /**
     * Dumb implementation of CDF based on the probability() method.  Can probably be
     * more efficiently implemented by specific implementations of interface.
     *
     * @return predictive CDF
     */
    public double[] CDF(){
        double[] cdf;
        int as;

        as = alphabetSize();

        cdf = new double[as];
        for(int i = 0; i < as; i++){
            cdf[i] = probability(i);
        }

        return cdf;
    }

}
