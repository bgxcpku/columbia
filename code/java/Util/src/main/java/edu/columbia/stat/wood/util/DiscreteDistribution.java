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

public interface DiscreteDistribution {
    
    public double probability(int type);

    public Iterator<Pair<Integer, Double>> iterator();
}