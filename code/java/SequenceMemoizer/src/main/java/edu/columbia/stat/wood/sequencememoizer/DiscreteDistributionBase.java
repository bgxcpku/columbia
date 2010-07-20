/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public abstract class DiscreteBaseDistributionAbstract implements DiscreteBaseDistribution {

    /**
     * Dumb implementation of cdf based on the probability() method.  Can probably be
     * more efficiently implemented by specific implementations of interface.
     *
     * @return cdf
     */
    public double[] cdf(){
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
