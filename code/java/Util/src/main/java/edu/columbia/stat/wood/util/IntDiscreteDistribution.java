/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import java.util.Iterator;

/**
 *
 * @author nicholasbartlett
 */
public interface IntDiscreteDistribution {

    /**
     * Gets the probability of certain type.
     * @param type int type
     * @return probability of this type, this should be in [0, 1.0]
     */
    public double probability(int type);

    /**
     * Gets an iterator over Byte Double pairs such that the Double value is the
     * probability of the Byte value in the distribution.
     * @return iterator
     */
    public Iterator<Pair<Integer, Double>> iterator();
    
}
