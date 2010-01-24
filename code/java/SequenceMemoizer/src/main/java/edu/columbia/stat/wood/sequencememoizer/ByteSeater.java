/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public class ByteSeater {
    public double logLoss = 0.0;
    public static Utils utils = new Utils(123);

    public ByteSeater(long seed){
        utils = new Utils(seed);
    }

    //returns a double array with the entries:
    //0: bits/byte
    //1: total bytes
    //2: total restaurants in tree
    public double[] seatByteSequence(int[] seq, SeatingStyle ss, Integer depth, Integer maxNumberRest){

        double[] returnVal = new double[3];
        returnVal[1] = seq.length;
        logLoss = 0.0;

        int counter = 0;
        int index = 0;
        StochasticMemoizer sm;
        if(ss == SeatingStyle.SIMPLE){
            sm = new StochasticMemoizer(256,depth);
            sm.sequence = seq;
            for(int obs = 0; obs<seq.length; obs++){
                if(counter++>=100000){
                    System.out.println(index);
                    counter = 0;
                }
                index++;
                sm.seatObs(sm.contextFreeRestaurant, obs, obs - 1, seq, 1.0 / 256);
                logLoss -= sm.obsLogProb / Math.log(2);
                sm.discounts.stepGradient(0.0001, Math.exp(sm.obsLogProb));
            }

        } else if (ss == SeatingStyle.SIMPLE_BOUNDED_MEMORY){
            sm = new StochasticMemoizer(256,depth);
            sm.sequence = seq;
           
            index = 0;
            int obs =0;
            int totalObsToBeSat = seq.length;

            while(index < totalObsToBeSat){
                if(counter++>=100000){
                    System.out.println(index);
                    counter = 0;
                }

                if(Restaurant.numberRest > maxNumberRest -2){
                    int[] newSeq = new int[sm.sequence.length - obs];
                    System.arraycopy(sm.sequence, obs, newSeq, 0, sm.sequence.length - obs);
                    
                    sm = new StochasticMemoizer(256,depth);
                    sm.sequence = newSeq;
                    obs = 0;
                }

                sm.seatObs(sm.contextFreeRestaurant, obs, obs - 1, seq, 1.0 / 256);
                logLoss -= sm.obsLogProb / Math.log(2);
                sm.discounts.stepGradient(0.0001, Math.exp(sm.obsLogProb));
                obs++;
                index++;
            }

        } else if(ss == SeatingStyle.RANDOM_DELETION){
            sm = new StochasticMemoizer(256,depth);
            sm.sequence = seq;
            for(int obs = 0; obs<seq.length; obs++){
                if (counter++>=100000){
                    System.out.println(index);
                    counter = 0;
                }

                if (Restaurant.numberRest > maxNumberRest -2) {
                    sm.deleteRandomLeafNodes(100);
                }

                index++;
                sm.seatObs(sm.contextFreeRestaurant, obs, obs - 1, seq, 1.0 / 256);
                logLoss -= sm.obsLogProb / Math.log(2);
                sm.discounts.stepGradient(0.0001, Math.exp(sm.obsLogProb));
            }

        } else if(ss == SeatingStyle.DISANTLY_USED_DELETION){
            sm = new StochasticMemoizer(256,depth);
            sm.sequence = seq;
            for(int obs = 0; obs<seq.length; obs++){
                if (counter++>=100000){
                    System.out.println(index);
                    counter = 0;
                }

                if (Restaurant.numberRest > maxNumberRest -2) {
                    sm.fillLeastUsedLeafNodeList(maxNumberRest);
                    sm.deleteLeastUsedLeafRestaurants(100);
                    sm.leastUsedLeafNodeList.clear();
                }

                index++;
                sm.seatObs(sm.contextFreeRestaurant, obs, obs - 1, seq, 1.0 / 256);
                logLoss -= sm.obsLogProb / Math.log(2);
                sm.discounts.stepGradient(0.0001, Math.exp(sm.obsLogProb));
            }

        } else if(ss == SeatingStyle.BAYES_FACTOR_DELETION){
            sm = new StochasticMemoizer(256,depth);
            sm.sequence = seq;
            for(int obs = 0; obs<seq.length; obs++){
                if (counter++>=100000){
                    System.out.println(index);
                    counter = 0;
                }

                if (Restaurant.numberRest > maxNumberRest -2) {
                    sm.deleteLeastUsefullRestaurantsForLogProbOfData(100);
                }

                index++;
                sm.seatObs(sm.contextFreeRestaurant, obs, obs - 1, seq, 1.0 / 256);
                logLoss -= sm.obsLogProb / Math.log(2);
                sm.discounts.stepGradient(0.0001, Math.exp(sm.obsLogProb));
            }
        }
        
        returnVal[0] = logLoss/seq.length;
        returnVal[2] = Restaurant.numberRest;
        return returnVal;
    }
}
