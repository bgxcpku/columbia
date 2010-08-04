/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import java.util.Random;

/**
 * Sequence memoizer model with sampling functionality.
 * 
 * @author nicholasbartlett 
 */

public class SamplingSequenceMemoizer extends BaseSequenceMemoizer {

    /**
     * Random object used for random number generation throughout the model.
     */
    public static Random RNG;
    
    private int alphabetSize, depth;
    private long seed;
    private SamplingRestaurant emptyContextRestaurant;
    private SamplingBaseRestaurant baseRestaurant;
    private Sequence sequence;
    private Discounts discounts;
    private DiscreteDistribution baseDistribution;

    /**
     * Initializes the object based on the specified parameters.
     *
     * @param params
     */
    public SamplingSequenceMemoizer(SMParameters params){
       alphabetSize = params.alphabetSize;
       depth = params.depth;
       seed = params.seed;
       discounts = new Discounts(params.discounts, params.infiniteDiscount);
       baseDistribution = params.baseDistribution;

       RNG = new Random(seed);
       sequence = new Sequence();
       baseRestaurant = new SamplingBaseRestaurant(baseDistribution);
       emptyContextRestaurant = new SamplingRestaurant(baseRestaurant, 0,0, discounts);
    }

    /**
     * Unsupported.
     *
     * @param maxNumberRestaurants
     */
    public void limitMemory(long maxNumberRestaurants){
        throw new UnsupportedOperationException("Not supported in this implementing class.");
    }

    /**
     * Incorporates the observation in the model with the assumption that this observation
     * is the next in a continuing sequence. Observations are restricted to the interval [0,alphabetSize).
     *
     * @param observation integer value of observation
     * @return the log probability of the observation in the predictive
     * distribution prior to the incorporation of the observation in the model
     */
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

    /**
     * Incorporates the observation in the model with the assumption that this observation
     * is the next in a continuing sequence. Observations are restricted to the interval [0,alphabetSize).
     *
     * @param observation integer value of observation
     * @return predictive predictive CDF prior to incorporating the observation into the model
     */
    public double[] continueSequenceCDF(int observation) {
        if (observation < 0 || observation >= alphabetSize) {
            throw new IllegalArgumentException("Observations must be integers in the interval [0,alphabetSize).");
        }

        double[] cdf;

        cdf = baseRestaurant.predictiveProbability();

        if(emptyContextRestaurant.seatCDF(cdf, observation, 0, depth, sequence.fullSeq(),sequence.length() - 1, new MutableDouble(1.0))) {
            baseRestaurant.seat(observation);
        }

        sequence.add(observation);
        discounts.stepDiscounts(0.0001, cdf[observation]);

        return cdf;
    }

    /**
     * Finds the observation on the predictive CDF with the assumption that the next observation
     * is the next in a continuing sequence.  The observation is then incorporated into
     * the model.
     *
     * @param pointOnCdf point on cdf, must be in [0.0,1.0)
     * @return Pair containing type of observation seated and predictive cdf prior to incorporating the
     * type into the model
     */
    public Pair<MutableInteger, double[]> continueSequencePointOnCDF(double pointOnCdf) {
        if (pointOnCdf < 0.0 || pointOnCdf >= 1.0) {
            throw new IllegalArgumentException("Point on CDF must be a double in the interval [0,1).");
        }

        MutableInteger type;
        double[] cdf;

        type = new MutableInteger(-1);
        cdf = baseRestaurant.predictiveProbability();

        if(emptyContextRestaurant.seatPointOnCDF(pointOnCdf, cdf, type, 0, depth, sequence.fullSeq(), sequence.length()-1, new MutableDouble(1.0))){
            baseRestaurant.seat(type.intVal());
        }

        sequence.add(type.intVal());
        discounts.stepDiscounts(0.0001, cdf[type.intVal()]);

        return new Pair(type, cdf);
    }

    /**
     * Generates iid draws from the predictive distribution given the context.
     *
     * @param context sequence of integers specifying context
     * @param numSamples number of iid draws to draw
     * @return integer array of samples from context specific predictive distribution
     */
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

    /**
     * Gets the predictive CDF over the entire alphabet given a specific context.
     *
     * @param context context
     * @return array of predictive probabilities for tokens 0 - (alphabetSize - 1)
     */
    public double[] predictiveProbability(Sequence context) {
        return get(emptyContextRestaurant, context.fullSeq(), context.length()-1, true).predictiveProbability();
    }

    /**
     * Gets the predictive probability of a  token in a given context.
     *
     * @param context context
     * @param token token to get predictive probability of
     * @return predicitve probability of token in given context
     */
    public double predictiveProbability(Sequence context, int token){
        return get(emptyContextRestaurant, context.fullSeq(), context.length()-1, true).predictiveProbability(token);
    }

    /**
     * Scores a sequence given that it starts after a certain context.
     *
     * @param initialContext intial context
     * @param seqeunce seqeunce of observations to score
     * @return log predictive probability of observing seqeunce after context
     */
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

    /**
     * Do Gibbs sampling of the parameters of the model. Seating
     * arrangements are sampled according to the exact conditional distributions
     * while discount parameters are sampled using a Metropolis step with a
     * normal jumping distribution.  Independent flat priors are imposed on the
     * individual discount parameters.
     *
     * @param numSweeps number of passes to make Gibbs sampling
     * @return joint log likelihood of the data and model parameters
     */
    public double sample(int numSweeps) {
        double logLik;

        logLik = 0.0;

        for(int i = 0; i < numSweeps; i++){
            sampleSeatingArrangments(emptyContextRestaurant);
            logLik = sampleDiscounts(.07);
        }
        
        return logLik;
    }

    /**
     * Get the joint log likelihood of the data and model parameters.
     *
     * @return joint log likelihood of the data and model parameters
     */
    public double score() {
        return score(emptyContextRestaurant);
    }

    /**
     * Get paramters in a SMParameters object.
     *
     * @return values of parameters of the model in its current state
     */
    public SMParameters getParameters() {
        double[] d;

        d = new double[discounts.length()];
        for(int i = 0; i < discounts.length(); i++){
            d[i] = discounts.get(i);
        }

        return new SMParameters(d, discounts.getdInfinity(), alphabetSize, depth, seed, baseDistribution);
    }

    private SamplingRestaurant get(SamplingRestaurant r, int[] context, int index, boolean forPrediction) {
        SamplingRestaurant child, newChild;
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
                    child = new SamplingRestaurant(r, 0, index + 1, discounts);
                } else {
                    el = (depth - d < index + 1) ? depth - d : index + 1;
                    child = new SamplingRestaurant(r, index - el + 1, el, discounts);
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

    private void sampleSeatingArrangments(SamplingRestaurant r){
        for(SamplingRestaurant child : r.values()){
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

    private double score(SamplingRestaurant r){
        double logLik;

        logLik = 0.0;

        for(SamplingRestaurant child : r.values()){
            logLik += score(child);
        }

        return logLik + r.logLik();
    }
}
