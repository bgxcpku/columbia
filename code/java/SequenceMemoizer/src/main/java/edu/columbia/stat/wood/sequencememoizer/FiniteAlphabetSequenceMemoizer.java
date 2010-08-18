/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import edu.columbia.stat.wood.util.MersenneTwisterFast;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Sequence memoizer model for finite alphabets.
 * 
 * @author nicholasbartlett 
 */

public class FiniteAlphabetSequenceMemoizer {

    /**
     * Random object used for random number generation throughout the model.
     */
    public static MersenneTwisterFast RNG;

    /**
     * A number used to provide a lower bound on the predictive probabilities
     * obtained by the model.
     */
    public static double MIN_SYMBOL_PROB = 5.01 / (double) (Integer.MAX_VALUE);
    
    private int alphabetSize, depth;
    private FiniteAlphabetBaseRestaurant baseRestaurant;
    private FiniteAlphabetRestaurant emptyContextRestaurant;
    private Sequence sequence;
    private Discounts discounts;
    
    private long seed;
    private FiniteDiscreteDistribution baseDistribution;
    
    /**
     * Initializes the object based on the specified parameters.
     *
     * @param params
     */
    public FiniteAlphabetSequenceMemoizer(FiniteAlphabetSequenceMemoizerParameters params){
       depth = params.depth;
       seed = params.seed;
       discounts = new Discounts(params.discounts, params.infiniteDiscount);
       baseDistribution = params.baseDistribution;
       alphabetSize = params.baseDistribution.alphabetSize();

       RNG = new MersenneTwisterFast(seed);
       sequence = new Sequence();
       baseRestaurant = new FiniteAlphabetBaseRestaurant(params.baseDistribution);
       emptyContextRestaurant = new FiniteAlphabetRestaurant(baseRestaurant, 0,0, discounts);
    }

    /**
     * Initializes the object with a specified alphabet size.
     *
     * @param alphabetSize
     */
    public FiniteAlphabetSequenceMemoizer(int alphabetSize){
        this(new FiniteAlphabetSequenceMemoizerParameters(alphabetSize));
    }

