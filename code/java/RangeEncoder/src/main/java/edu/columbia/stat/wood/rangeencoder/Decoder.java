/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.rangeencoder;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */

public class Decoder {

    private static long FIRST_BIT_INDICATOR = (long) 1 << 62;
    private static long LONG_MAX_VALUE = Long.MAX_VALUE;

    private long low = 0;
    private long range = LONG_MAX_VALUE;
    private long code;

    private BufferedBitInputStream bbis;
    private PredictiveModel pm;

    public Decoder(PredictiveModel pm, File inFile) throws IOException{
        this.pm = pm;
        this.bbis = new BufferedBitInputStream(inFile);
        this.initializeCode();
    }

    private void initializeCode() throws IOException{
        code = 0;
        int bit;
        for(int i = 62;i>=0; i--){
            bit = bbis.read();
            if(bit == 1){
                code += (long) 1<<i;
            }
        }
    }

    public int read() throws IOException{
        double pointOnCDF = (double) (code - low)/range;

        int decodedByte = pm.inverseCDF(pointOnCDF);

        //if decoded byte is last byte indication
        if(decodedByte == 256){
            return -1;
        }

        //update predictive model as we did in the encoder
        double[] lu = pm.cumulativeDistributionInterval(decodedByte);
        low += lu[0] * range;
        range *= (lu[1] - lu[0]);

        int b;
        while ((low ^ (low + range)) < FIRST_BIT_INDICATOR) {
            range = range << 1 ;
            low = low << 1 & LONG_MAX_VALUE;
            code = code<<1 & LONG_MAX_VALUE;

            b = bbis.read();
            if(b == 1){
                code++;
            }
        }

        pm.continueSequence(decodedByte);

        return decodedByte;
    }

    public void close() throws IOException{
        if(bbis != null){
            bbis.close();
        }
    }
}
