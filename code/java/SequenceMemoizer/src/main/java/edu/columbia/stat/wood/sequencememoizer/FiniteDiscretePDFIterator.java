/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.util.NoSuchElementException;

/**
 * Iterator object for the PDF of a finite discrete distribution.
 *
 * @author nicholasbartlett
 */
public class FiniteDiscretePDFIterator extends DiscretePDFIterator{
    private double[] pdf;
    private int index, l;

    /**
     * Creates the iterator object with the specified double[] backing it.
     * @param pdf the actual PDF ordered by type
     */
    public FiniteDiscretePDFIterator(double[] pdf){
        this.pdf = pdf;
        index = 0;
        l = pdf.length;
    }

    /**
     * Checks if there is a next value available.
     * 
     * @return if there is an available element of the PDF true, else false
     */
    public boolean hasNext() {
        return index < l;
    }

    /**
     * Gets the next value of the PDF and the type to which it corresponds.  This allows
     * you to iterate over the PDF in arbitrary order.
     *
     * @return an Integer, Double pair containing a type and the probability of that type
     */
    public Pair<Integer, Double> next() {
        if(index >= l){
            throw new NoSuchElementException();
        }
        return new Pair(index, pdf[index++]);
    }

    /**
     * Resets the iterator object to start over again at the beginning of the
     * backing array.
     */
    public void reset(){
        index = 0;
    }

}
