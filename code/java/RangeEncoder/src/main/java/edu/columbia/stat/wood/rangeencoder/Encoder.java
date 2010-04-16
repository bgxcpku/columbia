/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.rangeencoder;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author nicholasbartlett
 */

public class Encoder {

    private static long FIRST_BIT_INDICATOR = (long) 1 << 62;
    private static long LONG_MAX_VALUE = Long.MAX_VALUE;

    private long low = 0;
    private long range = LONG_MAX_VALUE;

    private OutputStream out;
    private PredictiveModel pm;

    private int currentByte = 0;
    private int byteIndex = 0;

    //stuff I don't really need to actually do the work, only to check results
    public double logLoss = 0.0;
    public int bitsEmitted = 0;
    public long minRange = LONG_MAX_VALUE;

    public Encoder(PredictiveModel pm, OutputStream out) {
        this.pm = pm;
        this.out = out;
    }

    public void encode(int observation) throws IOException {
        assert(0 <= observation && observation <= 256);

        double[] lu = pm.cumulativeDistributionInterval(observation);
        low += lu[0] * range;
        range *= (lu[1] - lu[0]);

        minRange = range < minRange ? range : minRange;

        while ((low ^ (low + range)) < FIRST_BIT_INDICATOR) {
            bitsEmitted++;

            this.write(low >> 62);

            range = range << 1 ;
            low = low << 1 & LONG_MAX_VALUE;
        }

        logLoss -= pm.continueSequence(observation) / Math.log(2);
    }

    public void emitRange() throws IOException{
        long rangeCode = low + range/2;

        long xOrHigh = rangeCode ^ (low + range);
        long xOrLow = rangeCode ^ low;

        int hLength = Long.toBinaryString(xOrHigh).length();
        int lLength = Long.toBinaryString(xOrLow).length();

        int lowerIndex = hLength < lLength ? hLength : lLength;

        for(int i = 0; i< (63 - lowerIndex); i++){
            this.write(rangeCode>>62);
            rangeCode = rangeCode << 1 & LONG_MAX_VALUE;
        }
    }

    public void write(long bit) throws IOException {
        assert (bit == 0 || bit == 1);

        if(bit == 1){
            currentByte += 1<<(7-byteIndex);
        }
        
        if (byteIndex == 7) {
            out.write(currentByte);
            byteIndex = -1;
            currentByte = 0;
        }

        byteIndex++;
    }

    public void flush() throws IOException{
        out.write(currentByte);
    }

    public void close() throws IOException {
        this.encode(256);
        this.emitRange();
        this.flush();
        out.close();
    }
}
