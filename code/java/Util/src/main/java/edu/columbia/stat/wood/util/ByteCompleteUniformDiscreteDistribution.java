/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import edu.columbia.stat.wood.pub.sequencememoizer.util.Pair;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Class to implement uniform distribution over all bytes.
 * @author nicholasbartlett
 */
public class ByteCompleteUniformDiscreteDistribution extends ByteUniformDiscreteDistribution {
    private final int alphabetSize, leftType, rightType;
    private final double p;

    /**
     * Creates uniform distribution over all 256 bytes.
     */
    public ByteCompleteUniformDiscreteDistribution(){
        alphabetSize = 256;
        p = 1.0 / 256.0;

        leftType = -128;
        rightType = 128;
    }

    /**
     * Gets alphabet size.
     * @return 256
     */
    @Override
    public final int alphabetSize(){
        return alphabetSize;
    }

    /**
     * Gets probability of type
     * @param type
     * @return 1.0 / 256.0
     */
    @Override
    public final double probability(byte type) {
        return p;
    }

    /**
     * Iterator over Byte Double pairs for this distribution.
     * @return iterator
     */
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
                throw new NoSuchElementException("No next element, you have reached the end");
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
