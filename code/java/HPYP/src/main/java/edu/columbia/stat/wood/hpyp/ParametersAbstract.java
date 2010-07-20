/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.hpyp;

/**
 * Container object for parameters of an HPYP model.
 * 
 * @author nicholasbartlett
 */

public class ParametersAbstract {
    /**
     * Shows the unique discount parameters.  Discounts are parameterized such that discounts[i] is
     * the discount for restaurants with depth i.  Depths >= discounts.length are handled in
     * a model specific way.
     */
    public double[] discounts;
    /**
     * Size of discrete alphabet.
     */
    public int alphabetSize;
    /**
     * Seed used for random number generator.
     */
    public long seed;
    /**
     * Max depth allowable by the model.  If using an infinte depth model then
     * this will be set to -1.
     */
    public int depth;
    /**
     * Base distribution above the empty context restaurant.
     */
    public DiscreteBaseDistribution baseDistribution;
}
