/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.deplump;

import edu.columbia.stat.wood.sequencememoizer.BytePredictiveModel;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class to implement the nuts and bolts of range encoding.
 *
 * @author nicholasbartlett
 */
public class Encoder {

    private long first_bit_indicator = (long) 1 << 62;
    private long long_max_value = Long.MAX_VALUE;
    private long low = 0;
    private long range = long_max_value;
    private long range_min_value = Integer.MAX_VALUE;
    private OutputStream out;
    private BytePredictiveModel pm;
    private int currentByte = 0;
    private int byteIndex = 0;

    /**
     * Creates the encoder using a specified predictive model and an underlying
     * OuputStream.
     *
     * @param pm predictive model
     * @param out underlying OutputStream
     */
    public Encoder(BytePredictiveModel pm, OutputStream out) {
        this.pm = pm;
        this.out = out;
    }

    /**
     * Actually encodes an integer observation.
     *
     * @param observation integer observation
     * @throws IOException
     */
    public void encode(byte observation) throws IOException {
        double l, h;

        pm.continueSequenceEncode(observation);
        l = pm.low;
        h = pm.high;

        low += l * range;
        range *= (h - l);

        while ((low ^ (low + range)) < first_bit_indicator) {
            write(low >> 62);

            range = range << 1;
            low = low << 1 & long_max_value;
        }

        if (range < range_min_value) {
            emitRange();
        }
    }

    private void encodeEOF() throws IOException {
        double l, h;

        pm.endOfStream();
        l = pm.low;
        h = pm.high;

        low += l * range;
        range *= (h - l);

        while ((low ^ (low + range)) < first_bit_indicator) {
            write(low >> 62);

            range = range << 1;
            low = low << 1 & long_max_value;
        }
    }

    private void emitRange() throws IOException {
        long code, diff;

        while (range < first_bit_indicator) {
            write((low + range) >> 62);
            range = range << 1 & long_max_value;
            low = low << 1 & long_max_value;
        }

        diff = 1 << 61;
        code = low + range - diff;

        write(code >> 62);
        code = code << 1 & long_max_value;
        write(code >> 62);

        low = 0;
        range = long_max_value;
    }

    private void write(long bit) throws IOException {
        if (bit == 1) {
            currentByte += 1 << (7 - byteIndex);
        }

        if (byteIndex == 7) {
            out.write(currentByte);
            byteIndex = -1;
            currentByte = 0;
        }

        byteIndex++;
    }

    /**
     * Transmits end of stream character, transmits current range and closes the
     * underlying OutputStream.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        encodeEOF();
        emitRange();
        out.write(currentByte);
    }
}
