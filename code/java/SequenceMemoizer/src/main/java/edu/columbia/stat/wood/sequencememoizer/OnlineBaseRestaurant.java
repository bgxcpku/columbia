/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
class OnlineBaseRestaurant extends OnlineRestaurant{

    FiniteDiscreteDistribution baseDistribution;
    private int[] customers;

    public OnlineBaseRestaurant(FiniteDiscreteDistribution baseDistribution) {
        super(null, null, null);
        this.baseDistribution = baseDistribution;
        customers = new int[baseDistribution.alphabetSize()];
    }

    @Override
    public int depth(){
        return 0;
    }

    @Override
    public double predictiveProbability(int type) {
        return baseDistribution.probability(type);
    }

    @Override
    public double[] predictiveProbability(){
        return baseDistribution.PDF();
    }
    
    public void seat(int type) {
        customers[type]++;
        //return Math.log(baseDistribution.probability(type));
    }
    
}
