/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import edu.columbia.stat.wood.pub.sequencememoizer.util.Pair;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Uniform distribution over the range [leftType, rightType)
 * @author nicholasbartlett
 */
public class ByteUniformDiscreteDistribution implements ByteDiscreteDistribution, Serializable {

    static final long serialVersionUID = 1;

    private int alphabetSize, leftType, rightType;
    private final double p;

    /**
     * Empty constructor creates uniform distribution over all 256 bytes.
     */
    public ByteUniformDiscreteDistribution(){
        alphabetSize = 256;
        p = 1.0 / (double) alphabetSize;
        leftType = -128;
        rightType = 128;
    }

    /**
     * Creates uniform distribution over bytes in [leftType, rightType)
     * @param leftType
     * @param rightType
     */
    public ByteUniformDiscreteDistribution(int leftType, int rightType){
        if(leftType < -128 || leftType > 128 || rightType < -128 || rightType >128){
            throw new IllegalArgumentException("Left and right type must both be between between -128" +
                    "and 128");
        }
        
        alphabetSize = rightType - leftType;
        p = 1.0 / (double) alphabetSize;
        this.leftType = leftType;
        this.rightType = rightType;
    }

    /**
     * Will return the alphabet size.
     * @return alphabet size
     */
    public int alphabetSize(){
        return alphabetSize;
    }

    /**
     * Gets the probability of a given type.
     * @param type
     * @return probability of type
     */
    public double probability(byte type) {
        if(type >= leftType && type < rightType){
            return p;
        } else {
            return 0.0;
        }
    }

    /**
     * Gets an iterator over Byte Double pairs for this distribution.
     * @return iterator
     */
    public Iterator<Pair<Byte, Double>> iterator() {
        return new UniformIterator();
    }

    private class UniformIterator implements Iterator<Pair<Byte, Double>>{
        private int index = leftType;

        public boolean hasNext() {
            return index < rightType;
        }

        public Pair<Byte,Double> next() {
            Pair<Byte,Double> pr;

            if(index < rightType){
                pr = new Pair(new Byte((byte) index), p);
                index++;
                return pr;
            } else {
                throw new NoSuchElementException("No next element, you have reached the end");
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
