/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

/**
 * A container object for the parameters of a sequence memoizer. For depths >= discounts.length, the discount is
 * discounts[discounts.length -1]^(alpha ^ (depth - discounts.length + 1)).  Alpha is set such that the product of discounts
 * for depths = discounts.length through infinity converges to the specified infinite discount.  This prevents the
 * product of the discounts from converging to 0.0 and helps prevent the overdeterminism seen in some other parameterizations
 * of the model.
 *
 * @author nicholasbartlett
 */
public class SequenceMemoizerParameters {
    /**
     * Unique discount parameters for depth in [0 , discounts.length).
     */
    public double[] discounts;

    /**
     * The product of the discounts for depths of discounts.length through infinity will converge to this infinite discount.
     * In more interpretable terms this means that if condition distribution R is at depth discounts.length-1 then the prior indicates that
     * restaurants at near infinite depth which share the same recent context of length discounts.length-1 will follow a Pitman-Yor
     * distribution centered at R with discount infiniteDiscount and concentration 0.0.
     */
    public double infiniteDiscount;

    /**
     * Max depth of model.
     */
    public int depth;

    /**
     * Random number seed used.
     */
    public long seed;

    /**
     * Constructor allowing all of the fields to be specified as arguments.
     * @param discounts 
     * @param infiniteDiscount
     * @param depth 
     * @param seed
     */
    public SequenceMemoizerParameters(double[] discounts, double infiniteDiscount, int depth, long seed) {
        this.discounts = discounts;
        this.infiniteDiscount = infiniteDiscount;
        this.depth = depth;
        this.seed = seed;
    }

    /**
     * Constructor allowing for some of the parameters to be specified.
     * Default values are discounts = {0.5, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95},
     * infiniteDiscount = 0.5, depth = 1023, seed = 3.
     * @param depth
     * @param seed
     */
    public SequenceMemoizerParameters(int depth, long seed){
        this.discounts = new double[]{0.5, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95};
        this.infiniteDiscount = 0.5;
        this.depth = depth;
        this.seed = seed;
    }

    /**
     * Default values are discounts = {0.5, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95},
     * infiniteDiscount = 0.5, depth = 1023, seed = 3.
     */
    public SequenceMemoizerParameters() {
        this(1023, 3);
    }
}
