/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 * Specialized restaurant for the very root of the tree.
 *
 * @author nicholasbartlett
 */
public class FiniteAlphabetBaseRestaurant extends FiniteAlphabetRestaurant {
    private int[] customers;
    private FiniteDiscreteDistribution baseDistribution;

    /**
     * Initializes the restaurant with a discrete distribution.
     *
     * @param baseDistribution discrete distribution used as base distribution
     */
    public FiniteAlphabetBaseRestaurant(FiniteDiscreteDistribution baseDistribution){
        super(null, 0, 0, null);
        customers = new int[baseDistribution.alphabetSize()];
        this.baseDistribution = baseDistribution;
    }

    /**
     * Gets depth
     * @return 0 since this method is only called when the depths are added to get
     * the depth of a lower level restaurant
     */
    @Override
    public int depth(){
        return 0;
    }

    /**
     * Seats a customer of the given type in the restaurant. 
     *
     * @param type type to be seated
     * @return the predictive log likelihood of the type prior to insertion in the model
     */
    @Override
    public double seat(int type) {
        customers[type]++;
        return Math.log(baseDistribution.probability(type));
    }

    /**
     * Gets the predictive probability of a given type in this restaurant /
     * conditional distribution.
     *
     * @param type type for requested predictive probability
     * @return predictive probability of type
     */
    @Override
    public double predictiveProbability(int type) {
        return baseDistribution.probability(type);
    }

    /**
     * Gets the predictive CDF at this node.  Predictive probabilities are for
     * types [0,alphabetSize), in order.
     *
     * @return double[] of predictive probabilities
     */
    @Override
    public double[] predictivePDF(){
        return baseDistribution.PDF();
    }

    /**
     * Unseats a given type from the restaurant.  This method is used in sampling.
     *
     * @param type type to useat
     */
    @Override
    public void unseat(int type){
        customers[type]--;
    }

    /**
     * Gets the log likelihood of the seating arrangement in the restaurant.
     *
     * @return log likelihood of seating arrangement;
     */
    @Override
    public double logLik() {
        double logLik;

        logLik = 0.0;
        for(int i = 0; i < baseDistribution.alphabetSize(); i++){
            logLik += customers[i] * Math.log(baseDistribution.probability(i));
        }

        return logLik;
    }

    /**
     * Prints state of restaurant.
     */
    @Override
    public void printState(){
        System.out.println("Uniform Restaurant has " + customers + " customers");
    }

    /**
     * Unsupported.
     *
     * @param edgeStart
     */
    @Override
    public void setEdgeStart(int edgeStart) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unsupported.
     *
     * @param edgeStart
     */
    @Override
    public int edgeStart() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unsupported.
     *
     * @param edgeStart
     */
    @Override
    public int edgeLength() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unsupported.
     *
     * @param edgeStart
     */
    @Override
    public void decrementEdgeLength(int decrementAmount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unsupported.
     *
     * @param edgeStart
     */
    @Override
    public void setTableConfig(int type, int[] config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unsupported.
     *
     * @param edgeStart
     */
    @Override
    public boolean seat(double p, int type, int d, int depth, int[] context, int index, MutableDouble returnP, MutableDouble discountMultFactor){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unsupported.
     *
     * @param edgeStart
     */
    @Override
    public boolean seatCDF(double[] pArray, int type, int d, int depth, int[] context, int index, MutableDouble discountMultFactor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unsupported.
     *
     * @param edgeStart
     */
    @Override
    public boolean seatPointOnCDF(double pointOnCdf, double[] pArray, MutableInteger type, int d, int depth, int[] context, int index, MutableDouble discountMultFactor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unsupported.
     *
     * @param edgeStart
     */
    @Override
    public FiniteAlphabetRestaurant fragment(FiniteAlphabetRestaurant irParent, int irEdgeStart, int irEdgeLength, boolean forPrediction) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unsupported.
     *
     * @param edgeStart
     */
    @Override
    public double discount(int depth){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unsupported.
     *
     * @param edgeStart
     */
    @Override
    public void sampleSeatingArrangements(){
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
