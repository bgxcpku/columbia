package edu.columbia.stat.wood.sequencememoizer;

/**
 * Sequence Memoizer class.
 * 
 * @author nicholasbartlett
 * 
 */
public interface SequenceMemoizer {

    /**
     * Limits the number of restaurants the HPYP is allowed to instantiate.  Memory
     * is limited by creating a dependent HPYP through deletion.  Deletion occurs
     * uniformly at random over the leaf nodes.
     *
     * @param maxNumberRestaurants max allowable number of instantiated restaurants in the model
     */
    public void limitMemory(long maxNumberRestaurants);

    /**
     * Incorporates the observation in the model with the assumption that this observation
     * is the next in a continuing sequence. Observations are restricted to the interval [0,alphabetSize).
     *
     * @param observation integer value of observation
     * @return the log probability of the observation in the predictive
     * distribution prior to the incorporation of the observation in the model
     */
    public double continueSequence(int observation);

    /**
     * Incorporates the observation in the model with the assumption that this observation
     * is the next in a continuing sequence. Observations are restricted to the interval [0,alphabetSize).
     * 
     * @param observation integer value of observation
     * @return predictive predictive CDF prior to incorporating the observation into the model
     */
    public double[] continueSequenceCDF(int observation);

    /**
     * Finds the observation on the predictive CDF with the assumption that the next observation
     * is the next in a continuing sequence.  The observation is then incorporated into
     * the model.
     *
     * @param pointOnCdf point on cdf, must be in [0.0,1.0)
     * @return Pair containing type of observation seated and predictive cdf prior to incorporating the
     * type into the model
     */
    public Pair<MutableInteger, double[]> continueSequencePointOnCDF(double pointOnCdf);

    /**
     * Incorporates an array of observations with the assumption that they are the next observations
     * in a continuing sequence.
     *
     * @param observations observations to append
     * @return the summed log probability of each observation prior to
     * the incorporation of the observation in the model
     */
    public double continueSequence(int[] observations);

    /**
     * Generates iid draws from the predictive distribution given the context.
     *
     * @param context sequence of integers specifying context
     * @param numSamples number of iid draws to draw
     * @return integer array of samples from context specific predictive distribution
     */
    public int[] generate(Sequence context, int numSamples);

    /**
     * Draws a single sequence by making independent draws from a sequence of predictive distributions
     * indexed by the contexts that arise during sequential generation.
     *
     * @param initialContext context indexing initial predictive distribution
     * @param sequenceLength number of draws made in sequence
     * @return integer sequence sampled
     */
    public int[] generateSequence(Sequence initialContext, int sequenceLength);

    /**
     * Gets the predictive CDF over the entire alphabet given a specific context.
     *
     * @param context context
     * @return array of predictive probabilities for tokens 0 - (alphabetSize - 1)
     */
    public double[] predictiveProbability(Sequence context);

    /**
     * Gets the predictive probability of a  token in a given context.
     *
     * @param context context
     * @param token token to get predictive probability of
     * @return predicitve probability of token in given context
     */
    public double predictiveProbability(Sequence context, int token);

    /**
     * Scores a sequence given that it starts after a certain context.
     *
     * @param initialContext intial context
     * @param seqeunce seqeunce of observations to score
     * @return log predictive probability of observing seqeunce after context
     */
    public double sequenceProbability(Sequence initialContext, int[] sequence);

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
    public double sample(int numSweeps);

    /**
     * Get the joint log likelihood of the data and model parameters.
     * 
     * @return joint log likelihood of the data and model parameters
     */
    public double score();

    /**
     * Get paramters in a SMParameters object.
     *
     * @return values of parameters of the model in its current state
     */
    public SMParameters getParameters();
}
