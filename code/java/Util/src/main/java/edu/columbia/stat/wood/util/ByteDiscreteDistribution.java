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

public interface ByteDiscreteDistribution {
    
    public double probability(byte type);

    public int alphabetSize();

    public Iterator<Pair<Byte, Double>> iterator();
}