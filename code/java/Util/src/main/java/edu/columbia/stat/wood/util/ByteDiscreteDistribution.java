/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import edu.columbia.stat.wood.pub.sequencememoizer.util.Pair;
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
     * Gets an iterator over Byte Double pairs such that the Double value is the
     * probability of the Byte value in the distribution.  Should iterate over all
     * Byte values which have probability mass assigned to them in this distribution.
     * @return iterator
     */
    public Iterator<Pair<Byte, Double>> iterator();
}