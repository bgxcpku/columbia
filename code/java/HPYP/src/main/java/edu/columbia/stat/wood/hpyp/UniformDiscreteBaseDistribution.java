/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.hpyp;

import java.util.Arrays;

/**
 * Uniform base distribution over finite alphabet size.
 * @author nicholasbartlett
 */

public class UniformDiscreteBaseDistribution  extends DiscreteBaseDistributionAbstract {

    private int alphabetSize;

    /**
     * Contstructs object with given alphabet size.
     *
     * @param alphabetSize alphabet size
     */
    public UniformDiscreteBaseDistribution(int alphabetSize){
        this.alphabetSize = alphabetSize;
    }

    /**
     * Gets probability from uniform distribution.
     *
     * @param type
     * @return probability
     */
    public double probability(int type) {
        return 1.0 / (double) alphabetSize;
    }

    /**
     * Gets alphabet size.
     *
     * @return alphabet size
     */
    public int alphabetSize() {
        return alphabetSize;
    }

    /**
     * Overides cdf() method to provide more efficient implementation.
     *
     * @return cdf
     */
    @Override
    public double[] cdf(){
        double cdf[];

        cdf = new double[alphabetSize];
        Arrays.fill(cdf, 1.0 / (double) alphabetSize);

        return cdf;
    }
}
