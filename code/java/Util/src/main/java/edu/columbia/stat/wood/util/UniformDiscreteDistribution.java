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
public class UniformDiscreteDistribution implements FiniteDiscreteDistribution {
    private int alphabetSize, leftType, rightType;
    private double p;

    public UniformDiscreteDistribution(int alphabetSize){
        this.alphabetSize = alphabetSize;
        p = 1.0 / (double) alphabetSize;
        leftType = 0;
        rightType = alphabetSize;
    }

    public UniformDiscreteDistribution(int leftType, int rightType){
        alphabetSize = rightType - leftType;
        p = 1.0 / (double) alphabetSize;
        this.leftType = leftType;
        this.rightType = rightType;
    }

    public int alphabetSize(){
        return alphabetSize;
    }

    public double probability(int type) {
        if(type >= leftType && type < rightType){
            return p;
        } else {
            return 0.0;
        }
    }

    public Iterator<Pair<Integer, Double>> iterator() {
        return new UniformIterator();
    }

    private class UniformIterator implements Iterator<Pair<Integer, Double>>{
        private int index = leftType;

        public boolean hasNext() {
            return index < rightType;
        }

        public Pair<Integer,Double> next() {
            if(index++ < rightType){
                return new Pair(index,p);
            } else {
                throw new RuntimeException("No next element, you have reached the end");
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
