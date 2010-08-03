/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 * Simple interface for a discrete distribution.
 * @author nicholasbartlett
 */
public interface DiscreteDistribution {

    /**
     * Gets the probability of the integer type.  Type should be in interval [0, alphabetSize());
     *
     * @param type
     * @return probability of type
     */
    public double probability(int type);

    /**
     * Gets CDF of discrete distribution.
     *
     * @return predictive CDF
     */
    public double[] CDF();

    /**
     * Gets alphabetSize.
     *
     * @return alphabet size
     */
    public int alphabetSize();
}
