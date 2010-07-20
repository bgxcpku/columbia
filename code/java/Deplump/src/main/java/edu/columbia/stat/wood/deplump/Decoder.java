/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.rangeencoder;

import java.io.IOException;
import java.io.InputStream;

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

    private int length;
    private double[] interval;
    private int decodedBytes;

    private BufferedBitInputStream bbis;
    private PredictiveModel pm;

    public Decoder(PredictiveModel pm, InputStream is) throws IOException{
        int b;

        length = 0;
        length += is.read() << 24;
        length += is.read() << 16;
        length += is.read() << 8;
        length += is.read();

        decodedBytes = 0;

        this.pm = pm;
        this.bbis = new BufferedBitInputStream(is);
        this.initializeCode();
        interval = new double[2];
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
        double pointOnCDF;
        int decodedByte, b;
        
        pointOnCDF = (double) (code - low)/range;
        decodedByte = pm.inverseCDF(pointOnCDF, interval);
        
        if(decodedBytes++ >= length){
            return -1;
        }

        low += interval[0] * range;
        range *= (interval[1] - interval[0]);

        while ((low ^ (low + range)) < FIRST_BIT_INDICATOR) {
            range = range << 1 ;
            low = low << 1 & LONG_MAX_VALUE;
            code = code<<1 & LONG_MAX_VALUE;

            b = bbis.read();
            if(b == 1){
                code++;
            }
        }

        return decodedByte;
    }

    public void close() throws IOException{
        if(bbis != null){
            bbis.close();
        }
    }
}
