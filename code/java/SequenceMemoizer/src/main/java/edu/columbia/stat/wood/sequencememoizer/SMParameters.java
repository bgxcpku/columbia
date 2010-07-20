/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

/**
 * A container object for the parameters of a sequence memoizer. For depths >= discounts.length, the discount is
 * discounts[discounts.length -1]^(alpha ^ (depth - discounts.length + 1)).  Alpha is set such that the marginal
 * discount value for an infinitely deep restaurant, which governs its relationship to restaurants are the
 * discounts.length - 1 depth, is equal to the infiniteDiscount.
 *
 * @author nicholasbartlett
 */
public class SMParameters extends ParametersAbstract{

    /**
     * Mariginal discount of an infinite depth restaurant wrt restaurants at the discounts.lenghth - 1 depth.
     */
    public double infiniteDiscount;

    /**
     * Constructor allowing all of the fields to be specified as arguments.
     *
     * @param discounts double[] of discounts
     * @param infiniteDiscount discount governing difference between infinite context restaurants and restaurants with contexts of length  = discounts.length-1
     * @param alphabetSize
     * @param depth maximum context length
     * @param seed seed for random number generator
     */
    public SMParameters(double[] discounts, double infiniteDiscount, int alphabetSize, int depth, long seed, DiscreteBaseDistribution baseDistribution) {
        this.discounts = discounts;
        this.infiniteDiscount = infiniteDiscount;
        this.alphabetSize = alphabetSize;
        this.depth = depth;
        this.seed = seed;
        this.baseDistribution = baseDistribution;
    }

    /**
     * Default values are discounts = {0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95},
     * infiniteDiscount = 0.5, alphabetSize = 256, depth = -1, and seed = 0;
     *
     * @param infiniteDiscount
     * @param alphabetSize
     * @param depth
     * @param seed
     */
    public SMParameters(double infiniteDiscount, int alphabetSize, int depth, long seed) {
        this.discounts = new double[]{0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95};
        this.infiniteDiscount = infiniteDiscount;
        this.alphabetSize = alphabetSize;
        this.depth = depth;
        this.seed = seed;
        this.baseDistribution = new UniformDiscreteBaseDistribution(alphabetSize);
    }

    /**
     * Default values are discounts = {0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95},
     * infiniteDiscount = 0.5, alphabetSize = 256, depth = -1, and seed = 0;
     *
     * @param discounts
     * @param alphabetSize
     * @param depth
     * @param seed
     */
    public SMParameters(double[] discounts, int alphabetSize, int depth, long seed) {
        this.discounts = discounts;
        this.infiniteDiscount = 0.5;
        this.alphabetSize = alphabetSize;
        this.depth = depth;
        this.seed = seed;
        this.baseDistribution = new UniformDiscreteBaseDistribution(alphabetSize);
    }

    /**
     * Default values are discounts = {0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95},
     * infiniteDiscount = 0.5, alphabetSize = 256, depth = -1, and seed = 0;
     *
     * @param alphabetSize
     * @param depth
     * @param seed
     */
    public SMParameters(int alphabetSize, int depth, long seed) {
        this(0.5, alphabetSize, depth, seed);
    }

    /**
     * Default values are discounts = {0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95},
     * infiniteDiscount = 0.5, alphabetSize = 256, depth = -1, and seed = 0;
     *
     * @param alphabetSize
     * @param seed
     */
    public SMParameters(int alphabetSize, long seed) {
        this(0.5, alphabetSize, -1, seed);
    }

    /**
     * Default values are discounts = {0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95},
     * infiniteDiscount = 0.5, alphabetSize = 256, depth = -1, and seed = 0;
     */
    public SMParameters() {
        this(0.5, 256, -1, 0);
    }
}
