/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.util.Comparator;

/**
 *
 * @author nicholasbartlett
 */
public class PairComparatorDouble implements Comparator<Pair<Restaurant, Double>> {

    public PairComparatorDouble() {
    }

    public int compare(Pair<Restaurant, Double> o1, Pair<Restaurant, Double> o2) {
        if (o1.second().doubleValue() < o2.second().doubleValue()) {
            return -1;
        } else if (o1.second().doubleValue() == o2.second().doubleValue()) {
            return 0;
        } else {
            return 1;
        }
    }
}
