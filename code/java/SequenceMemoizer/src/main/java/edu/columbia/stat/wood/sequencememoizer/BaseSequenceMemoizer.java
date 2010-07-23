/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

/**
 * Basic implementation of some of the Sequence Memoizer methods.
 *
 * @author nicholasbartlett
 */

public abstract class BaseSequenceMemoizer implements SequenceMemoizer {

    /**
     * Incorporates an array of observations with the assumption that they are the next observations
     * in a continuing sequence.
     *
     * @param observations observations to append
     * @return the summed log probability of each observation prior to
     * the incorporation of the observation in the model
     */
    public double continueSequence(int[] observations) {
        double logLik;

        logLik = 0.0;

        for (int obs : observations) {
            logLik += continueSequence(obs);
        }

        return logLik;
    }

    /**
     * Draws a single sequence by making independent draws from a sequence of predictive distributions
     * indexed by the contexts that arise during sequential generation.
     *
     * @param initialContext context indexing initial predictive distribution
     * @param sequenceLength number of draws made in sequence
     * @return integer sequence sampled
     */
    public int[] generateSequence(Sequence initialContext, int sequenceLength){
        int[] generatedSequence;
        int generatedToken;
        Sequence context;

        generatedSequence = new int[sequenceLength];
        context = initialContext;

        for(int i = 0; i < sequenceLength; i++){
            generatedToken = generate(context, 1)[0];
            generatedSequence[i] = generatedToken;
            context.add(generatedToken);
        }

        return generatedSequence;
    }
}
