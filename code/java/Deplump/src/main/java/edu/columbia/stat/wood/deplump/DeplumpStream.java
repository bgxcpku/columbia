/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Compression stream using Deplump predictie model.
 *
 * @author nicholasbartlett
 */

public class DeplumpStream extends OutputStream {

    private Encoder enc;

    /**
     * @param out underlying OutputStream
     * @throws IOException
     */
    public DeplumpStream(OutputStream out) throws IOException {
        enc =  new Encoder(new SMPredictiveModel(), out);
    }

    @Override
    /**
     * Ends stream and closes underlying OutputStream.
     */
    public void close() throws IOException {
        enc.close();
    }

    @Override
    /**
     * Flushes underlying OutputStream.
     */
    public void flush() throws IOException {
        enc.flush();
    }

    @Override
    /**
     * Unsuported.
     */
    public void write(byte[] byteSequence) throws IOException {
        throw new RuntimeException("unsuported");
    }

    @Override
    /**
     * Unsuported.
     */
    public void write(byte[] byteSequence, int off, int len) throws IOException {
        throw new RuntimeException("unsuported");
    }

    /**
     * Writes specified integer.
     *
     * @param i integer to write
     * @throws IOException
     */
    public void write(int i) throws IOException {
        enc.encode(i);
    }
}
