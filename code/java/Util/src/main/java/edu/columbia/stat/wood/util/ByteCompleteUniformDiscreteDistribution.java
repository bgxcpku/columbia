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
public class ByteCompleteUniformDiscreteDistribution extends ByteUniformDiscreteDistribution {
    private final int alphabetSize, leftType, rightType;
    private final double p;

    public ByteCompleteUniformDiscreteDistribution(){
        alphabetSize = 256;
        p = 1.0 / 256.0;

        leftType = -128;
        rightType = 128;
    }

    @Override
    public final int alphabetSize(){
        return alphabetSize;
    }

    @Override
    public final double probability(byte type) {
        return p;
    }

    @Override
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
