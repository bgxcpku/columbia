/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer.v1;

import edu.columbia.stat.wood.util.ByteDiscreteDistribution;

/**
 * Interface which a byte based sequence memoizer should implement.
 * @author nicholasbartlett
 */

public interface ByteSequenceMemoizerInterface {

    /**
     * Indicates to the model that you wish to start a new sequence.  The first
     * time continueSequence is used after this the assumption is that the type
     * is being seen with empty context.
     */
    public void newSequence();

    /**
     * Continue the current sequence by incorporating the new observation.
     * @param observation new observation
     * @return log predictive probability of observation prior to incorporating the observation into the model
     */
    public double continueSequence(byte observation);

    /**
     * Continue the sequence with the sequence of observations supplied.  This is
     * the same as looping over the method call continueSequence for each observation.
     * @param observations
     * @return the sum of the log predictive probabilities of each observation prior to incorporating it into the model
     */
    public double continueSequence(byte[] observations);

    /**
     * Generate some number of samples given a specified context.
     * @param context context, arranged most distant to the left, most recent to the right
     * @param numSamples number of samples desired
     * @return samples from the predictive distribution in the specified context
     */
    public byte[] generate(byte[] context, int numSamples);

    /**
     * Generate a sequence from the predictive model, starting with the specified context.
     * @param context starting context
     * @param sequenceLength length of sequence
     * @return sampled sequence of specified length
     */
    public byte[] generateSequence(byte[] context, int sequenceLength);

    /**
     * Get the predictive distribution in a specified context.
     * @param context
     * @return discrete distribution over byte types
     */
    public ByteDiscreteDistribution predictiveDistribution(byte[] context);

    /**
     * Get the predictive probability of a given type in a given context.
     * @param context
     * @param token
     * @return probability of token in the specified context
     */
    public double predictiveProbability(byte[] context, byte token);

    /**
     * Gets the log probability of a sequence given the current state of the model.
     * @param context context prior to the start of the sequence to score
     * @param sequence
     * @return log probability of sequence in specified context.
     */
    public double sequenceProbability(byte[] context , byte[] sequence);

    /**
     * Sample the parameters in the model.  The seating arrangements are sampled
     * using Gibbs sampling while the discount parameters use a Gibbs-Metropolis step.
     * @param numSweeps number of sweeps for the Gibbs sampler
     * @return joint log probability of model and data
     */
    public double sample(int numSweeps);

    /**
     * Sample only the seating arrangements.
     * @param numSweeps
     */
    public void sampleSeatingArrangements(int numSweeps);

    /**
     * Sample only the discounts.
     * @param numSweeps
     * @return joint log probability of model and data
     */
    public double sampleDiscounts(int numSweeps);

    /**
     * Get the joint log probability of model and data.
     * @return joint log probability of model and data
     */
    public double score();

    /**
     * Get the parameters of this model.
     * @return parameters of this model
     */
    public ByteSequenceMemoizerParameters getParameters();
}
