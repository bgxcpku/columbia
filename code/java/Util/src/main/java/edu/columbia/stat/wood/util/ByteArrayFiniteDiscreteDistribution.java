/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import java.io.Serializable;
import java.util.Iterator;

/**
 *
 * @author nicholasbartlett
 */
public class ByteArrayFiniteDiscreteDistribution implements ByteDiscreteDistribution, Serializable {

    static final long serialVersionUID = 1;

    private double[] pdf;

    public ByteArrayFiniteDiscreteDistribution(double[] pdf){
        if(pdf.length != 256){
            throw new IllegalArgumentException("pdf must be of length 256 to use this constructor");
        }
        this.pdf = pdf;
    }

    public int alphabetSize() {
        return pdf.length;
    }

    public double probability(byte type) {
        return pdf[(int) type & 0xFF];
    }
    
    public Iterator<Pair<Byte, Double>> iterator() {
        return new ArrayIterator();
    }

    private class ArrayIterator implements Iterator<Pair<Byte, Double>>{
        private int index = 0;

        public boolean hasNext() {
            return index < pdf.length;
        }

        public Pair<Byte, Double> next() {
            Pair<Byte, Double> pr;

            if(index < pdf.length){
                pr = new Pair(new Byte((byte) index), pdf[index]);
                index++;
                return pr;
            } else {
                throw new RuntimeException("No next element, you have reached the end");
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
