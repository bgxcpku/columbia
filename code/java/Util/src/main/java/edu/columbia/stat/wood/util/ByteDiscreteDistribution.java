/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import java.util.Iterator;

/**
 * Discrete distribution for byte types.
 * @author nicholasbartlett
 */

public interface ByteDiscreteDistribution {

    /**
     * Gets the probability of certain type.
     * @param type byte type
     * @return probability of this type, this should be in [0, 1.0]
     */
    public double probability(byte type);

    /**
     * Gets the alphabet size.  Since bytes only take values from -128 - 127, this is
     * not a restriction.
     * @return alphabet size
     */
    //public int alphabetSize();

    /**
     * Gets an iterator over Byte Double pairs such that the Double value is the
     * probability of the Byte value in the distribution.
     * @return iterator
     */
    public Iterator<Pair<Byte, Double>> iterator();
}