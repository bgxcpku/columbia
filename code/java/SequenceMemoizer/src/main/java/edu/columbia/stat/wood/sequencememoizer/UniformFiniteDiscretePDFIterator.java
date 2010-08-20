/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.util.NoSuchElementException;

/**
 * An iterator object to iterate over the type probability pairs which define
 * the PDF of a uniform finite discrete distribution.
 *
 * @author nicholasbartlett
 */
public class UniformFiniteDiscretePDFIterator {
    private int alphabetSize, index;
    private double p;

    /**
     * @param alphabetSize finite size of alphabet
     */
    public UniformFiniteDiscretePDFIterator(int alphabetSize){
        //super(null);
        p = 1.0 / (double) alphabetSize;
    }

    /**
     * Checks if there is a next value available.
     *
     * @return if there is an available element of the PDF true, else false
     */
    //@Override
    public boolean hasNext() {
        return index < alphabetSize;
    }

    /**
     * Gets the next type in the distribution and the  corresponding value of the PDF.  
     * This allows you to iterate over the PDF in an abritrary order.
     *
     * @return an Integer, Double pair containing the type and the probability of that type
     */
    //@Override
    public Pair<Integer, Double> next() {
        if(index >= alphabetSize){
            throw new NoSuchElementException();
        }
        return new Pair(index++, p);
    }
}
