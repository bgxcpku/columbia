/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.stochasticmemoizerforsequencedata;

import edu.columbia.stat.wood.hpyp.DiscreteBaseDistribution;
import edu.columbia.stat.wood.hpyp.Sequence;
import edu.columbia.stat.wood.hpyp.HPYPAbstract;
import edu.columbia.stat.wood.hpyp.MutableDouble;
import edu.columbia.stat.wood.hpyp.MutableInteger;
import edu.columbia.stat.wood.hpyp.Pair;
import java.util.Random;

/**
 *
 * @author nicholasbartlett
 * 
 */

public class SequenceMemoizer extends HPYPAbstract {
    /**
     * Random object used for random number generation throughout the model.
     */
    
    public static Random RNG;
    
    private int alphabetSize, depth;
    private long seed;
    private Restaurant emptyContextRestaurant;
    private BaseRestaurant baseRestaurant;
    private Sequence sequence;
    private Discounts discounts;
    private DiscreteBaseDistribution baseDistribution;

    public SequenceMemoizer(SMParameters params){
       alphabetSize = params.alphabetSize;
       depth = params.depth;
       seed = params.seed;
       discounts = new Discounts(params.discounts, params.infiniteDiscount);
       baseDistribution = params.baseDistribution;

       RNG = new Random(seed);
       sequence = new Sequence();
       baseRestaurant = new BaseRestaurant(baseDistribution);
       emptyContextRestaurant = new Restaurant(baseRestaurant, 0,0, discounts);
    }

