/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
import java.util.Comparator;

public class PairComparator implements Comparator<Pair<Restaurant, Integer>> {

    public PairComparator() {
    }

    public int compare(Pair<Restaurant, Integer> o1, Pair<Restaurant, Integer> o2) {
        if (o1.second().intValue() < o2.second().intValue()) {
            return -1;
        } else if (o1.second().intValue() == o2.second().intValue()) {
            return 0;
        } else {
            return 1;
        }
    }
}
