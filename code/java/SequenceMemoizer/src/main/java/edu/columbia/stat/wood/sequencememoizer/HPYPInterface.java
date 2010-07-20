package edu.columbia.stat.wood.sequencememoizer;


/**
 *
 * @author nicholasbartlett
 * 
 */
public interface HPYPInterface {

    /**
     * Limits the number of restaurants the HPYP is allowed to instantiate.  Memory
     * is limited by creating a dependent HPYP through deletion.  Deletion occurs
     * uniformly at random over the leaf nodes.
     *
     * @param maxNumberRestaurants max allowable number of instantiated restaurants in the model
     */
    public void limitMemory(long maxNumberRestaurants);

    /**
     * Appends the observation to the sequence and adds the observation to the model.
     * Observations are restricted to the interval [0,alphabetSize).
     *
     * @param observation integer value of observation
     * @return the log probability of the observation in the predictive
     * distribution prior to continuation / insertion
     */
    public double continueSequence(int observation);

    /**
     * Appends the observation to the sequence and adds the observation to the model.
     * Observations are restricted to the interval [0,alphabetSize).  This
     * is the method needed for compression.
     *
     * @param observation integer value of observation
     * @return predictive cdf given the context of the observation
     */
    public double[] continueSequenceCdf(int observation);

    /**
     * Finds the observation on the predictive cdf given the current context and
     * seats the observation.  This is the method needed for decompression.
     *
     * @param pointOnCdf point on cdf
     * @return Pair containing type of observation seated and predictive cdf
     */
    public Pair<MutableInteger, double[]> continueSequencePointOnCdf(double pointOnCdf);

    /**
     * Appends an array of observations to the observed sequence in order.
     *
     * @param observations observations to append
     * @return the summed log probability of all observations prior to 
     * continuation / insertion of each
     */
    public double continueSequence(int[] observations);

    /**
     * Generates iid draws from the predictive distribution given the context.
     *
     * @param context array of integers specifying context
     * @param numSamples number of iid draws
     * @return integer array of samples from context
     */
    public int[] generate(Sequence context, int numSamples);

    /**
     * Generates independent draws from a sequence of predictive distributions
     * indexed by the contexts that arise during sequential generation.
     *
     * @param initialContext context indexing initial predictive distribution
     * @param sequenceLength number of draws made in sequence
     * @return integer array of sequential samples
     */
    public int[] generateSequence(Sequence initialContext, int sequenceLength);

    /**
     * Gets the predictive CDF over the entire alphabet in a given context.
     *
     * @param context predictive probability context
     * @param index index of last element of context
     * @return array of predictive values for tokens 0 - (alphabetSize - 1)
     */
    public double[] predictiveProbability(Sequence context);

    /**
     * Gets the predictive probability of a  token in a given context.
     *
     * @param context predictive probability context
     * @param index index of last element of context
     * @param token token to get predictive probability of
     * @return array of predictive values ordered according to tokens
     */
    public double predictiveProbability(Sequence context, int token);

    /**
     * Scores a sequence given that it starts after a certain context.
     *
     * @param initialContext context of sequence
     * @param index index of last element of context
     * @param seqeunce seqeunce of observations to score
     * @return log predictive probability of observing seqeunce after context
     */
    public double sequenceProbability(Sequence initialContext, int[] sequence);

    /**
     * Do Gibbs sampling of the parameters of the model.  Seating
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
    public ParametersAbstract getParameters();
}
