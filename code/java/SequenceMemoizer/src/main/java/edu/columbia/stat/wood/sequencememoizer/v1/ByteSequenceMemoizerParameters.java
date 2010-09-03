/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer.v1;

import edu.columbia.stat.wood.util.ByteCompleteUniformDiscreteDistribution;
import edu.columbia.stat.wood.util.ByteDiscreteDistribution;

/**
 *
 * @author nicholasbartlett
 */
public class ByteSequenceMemoizerParameters extends SequenceMemoizerParameters{

    public ByteDiscreteDistribution baseDistribution;

    public ByteSequenceMemoizerParameters(ByteDiscreteDistribution baseDistribution, double[] discounts, double infiniteDiscount, int depth, long seed, long maxNumberRestaurants, long maxSequenceLength) {
        super(discounts, infiniteDiscount, depth, seed, maxNumberRestaurants, maxSequenceLength);
        this.baseDistribution = baseDistribution;
    }

    public ByteSequenceMemoizerParameters(int depth, long maxNumberRestaurants, long maxSequenceLength){
        super(depth, maxNumberRestaurants, maxSequenceLength);
        baseDistribution = new ByteCompleteUniformDiscreteDistribution();
    }

    public ByteSequenceMemoizerParameters(int depth){
        super(depth, -1, -1);
        baseDistribution = new ByteCompleteUniformDiscreteDistribution();
    }
   
    public ByteSequenceMemoizerParameters() {
        super();
        baseDistribution = new ByteCompleteUniformDiscreteDistribution();
    }
}
