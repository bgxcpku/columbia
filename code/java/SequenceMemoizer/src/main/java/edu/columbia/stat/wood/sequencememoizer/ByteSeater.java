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
    public double[] seatSequence(int[] seq, SMTree sm) {
        double[] returnVal = new double[3];
        returnVal[1] = seq.length;
        double logLoss = 0.0;
        int currentRunLength = 0;
        RunLengthEncoder rle = new RunLengthEncoder();

        sm.seatObs(seq[0], false, true);
        int intermediateSample = 1;
        int i = 1;
        while(i < seq.length){

            if (intermediateSample >= 100000) {
                System.out.println(i);
                intermediateSample = 0;
            }
            intermediateSample++;

            currentRunLength = (seq[i] == seq[i - 1])?(currentRunLength+1):0;

            if (currentRunLength > maxRunLength && maxRunLength > 0) {
                int[] rleOut = rle.encode(i, seq);
                for (int j = 0; j < rleOut.length; j++) {
                    long obsUpdate = (long) rleOut[j] - (long) Integer.MIN_VALUE;
                    i += (int) obsUpdate;
                    logLoss += 32;
                }
            } else {
                logLoss += sm.seatObs(seq[i], false, true);
                i++;
            }
        }
        
        returnVal[0] = logLoss / seq.length;
        returnVal[2] = Restaurant.numberRest;
        return returnVal;
    }
}
