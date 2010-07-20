/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.hpyp;

/**
 *
 * @author nicholasbartlett
 */
public interface DiscreteBaseDistribution {

    /**
     * Gets the probability of the integer type.  Type should be in interval [0, alphabetSize());
     *
     * @param type
     * @return probability of type
     */
    public double probability(int type);

    /**
     * Gets cdf of discrete distribution.
     *
     * @return cdf
     */
    public double[] cdf();

    /**
     * Gets alphabetSize.
     *
     * @return alphabet size
     */
    public int alphabetSize();
}
