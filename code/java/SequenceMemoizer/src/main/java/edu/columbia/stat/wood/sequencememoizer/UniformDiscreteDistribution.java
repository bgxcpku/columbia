/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.util.Arrays;

/**
 * Uniform base distribution over finite alphabet size.
 *
 * @author nicholasbartlett
 */

public class UniformDiscreteDistribution  extends BaseDiscreteDistribution {

    private int alphabetSize;

    /**
     * Contstructs object with given alphabet size.
     *
     * @param alphabetSize alphabet size
     */
    public UniformDiscreteDistribution(int alphabetSize){
        this.alphabetSize = alphabetSize;
    }

    /**
     * Gets probability from uniform distribution.
     *
     * @param type
     * @return probability probability of type
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
     * Overides CDF() method to provide more efficient implementation.
     *
     * @return predictive CDF
     */
    @Override
    public double[] CDF(){
        double cdf[];

        cdf = new double[alphabetSize];
        Arrays.fill(cdf, 1.0 / (double) alphabetSize);

        return cdf;
    }
}
