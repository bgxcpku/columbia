/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import java.util.Iterator;

/**
 * Uniform distribution over the range [leftType, rightType)
 * @author nicholasbartlett
 */
public class UniformByteDiscreteDistribution implements ByteFiniteDiscreteDistribution {
    private int alphabetSize, leftType, rightType;
    private final double p;

    public UniformByteDiscreteDistribution(){
        alphabetSize = 256;
        p = 1.0 / (double) alphabetSize;
        leftType = -128;
        rightType = 128;
    }

    public UniformByteDiscreteDistribution(int leftType, int rightType){
        if(leftType < -128 || leftType > 128 || rightType < -128 || rightType >128){
            throw new IllegalArgumentException("Left and right type must both be between between -128" +
                    "and 128");
        }
        
        alphabetSize = rightType - leftType;
        p = 1.0 / (double) alphabetSize;
        this.leftType = leftType;
        this.rightType = rightType;
    }

    public int alphabetSize(){
        return alphabetSize;
    }

    public double probability(byte type) {
        int t;

        t = (int) type;
        if(t >= leftType && t < rightType){
            return p;
        } else {
            return 0.0;
        }
    }

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
                throw new RuntimeException("No next element, you have reached the end");
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
