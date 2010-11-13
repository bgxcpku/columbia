/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;


import edu.columbia.stat.wood.pub.sequencememoizer.util.Pair;
import java.io.Serializable;
import java.util.Iterator;

/**
 * Array based discrete distribution over all 256 bytes.
 * @author nicholasbartlett
 */
public class ByteArrayFiniteDiscreteDistribution implements ByteDiscreteDistribution, Serializable {

    static final long serialVersionUID = 1;

    private double[] pdf;

    /**
     * @param pdf pdf such that byte b will occur with probability pdf[(int) b & 0xFF].  The
     * operation (int) b & 0xFF reverses the coercion of an int to a byte.
     */
    public ByteArrayFiniteDiscreteDistribution(double[] pdf){
        if(pdf.length != 256){
            throw new IllegalArgumentException("pdf must be of length 256 to use this constructor");
        }
        this.pdf = pdf;
    }

    /**
     * Gets the alphabet size, which for this implementation will always be 256.
     * @return 256
     */
    public final int alphabetSize() {
        return 256;
    }

    /**
     * Gets the probability of the type.
     * @param type
     * @return probability of type
     */
    public double probability(byte type) {
        return pdf[(int) type & 0xFF];
    }

    /**
     * Gets iterator over Byte Double pairs for this distribution.
     * @return iterator
     */
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
