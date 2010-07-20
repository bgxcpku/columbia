/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.deplump;

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

    double[] interval;

    //stuff I don't really need to actually do the work, only to check results
    public long minRange = LONG_MAX_VALUE;

    public Encoder(PredictiveModel pm, OutputStream out) {
        this.pm = pm;
        this.out = out;
        interval = new double[2];
    }

    public void encode(int observation) throws IOException {
        pm.cumulativeDistributionInterval(observation, interval);
        low += interval[0] * range;
        range *= (interval[1] - interval[0]);

        minRange = range < minRange ? range : minRange;

        while ((low ^ (low + range)) < FIRST_BIT_INDICATOR) {
            write(low >> 62);

            range = range << 1 ;
            low = low << 1 & LONG_MAX_VALUE;
        }
    }

    public void emitRange() throws IOException{
        long rangeCode;

        rangeCode = low + range/2;

        for(int i = 0 ; i < 63; i++){
            write(rangeCode >> 62);
            rangeCode = rangeCode << 1 & LONG_MAX_VALUE;
        }
    }

    public void write(long bit) throws IOException {
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
        emitRange();
        flush();
        out.close();
    }
}
