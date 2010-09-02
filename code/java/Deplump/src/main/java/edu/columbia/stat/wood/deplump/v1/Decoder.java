/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.deplump;

import edu.columbia.stat.wood.sequencememoizer.BytePredictiveModel;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class which implements range decoder.
 * @author nicholasbartlett
 */
public class Decoder {

    private long first_bit_indicator = (long) 1 << 62;
    private long long_max_value = Long.MAX_VALUE;
    private long range_min_value = Integer.MAX_VALUE;
    private long low = 0;
    private long range = long_max_value;
    private long code;
    private BufferedBitInputStream bbis;
    private BytePredictiveModel pm;

    /**
     * Initializes decoder with specified PredictiveModel and unerlying InputStream.
     *
     * @param pm PredictiveModel used for copmressing
     * @param is underlying InputStream
     * @throws IOException
     */
    public Decoder(BytePredictiveModel pm, InputStream is) throws IOException {
        this.pm = pm;
        this.bbis = new BufferedBitInputStream(is);
        initializeCode();
    }

    private void initializeCode() throws IOException {
        code = 0;
        int bit;
        for (int i = 62; i >= 0; i--) {
            bit = bbis.read();
            if (bit == 1) {
                code += (long) 1 << i;
            }
        }
    }

    /**
     * Decodes the next byte and returns as int.  Returns -1 if end of stream code is decoded.
     * 
     * @return next byte or -1 if end of stream is reached
     * @throws IOException
     */
    public int read() throws IOException {
        double pointOnCDF, l, h;
        int b;

        pointOnCDF = (double) (code - low) / (double) range;
        pm.continueSequenceDecode(pointOnCDF);

        l = pm.low;
        h = pm.high;

        if (pm.decode == 256) {
            return -1;
        }

        low += l * range;
        range *= (h - l);

        while ((low ^ (low + range)) < first_bit_indicator) {
            range = range << 1;
            low = low << 1 & long_max_value;
            code = code << 1 & long_max_value;

            b = bbis.read();
            if (b == 1) {
                code++;
            }
        }

        if (range < range_min_value) {
            emitRange();
        }

        return pm.decode;
    }

    private void emitRange() throws IOException {
        int b;

        while (range < first_bit_indicator) {
            range = range << 1 & long_max_value;
            low = low << 1 & long_max_value;

            b = bbis.read();
            if (b == 1) {
                code++;
            }
        }

        b = bbis.read();
        if (b == 1) {
            code++;
        }

        b = bbis.read();
        if (b == 1) {
            code++;
        }

        low = 0;
        range = long_max_value;
    }
}
