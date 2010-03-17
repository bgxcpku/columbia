package edu.columbia.stat.wood.sequencememoizer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nicholasbartlett
 */
public interface SequenceMemoizerImplementation {

 public abstract void constrainMemory(int restaurants);



   /**
    * indicates to the SM that future insertions are in a new sequence, seperate
    * from the old sequence
    * @param
    * @return
    */

   public abstract void newSeq();

   /**
    * appends the observation to the sequence
    * @param observation
    * @return the log probability of the observation in the predictive
    * distribution prior to continuation / insertion
    */
   public abstract double continueSequence(int observation);

   /**
    * appends all observations to the observed sequence in
    * order
    * @param observations
    * @return the summed log probability of all observations in their
predictive
    * distributions prior to continuation / insertion
    */
   public abstract double continueSequence(int[] observations);

   /**
    * generates numSamples iid draws from the predictive distribution
given the context
    * @param context
    * @param numSamples
    * @return integer array of samples from context
    */
   public abstract int[] generate(int[] context, int numSamples);

   /**
    * Generates sequenceLength independent draws from a sequence
    * of predictive distributions indexed by the contexts that arise
during sequential generation starting
    * with the initial context given by initialContext
    *
    * @param initialContext
    * @param sequenceLength
    * @return integer array of sequential samples starting from from
initialContext
    */
   public abstract int[] generateSequence(int[] initialContext, int
sequenceLength);

   /**
    *
    *
    * @param context
    * @param tokens
    * @return
    */
   public abstract double[] predictiveProbability(int[] context, int[] tokens);

   /**
    *
    * @param context
    * @return
    */
   public abstract double[] predictiveProbability(int[] context);


   /**
    *
    * @param context
    * @return
    */
   public abstract double sequenceProbability(int[] context, int[] sequence);

   /**
    *
    * @param context
    * @param token
    * @return
    */
  // public abstract double[] cumulativeDistributionInterval(int[]
//context, int token);

//   public abstract int sampleFromCumulativeDistributionFunction(double pointOnCDF);

   /**
    *
    * @param numSweeps
    */
   public abstract double sample(int numSweeps);

   /**
    *
    * @return
    */
   public abstract double score();

   /**
    *
    * @return
    */
   public abstract String getParameterNames();

   /**
    *
    * @return
    */
   public abstract String getParameterValues();
}