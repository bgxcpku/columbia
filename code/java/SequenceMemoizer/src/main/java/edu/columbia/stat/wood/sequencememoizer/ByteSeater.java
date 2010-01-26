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
    public int maxRunLength;

    public ByteSeater(long seed, int maxRunLength) {
        utils = new Utils(seed);
        this.maxRunLength = maxRunLength;
    }

    //returns a double array with the entries:
    //0: bits/byte
    //1: total bytes
    //2: total restaurants in tree
    public double[] seatByteSequence(int[] seq, SeatingStyle ss, Integer depth, Integer maxNumberRest) {

        double[] returnVal = new double[3];
        returnVal[1] = seq.length;
        logLoss = 0.0;
        int currentRunLength = 0;
        //maxRunLength = 20;
        RunLengthEncoder rle = new RunLengthEncoder();

        int counter = 0;
        StochasticMemoizer sm;
        if (ss != SeatingStyle.SIMPLE_BOUNDED_MEMORY) {
            sm = new StochasticMemoizer(256, depth);
            sm.sequence = seq;
            int obs = 0;
            while (obs < seq.length) {
                if (obs > 0) {
                    if (seq[obs] == seq[obs - 1]) {
                        currentRunLength++;
                    } else {
                        currentRunLength = 0;
                    }
                }

                if (ss != SeatingStyle.SIMPLE) {
                    if (Restaurant.numberRest > maxNumberRest - 2) {
                        if (ss == SeatingStyle.RANDOM_DELETION) {
                            sm.deleteRandomLeafNodes(100);
                        } else if (ss == SeatingStyle.DISANTLY_USED_DELETION) {
                            sm.fillLeastUsedLeafNodeList(maxNumberRest);
                            sm.deleteLeastUsedLeafRestaurants(100);
                            sm.leastUsedLeafNodeList.clear();
                        } else if (ss == SeatingStyle.BAYES_FACTOR_DELETION) {
                            sm.deleteLeastUsefullRestaurantsForLogProbOfData(100);
                        }
                    }
                }

                //update us on the status every 100,000 obs seated
                if ((obs - counter) >= 100000) {
                    System.out.println(obs);
                    System.out.println("number of rest = " + Restaurant.numberRest);
                    counter = obs;
                }
                
                if (currentRunLength > maxRunLength && maxRunLength > 0) {
                    System.out.println("in run pred prob is = " +Math.exp(sm.obsLogProb));
                    System.out.println("which gives something better than: " + sm.obsLogProb/Math.log(2));
                    int[] rleOut = rle.encode(obs, seq);
                    for (int i = 0; i < rleOut.length; i++) {
                        long obsUpdate = (long) rleOut[i] - (long) Integer.MIN_VALUE;
                        obs += (int) obsUpdate;
                        logLoss += 32;
                        System.out.println("bits per byte from rle = " + 32.0/obsUpdate);
                        System.out.println("total bytes in run = " + obsUpdate);
                    }
                } else {
                    sm.seatObs(sm.contextFreeRestaurant, obs, obs - 1, seq, 1.0 / 256);
                    logLoss -= sm.obsLogProb / Math.log(2);
                    sm.discounts.stepGradient(0.0001, Math.exp(sm.obsLogProb));
                    obs++;
                }
            }

        } else if (ss == SeatingStyle.SIMPLE_BOUNDED_MEMORY) {
            sm = new StochasticMemoizer(256, depth);
            sm.sequence = seq;

            int index = 0;
            int obs = 0;

            while (index < seq.length) {
                if (index > 0) {
                    if (seq[index] == seq[index - 1]) {
                        currentRunLength++;
                    } else {
                        currentRunLength = 0;
                    }
                }

                if((index - counter) >= 100000){
                    System.out.println(index);
                    System.out.println("number of rest = " + Restaurant.numberRest);
                    counter = index;
                }
                
                if (Restaurant.numberRest > maxNumberRest - 2) {
                    int[] newSeq = new int[sm.sequence.length - obs];
                    System.arraycopy(sm.sequence, obs, newSeq, 0, sm.sequence.length - obs);

                    sm = new StochasticMemoizer(256, depth);
                    sm.sequence = newSeq;
                    obs = 0;
                }

                if (currentRunLength > maxRunLength && maxRunLength > 0) {
                    int[] rleOut = rle.encode(index, seq);
                    for (int i = 0; i < rleOut.length; i++) {
                        long obsUpdate = (long) rleOut[i] - (long) Integer.MIN_VALUE;
                        index += (int) obsUpdate;
                        obs += (int) obsUpdate;
                        logLoss += 32;
                    }
                } else {
                    sm.seatObs(sm.contextFreeRestaurant, obs, obs - 1, sm.sequence, 1.0 / 256);
                    logLoss -= sm.obsLogProb / Math.log(2);
                    sm.discounts.stepGradient(0.0001, Math.exp(sm.obsLogProb));
                    obs++;
                    index++;
                }
            }
        }

        returnVal[0] = logLoss / seq.length;
        returnVal[2] = Restaurant.numberRest;
        return returnVal;
    }
}
