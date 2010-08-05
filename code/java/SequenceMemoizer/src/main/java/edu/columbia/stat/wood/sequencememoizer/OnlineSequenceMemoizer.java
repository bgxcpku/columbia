/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.util.Random;

/**
 * A sequence memoizer object using a compressed respresentation and permitting a
 * usage with a constant memory footprint.
 * 
 * @author nicholasbartlett
 */
public class OnlineSequenceMemoizer {

    public static Random RNG;
    
    private int alphabetSize, depth;
    public OnlineRestaurant emptyContextRestaurant;
    private OnlineBaseRestaurant baseRestaurant;
    private Discounts discounts;
    private long seed;
    private FiniteDiscreteDistribution baseDistribution;
    private int[] context;

    public OnlineSequenceMemoizer(FiniteAlphabetSequenceMemoizerParameters params){
       alphabetSize = params.baseDistribution.alphabetSize();
       depth = params.depth;
       seed = params.seed;
       discounts = new Discounts(params.discounts, params.infiniteDiscount);
       baseDistribution = params.baseDistribution;

       RNG = new Random(seed);
       baseRestaurant = new OnlineBaseRestaurant(params.baseDistribution);
       emptyContextRestaurant = new OnlineRestaurant(baseRestaurant, new int[0], discounts);
       context = new int[0];
    }

    public double continueSequence(int observation) {
        if (observation < 0 || observation >= alphabetSize) {
            throw new IllegalArgumentException("Observations must be integers in the interval [0,alphabetSize).");
        }

        MutableDouble p;

        if(emptyContextRestaurant.seat(baseRestaurant.predictiveProbability(observation), observation, 0, depth, context, context.length-1, p = new MutableDouble(-1), new MutableDouble(1.0))){
            baseRestaurant.seat(observation);
        }

        addToContext(observation);
        discounts.stepDiscounts(0.0001, p.doubleVal());

        return Math.log(p.doubleVal());
    }

    private void addToContext(int type){
        if(context.length == depth){
            for(int i = 0; i< context.length - 1; i++){
                context[i] = context[i + 1];
            }
            context[context.length - 1] = type;
        } else if(context!= null) {
            int[] newContext;

            newContext = new int[context.length + 1];
            System.arraycopy(context, 0, newContext, 0, context.length);
            newContext[context.length] = type;
            context = newContext;
        } else {
            context = new int[]{type};
        }
    }
}
