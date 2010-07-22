/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.deplump;

/**
 * Reads a Deplump compressed stream.
 *
 * @author nicholasbartlett
 */

import java.io.IOException;
import java.io.InputStream;

public class PlumpStream extends InputStream {

    private Decoder dec;
    private boolean eos = false;

    /**
     * Creates a PlumpStream with a specified underlying InputStream.
     *
     * @param is underlying InputStream
     * @throws IOException
     */
    public PlumpStream(InputStream is) throws IOException {
        dec = new Decoder(new SMPredictiveModel(), is);
    }

    @Override
    /**
     * Unsupported.
     */
    public int available() {
        throw new RuntimeException("unsupported");
    }

    @Override
    /**
     * Mark is not supported.
     */
    public boolean markSupported() {
        return false;
    }

    @Override
    /**
     * Closes this input stream.
     */
    public void close() throws IOException {
        dec.close();
    }

    @Override
    /**
     * Unsupported.
     */
    public int read(byte[] bs) throws IOException {
        throw new RuntimeException("unsupported");
    }

    @Override
    /**
     * Unsupported.
     */
    public int read(byte[] bs, int off, int len) throws IOException {
        throw new RuntimeException("unsupported");
    }

    /**
     * Reads the next byte and returns it as an integer in [0, 256).  Returns -1
     * if the end of the stream has been reached.
     *
     * @return next byte as integer in [0, 256) or -1 if end of stream
     * @throws IOException
     */
    public int read() throws IOException {
        int b;

        if(eos){
            return -1;
        } else {
            b = dec.read();
            if(b == -1){
                eos = true;
            }
            return b;
        }
    }

    @Override
    /**
     * Unsupported.
     */
    public long skip(long n) throws IOException {
        throw new RuntimeException("unsupported");
    }
}
