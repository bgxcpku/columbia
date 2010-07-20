/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public class BaseRestaurant extends Restaurant {
    private int[] customers;
    private DiscreteDistribution baseDistribution;

    public BaseRestaurant(DiscreteDistribution baseDistribution){
        super(null, 0, 0, null);
        customers = new int[baseDistribution.alphabetSize()];
        this.baseDistribution = baseDistribution;
    }

    @Override
    public int depth(){
        return 0;
    }

    @Override
    public double seat(int type) {
        customers[type]++;
        return Math.log(baseDistribution.probability(type));
    }

    @Override
    public double predictiveProbability(int type) {
        return baseDistribution.probability(type);
    }

    @Override
    public double[] predictiveProbability(){
        return baseDistribution.cdf();
    }

    @Override
    public void unseat(int type){
        customers[type]--;
    }

    @Override
    public double logLik() {
        double logLik;

        logLik = 0.0;
        for(int i = 0; i < baseDistribution.alphabetSize(); i++){
            logLik += customers[i] * Math.log(baseDistribution.probability(i));
        }

        return logLik;
    }

    @Override
    public void printState(){
        System.out.println("Uniform Restaurant has " + customers + " customers");
    }

    @Override
    public void setEdgeStart(int edgeStart) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int edgeStart() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int edgeLength() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void decrementEdgeLength(int decrementAmount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTableConfig(int type, int[] config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean seat(double p, int type, int d, int depth, int[] context, int index, MutableDouble returnP, MutableDouble discountMultFactor){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean seatCdf(double[] pArray, int type, int d, int depth, int[] context, int index, MutableDouble discountMultFactor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean seatPointOnCdf(double pointOnCdf, double[] pArray, MutableInteger type, int d, int depth, int[] context, int index, MutableDouble discountMultFactor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Restaurant fragment(Restaurant irParent, int irEdgeStart, int irEdgeLength, boolean forPrediction) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double discount(int depth){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sampleSeatingArrangements(){
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