    public void limitMemory(long maxNumberRestaurants){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double continueSequence(int observation) {
        if (observation < 0 || observation >= alphabetSize) {
            throw new IllegalArgumentException("Observations must be integers in the interval [0,alphabetSize).");
        }

        MutableDouble p;

        if(emptyContextRestaurant.seat(baseRestaurant.predictiveProbability(observation), observation, 0, depth, sequence.fullSeq(), sequence.length()-1, p = new MutableDouble(-1), new MutableDouble(1.0))){
            baseRestaurant.seat(observation);
        }

        sequence.add(observation);
        discounts.stepDiscounts(0.0001, p.doubleVal());

        return Math.log(p.doubleVal());
    }

    public double[] continueSequenceCdf(int observation) {
        if (observation < 0 || observation >= alphabetSize) {
            throw new IllegalArgumentException("Observations must be integers in the interval [0,alphabetSize).");
        }

        double[] cdf;

        cdf = baseRestaurant.predictiveProbability();

        if(emptyContextRestaurant.seatCdf(cdf, observation, 0, depth, sequence.fullSeq(),sequence.length() - 1, new MutableDouble(1.0))) {
            baseRestaurant.seat(observation);
        }

        sequence.add(observation);
        discounts.stepDiscounts(0.0001, cdf[observation]);

        return cdf;
    }

    public Pair<MutableInteger, double[]> continueSequencePointOnCdf(double pointOnCdf) {
        if (pointOnCdf < 0.0 || pointOnCdf >= 1.0) {
            throw new IllegalArgumentException("Point on CDF must be a double in the interval [0,1).");
        }

        MutableInteger type;
        double[] cdf;

        type = new MutableInteger(-1);
        cdf = baseRestaurant.predictiveProbability();

        if(emptyContextRestaurant.seatPointOnCdf(pointOnCdf, cdf, type, 0, depth, sequence.fullSeq(), sequence.length()-1, new MutableDouble(1.0))){
            baseRestaurant.seat(type.intVal());
        }

        sequence.add(type.intVal());
        discounts.stepDiscounts(0.0001, cdf[type.intVal()]);

        return new Pair(type, cdf);
    }

    public int[] generate(Sequence context, int numSamples) {
        double[] cdf;
        int[] samples;
        double r, cuSum;

        samples = new int[numSamples];
        cdf = predictiveProbability(context);

        for(int i = 0; i < numSamples; i++){
            r = RNG.nextDouble();
            cuSum = 0.0;
            for(int token = 0; token < cdf.length; token++){
                cuSum += cdf[token];
                if(cuSum > r){
                    samples[i] = token;
                    break;
                }
                assert token != cdf.length - 1;
            }
        }

        return samples;
    }

    public double[] predictiveProbability(Sequence context) {
        return get(emptyContextRestaurant, context.fullSeq(), context.length()-1, true).predictiveProbability();
    }

    public double predictiveProbability(Sequence context, int token){
        return get(emptyContextRestaurant, context.fullSeq(), context.length()-1, false).predictiveProbability(token);
    }

    public double sequenceProbability(Sequence initialContext, int[] sequence) {
        double logPredictiveProb;
        Sequence context;

        logPredictiveProb = 0.0;
        context = initialContext;

        for(int i = 0; i < sequence.length; i++){
            logPredictiveProb += Math.log(get(emptyContextRestaurant,context.fullSeq(), context.length()-1,true).predictiveProbability(sequence[i]));
            context.add(sequence[i]);
        }

        return logPredictiveProb;
    }

    public double sample(int numSweeps) {
        double logLik;

        logLik = 0.0;

        for(int i = 0; i < numSweeps; i++){
            sampleSeatingArrangments(emptyContextRestaurant);
            logLik = sampleDiscounts(.07);
        }
        
        return logLik;
    }

    public double score() {
        return score(emptyContextRestaurant);
    }

    public SMParameters getParameters() {
        double[] d;

        d = new double[discounts.length()];
        for(int i = 0; i < discounts.length(); i++){
            d[i] = discounts.get(i);
        }

        return new SMParameters(d, discounts.getdInfinity(), alphabetSize, depth, seed, baseDistribution);
    }

    private Restaurant get(Restaurant r, int[] context, int index, boolean forPrediction) {
        Restaurant child, newChild;
        int overlap, es, el, d;
        boolean leafNode;
        int[] seq;

        seq = sequence.fullSeq();
        d = r.depth();

        if(depth == -1){
            leafNode = index == -1;
        } else {
            leafNode = index == -1 || d == depth;
        }

        //if leaf node then return r
        if (leafNode) {
            return r;
        }

        child = r.get(context[index]);

        //if no children in that direction
        if (child == null) {
            if (forPrediction) {
                return r;
            } else {

                if (depth == -1) {
                    child = new Restaurant(r, 0, index + 1, discounts);
                } else {
                    el = (depth - d < index + 1) ? depth - d : index + 1;
                    child = new Restaurant(r, index - el + 1, el, discounts);
                }

                r.put(context[index], child);
                return child;
            }
        }

        //if child in that direction need to check how much overlap
        es = child.edgeStart();
        el = child.edgeLength();
        overlap = 0;
        while ( overlap < el && index >= overlap && seq[es + el - 1 - overlap] == context[index - overlap]) {
            overlap++;
        }

        assert overlap > 0;

        //if overlap is complete edge down to child then call get on child
        if (overlap == el) {
            if(!forPrediction){
                child.setEdgeStart(index - el + 1);
            }
            return get(child, context, index - el, forPrediction);
        }

        //if overlap is not complete edge down to child
        if(forPrediction){
            newChild = child.fragment(r, es + el - overlap, overlap, forPrediction);
        } else {
            newChild = child.fragment(r, index - overlap + 1, overlap, forPrediction);
            r.put(context[index], newChild);
            newChild.put(seq[es + el - overlap - 1 ], child);
        }

        return get(newChild, context, index - overlap, forPrediction);
    }

    private void sampleSeatingArrangments(Restaurant r){
        for(Restaurant child : r.values()){
            sampleSeatingArrangments(child);
        }
        r.sampleSeatingArrangements();
    }

    private double sampleDiscounts(double stdProposal){
        double logLik, pLogLik, currentValue, proposal;
        boolean accept;

        logLik = score();
        
        for(int dIndex = 0; dIndex < discounts.length(); dIndex++){
            currentValue = discounts.get(dIndex);
            proposal = currentValue + stdProposal * RNG.nextGaussian();
            
            if(proposal > 0.0 && proposal < 1.0){
                discounts.set(dIndex, proposal);
                pLogLik = score();

                accept = RNG.nextDouble() < Math.exp(pLogLik - logLik);
                if(accept){
                    logLik = pLogLik;
                } else {
                    discounts.set(dIndex,currentValue);
                }
            }
        }

        currentValue = discounts.getdInfinity();
        proposal = currentValue + stdProposal * RNG.nextGaussian();

        if(proposal > 0.0 && proposal < 1.0){
            discounts.setDInfinity(proposal);
            pLogLik = score();
            
            accept = RNG.nextDouble() < Math.exp(pLogLik - logLik);
            if(accept){
                logLik = pLogLik;
            } else {
                discounts.setDInfinity(currentValue);
            }
        }

        return logLik;
    }

    private double score(Restaurant r){
        double logLik;

        logLik = 0.0;

        for(Restaurant child : r.values()){
            logLik += score(child);
        }

        return logLik + r.logLik();
    }
}
