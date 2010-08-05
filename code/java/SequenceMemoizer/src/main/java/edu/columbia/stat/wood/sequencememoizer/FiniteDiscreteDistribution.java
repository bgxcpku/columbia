/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 * A discrete distribution with a finite alphabet size. Types must be in [0, alphabet size).
 *
 * @author nicholasbartlett
 */
public abstract class FiniteDiscreteDistribution implements DiscreteDistribution{

    /**
     * Gets the finite alphabet size for this discrete distribution.
     *
     * @return alphabet size
     */
    public abstract int alphabetSize();

    /**
     * Gets the PDF for this finite discrete distribution.
     *
     * @return PDF ordered by type
     */
    public double[] PDF() {
        double[] pdf;
        int as;

        as = alphabetSize();

        pdf = new double[as];
        for(int i = 0 ; i < as; i++){
            pdf[i] = probability(i);
        }

        return pdf;
    }

    /**
     * Fills the provided container object with the PDF.
     *
     * @param pdf container object to be filled with PDF ordered by type
     */
    public void PDF(double[] pdf){
        if(pdf.length != alphabetSize()){
            throw new IllegalArgumentException("The provided pdf is of length "
                    + pdf.length + " but needs to be of length " + alphabetSize() + ".");
        }

        int as;

        as = alphabetSize();

        pdf = new double[as];
        for(int i = 0 ; i < as; i++){
            pdf[i] = probability(i);
        }
    }

    /**
     * Gets an iterator object which iterates over the set of type, probability pairs
     * which defines the PDF of this discrete distribution.
     *
     * @return iterator
     */
    public FiniteDiscretePDFIterator iterator(){
        return new FiniteDiscretePDFIterator(PDF());
    }
}
