/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public class FiniteAlphabetSequenceMemoizerParameters extends SequenceMemoizerParameters {

    /**
     * Finite discrete base distribution used as root.
     */
    public FiniteDiscreteDistribution baseDistribution;

    /**
     * Constructor allowing all of the fields to be specified as arguments.
     *
     * @param discounts
     * @param infiniteDiscount
     * @param depth
     * @param seed
     * @param baseDistribution
     */
    public FiniteAlphabetSequenceMemoizerParameters(double[] discounts, double infiniteDiscount, int depth, long seed, FiniteDiscreteDistribution baseDistribution) {
        this.discounts = discounts;
        this.infiniteDiscount = infiniteDiscount;
        this.depth = depth;
        this.seed = seed;
        this.baseDistribution = baseDistribution;
    }

    /**
     * Default values are discounts = {0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95},
     * infiniteDiscount = 0.5, depth = -1, seed = 1, and baseDistribution = UniformDiscreteDistribution(256).
     *
     * @param alphabetSize
     * @param depth
     * @param seed
     */
    public FiniteAlphabetSequenceMemoizerParameters(int alphabetSize, int depth, long seed){
        this.discounts = new double[]{0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95};
        this.infiniteDiscount = 0.5;
        this.depth = depth;
        this.seed = seed;
        this.baseDistribution = new UniformDiscreteDistribution(alphabetSize);
    }

    /**
     * Default values are discounts = {0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95},
     * infiniteDiscount = 0.5, depth = -1, seed = 1, and baseDistribution = UniformDiscreteDistribution(256).
     *
     * @param alphabetSize
     */

    public FiniteAlphabetSequenceMemoizerParameters(int alphabetSize){
        this(alphabetSize, -1, 1);
    }

    /**
     * Default values are discounts = {0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95},
     * infiniteDiscount = 0.5, depth = -1, seed = 1, and baseDistribution = UniformDiscreteDistribution(256).
     */
    public FiniteAlphabetSequenceMemoizerParameters() {
        this(256);
    }
}
