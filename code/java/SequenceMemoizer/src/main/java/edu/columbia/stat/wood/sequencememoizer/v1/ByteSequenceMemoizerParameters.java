/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer.v1;

import edu.columbia.stat.wood.pub.sequencememoizer.SequenceMemoizerParameters;
import edu.columbia.stat.wood.util.ByteCompleteUniformDiscreteDistribution;
import edu.columbia.stat.wood.util.ByteDiscreteDistribution;

/**
 * Parameter object specific to the sequence memoizer with byte types.
 * @author nicholasbartlett
 */
public class ByteSequenceMemoizerParameters extends SequenceMemoizerParameters{

    /**
     * Discrete base distribution for byte types.
     */
    public ByteDiscreteDistribution baseDistribution;

    /**
     * Constructor allowing all of the parameters to be specified.
     * @param baseDistribution
     * @param discounts
     * @param infiniteDiscount
     * @param depth
     * @param seed
     * @param maxNumberRestaurants
     * @param maxSequenceLength
     */
    public ByteSequenceMemoizerParameters(ByteDiscreteDistribution baseDistribution, double[] discounts, double infiniteDiscount, int depth, long seed, long maxNumberRestaurants, long maxSequenceLength, long maxCustomersInRestaurant) {
        super(discounts, infiniteDiscount, depth, seed, maxNumberRestaurants, maxSequenceLength,maxCustomersInRestaurant);
        this.baseDistribution = baseDistribution;
    }

    /**
     * Constructor allowing some parameters to be specified.  The default base distribution
     * is uniform over all 256 bytes.
     * @param depth
     * @param maxNumberRestaurants
     * @param maxSequenceLength
     */
    public ByteSequenceMemoizerParameters(int depth, long maxNumberRestaurants, long maxSequenceLength){
        super(depth, maxNumberRestaurants, maxSequenceLength);
        baseDistribution = new ByteCompleteUniformDiscreteDistribution();
    }

    /**
     * Constructor allowing some parameters to be specified.  The default base distribution
     * is uniform over all 256 bytes.
     * @param depth
     */
    public ByteSequenceMemoizerParameters(int depth){
        super(depth, Long.MAX_VALUE, Long.MAX_VALUE);
        baseDistribution = new ByteCompleteUniformDiscreteDistribution();
    }

    /**
     * The default base distribution is uniform over all 256 bytes.
     */
    public ByteSequenceMemoizerParameters() {
        super();
        baseDistribution = new ByteCompleteUniformDiscreteDistribution();
    }
}