    /**
     * Initializes the object with default parameters.
     */
    public FiniteAlphabetSequenceMemoizer(){
        this(new FiniteAlphabetSequenceMemoizerParameters());
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
     * is the next in a continuing sequence.
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
     * is the next in a continuing sequence.
     *
     * @param observation integer value of observation
     * @param range container object for values of CDF(observation-1) and CDF(observation)
     */
    public void continueSequenceRange(int observation, Range r) {
        if (observation < 0 || observation >= alphabetSize) {
            throw new IllegalArgumentException("Observations must be integers in the interval [0,alphabetSize).");
        }

        double[] pdf;
        double eofAdjustment, cuSum;

        pdf = baseRestaurant.predictivePDF();

        if(emptyContextRestaurant.seatPDF(pdf, observation, 0, depth, sequence.fullSeq(),sequence.length() - 1, new MutableDouble(1.0))) {
            baseRestaurant.seat(observation);
        }

        sequence.add(observation);
        discounts.stepDiscounts(0.0001, pdf[observation]);

        eofAdjustment =  1.0 + MIN_SYMBOL_PROB * (double) alphabetSize ;

        cuSum = 0.0;
        for(int i = 0; i < observation; i++){
            cuSum += (pdf[i] + MIN_SYMBOL_PROB) / eofAdjustment;
        }

        r.set(cuSum, cuSum + (pdf[observation] + MIN_SYMBOL_PROB) / eofAdjustment);
    }

    /**
     * Finds the observation on the predictive CDF such that CDF(observation) greater than pointOnCDF
     * and CDF(observation - 1) less than or equal to pointOnCDF. The predictive CDF is calculated based on the
     * assumption that the observation is the next in a continuing sequence. The observation is then incorporated into
     * the model.
     *
     * @param pointOnCdf point on cdf, must be in [0.0,1.0)
     * @param rad container object for observation type, CDF(observation-1), and CDF(observation)
     */
    public void continueSequenceRangeAndDecode(double pointOnCdf, RangeAndDecode rad) {
        if (pointOnCdf < 0.0 || pointOnCdf >= 1.0) {
            throw new IllegalArgumentException("Point on CDF must be a double in the interval [0,1).");
        }

        MutableInteger type;
        double[] pdf;
        double eofAdjustment, cuSum;

        type = new MutableInteger(-1);
        pdf = baseRestaurant.predictivePDF();

        if(emptyContextRestaurant.seatPointOnCDF(pointOnCdf, pdf, type, 0, depth, sequence.fullSeq(), sequence.length()-1, new MutableDouble(1.0))){
            baseRestaurant.seat(type.intVal());
        }

        sequence.add(type.intVal());
        discounts.stepDiscounts(0.0001, pdf[type.intVal()]);

        eofAdjustment =  1.0 + MIN_SYMBOL_PROB * (double) alphabetSize ;

        cuSum = 0.0;
        for(int i = 0; i < type.intVal(); i++){
            cuSum += (pdf[i] + MIN_SYMBOL_PROB) / eofAdjustment;
        }

        rad.set(type.intVal(), cuSum, cuSum + (pdf[type.intVal()] + MIN_SYMBOL_PROB) / eofAdjustment);
    }

    /**
     * Generates iid draws from the predictive distribution given the context.
     *
     * @param context sequence of integers specifying context
     * @param numSamples number of iid draws to draw
     * @return integer array of samples from context specific predictive distribution
     */
    public int[] generate(Sequence context, int numSamples) {
        FiniteDiscretePDFIterator iterator;
        Pair<Integer, Double> pdfPair;
        int[] samples;
        double r, cuSum;

        samples = new int[numSamples];
        iterator = predictivePDF(context);

        for(int i = 0; i < numSamples; i++){
            r = RNG.nextDouble();
            cuSum = 0.0;
            iterator.reset();
            while(iterator.hasNext()){
                pdfPair = iterator.next();
                cuSum += pdfPair.second();
                if(cuSum > r){
                    samples[i] = pdfPair.first();
                    break;
                }
                assert cuSum < 1.0;
            }
        }

        return samples;
    }

    /**
     * Gets an iterator object to return the type, probability pairs which define
     * the predictive PDF in the specified context.
     *
     * @param context context
     * @return iterator object to return type, probability pairs of the predictive PDF
     */
    public FiniteDiscretePDFIterator predictivePDF(Sequence context) {
        return new FiniteDiscretePDFIterator(get(emptyContextRestaurant, context.fullSeq(), context.length()-1, true).predictivePDF());
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
     * Get paramters.
     *
     * @return values of parameters of the model in its current state
     */
    public SequenceMemoizerParameters getParameters() {
        double[] d;

        d = new double[discounts.length()];
        for(int i = 0; i < discounts.length(); i++){
            d[i] = discounts.get(i);
        }

        return new FiniteAlphabetSequenceMemoizerParameters(d, discounts.getdInfinity(), depth, seed, baseDistribution);
    }

    private FiniteAlphabetRestaurant get(FiniteAlphabetRestaurant r, int[] context, int index, boolean forPrediction) {
        FiniteAlphabetRestaurant child, newChild;
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
                    child = new FiniteAlphabetRestaurant(r, 0, index + 1, discounts);
                } else {
                    el = (depth - d < index + 1) ? depth - d : index + 1;
                    child = new FiniteAlphabetRestaurant(r, index - el + 1, el, discounts);
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

    private void sampleSeatingArrangments(FiniteAlphabetRestaurant r){
        for(FiniteAlphabetRestaurant child : r.values()){
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

    private double score(FiniteAlphabetRestaurant r){
        double logLik;

        logLik = 0.0;

        for(FiniteAlphabetRestaurant child : r.values()){
            logLik += score(child);
        }

        return logLik + r.logLik();
    }

    public static void main(String[] args) throws FileNotFoundException, IOException{
        FiniteAlphabetSequenceMemoizer sm;
        sm = new FiniteAlphabetSequenceMemoizer(new FiniteAlphabetSequenceMemoizerParameters(257, 15, 1));

        BufferedInputStream bis = null;
        File f, g;

        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/pride_and_prejudice.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/AliceInWonderland.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/nyt/lmdata-nyt.1-10000");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/wikipedia/enwik8");
        f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/calgary_corpus/geo");

        double logLik = 0.0;

        try{
            
            bis = new BufferedInputStream(new FileInputStream(f));

            int b, ind;
            ind = 0;
            while((b = bis.read()) > -1 ) {
                if(ind++ % 100000 == 0){
                    System.out.println(ind++-1);
                }
                logLik += sm.continueSequence(b);
            }
        } finally {
            if (bis != null){
                bis.close();
            }
        }

        System.out.println();
        
        System.out.println(-logLik / Math.log(2) / f.length());
        System.out.println(FiniteAlphabetRestaurant.count);
    }
}
