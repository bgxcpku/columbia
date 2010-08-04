/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 * Interface for a discrete distribution.
 *
 * @author nicholasbartlett
 */
public interface DiscreteDistribution {

    /**
     * Gets the probability of the integer type.
     *
     * @param type
     * @return probability of type
     */
    public double probability(int type);

    /**
     * Gets an iterator object which iterates over the set of type, probability pairs
     * which defines the PDF of this discrete distribution.
     *
     * @return iterator
     */
    public DiscretePDFIterator iterator();

}
