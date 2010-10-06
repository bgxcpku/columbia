/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.pdia;

import edu.columbia.stat.wood.util.IntDiscreteDistribution;
import edu.columbia.stat.wood.util.Pair;
import java.util.Iterator;

/**
 *
 * @author nicholasbartlett
 */
public class IntGeometricDistribution implements IntDiscreteDistribution {

    private double p;
    
    public IntGeometricDistribution(double p){
        this.p = p;
    }

    @Override
    public double probability(int type) {
        if(type < 0){
            return 0.0;
        } else if (type == 0){
            return p;
        } else {
            return Math.exp((double) type * Math.log(1.0 - p) + Math.log(p));
        }
    }

    @Override
    public Iterator<Pair<Integer, Double>> iterator() {
        return new GeoIterator();
    }

    private class GeoIterator implements Iterator<Pair<Integer,Double>>{

        private double productOfQ = 1.0;
        private int lastInt = -1;

        @Override
        public final boolean hasNext() {
            return true;
        }

        @Override
        public Pair<Integer, Double> next() {
            Pair<Integer,Double> ret = new Pair(new Integer(++lastInt), productOfQ*p);
            productOfQ *= (1.0 - p);
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
