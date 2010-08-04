/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.util.NoSuchElementException;

/**
 *
 * @author nicholasbartlett
 */
public class UniformFiniteDiscretePDFIterator extends FiniteDiscretePDFIterator {
    private int alphabetSize, index;
    private double p;

    public UniformFiniteDiscretePDFIterator(int alphabetSize){
        super(null);
        p = 1.0 / (double) alphabetSize;
    }

    /**
     * Checks if there is a next value available.
     *
     * @return if there is an available element of the PDF true, else false
     */
    @Override
    public boolean hasNext() {
        return index < alphabetSize;
    }

    /**
     * Gets the next value of the PDF and the type to which it corresponds.  This allows
     * you to iterate over the PDF in the order the prorammer finds most convenient.
     *
     * @return an Integer, Double pair representing the type and the probability of that type
     */
    @Override
    public Pair<Integer, Double> next() {
        if(index >= alphabetSize){
            throw new NoSuchElementException();
        }
        return new Pair(index++, p);
    }
}
