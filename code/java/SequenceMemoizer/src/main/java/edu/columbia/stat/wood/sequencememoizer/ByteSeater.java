/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.util.Random;

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

    public void seatByteSequence(int[] seq, SeatingStyle ss, Integer depth, Integer maxNumberRest){
        Random seadFixer = new Random(0);
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

            System.out.println("seating style was SIMPLE");
            System.out.println("bits per byte = " + logLoss/seq.length);
            System.out.println("total bytes in file = " + seq.length);
            System.out.println("total restaurants at finish = " + Restaurant.numberRest);

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

            System.out.println("seating style was SIMPLE_BOUNDED_MEMORY");
            System.out.println("bits per byte = " + logLoss/totalObsToBeSat);
            System.out.println("total bytes in file = " + totalObsToBeSat);
            System.out.println("total restaurants at finish = " + Restaurant.numberRest);

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

                System.out.println("seating style was RANDOM_DELETION");
                System.out.println("bits per byte = " + logLoss/seq.length);
                System.out.println("total bytes in file = " + seq.length);
                System.out.println("total restaurants at finish = " + Restaurant.numberRest);
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

                System.out.println("seating style was DISANTLY_USED_DELETION");
                System.out.println("bits per byte = " + logLoss/seq.length);
                System.out.println("total bytes in file = " + seq.length);
                System.out.println("total restaurants at finish = " + Restaurant.numberRest);
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

                System.out.println("seating style was BAYES_FACTOR_DELETION");
                System.out.println("bits per byte = " + logLoss/seq.length);
                System.out.println("total bytes in file = " + seq.length);
                System.out.println("total restaurants at finish = " + Restaurant.numberRest);
            }
        }
        System.out.println();
    }
}
