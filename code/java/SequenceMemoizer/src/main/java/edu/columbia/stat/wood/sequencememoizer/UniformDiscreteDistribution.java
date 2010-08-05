/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.util.Arrays;

/**
 * Uniform distribution over finite alphabet size.  Types must be in
 * the interval [0, alphabet size).
 *
 * @author nicholasbartlett
 */

public class UniformDiscreteDistribution extends FiniteDiscreteDistribution {

    private int alphabetSize;
    private double p;

    /**
     * Contstructs the object with given alphabet size.
     *
     * @param alphabetSize alphabet size
     */
    public UniformDiscreteDistribution(int alphabetSize){
        if(alphabetSize <= 0){
            throw new IllegalArgumentException("The alphabet size must be greater than zero.");
        }
        this.alphabetSize = alphabetSize;
        p = 1.0 / (double) alphabetSize;
    }

    /**
     * Gets the probability from the uniform distribution.
     *
     * @param type
     * @return probability probability of type
     */
    public double probability(int type) {
        if(type < 0 || type >= alphabetSize){
            throw new IllegalArgumentException("The type must be in [0, alphabetSize).");
        }
        return p;
    }

    /**
     * Gets the alphabet size.
     *
     * @return alphabet size
     */
    public int alphabetSize() {
        return alphabetSize;
    }

    /**
     * Overides PDF() method to provide more efficient implementation.
     *
     * @return PDF ordered by type
     */
    @Override
    public double[] PDF(){
        double pdf[];

        pdf = new double[alphabetSize];
        Arrays.fill(pdf, p);

        return pdf;
    }

    /**
     * Overides PDF(double[] pdf) method to proved more efficient implementation.
     *
     * @param pdf container object for PDF, ordered by type
     */
    @Override
    public void PDF(double[] pdf){
        if(pdf.length != alphabetSize){
            throw new IllegalArgumentException("The provided pdf is of length "
                    + pdf.length + " but needs to be of length " + alphabetSize + ".");
        }
        Arrays.fill(pdf, p);
    }

    /**
     * Gets an iterator object which iterates over the set of type, probability pairs
     * which define the PDF of this discrete distribution.
     *
     * @return iterator object
     */
    @Override
    public UniformFiniteDiscretePDFIterator iterator(){
        return new UniformFiniteDiscretePDFIterator(alphabetSize);
    }
}
