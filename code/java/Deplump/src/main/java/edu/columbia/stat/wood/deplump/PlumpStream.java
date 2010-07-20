/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.deplump;

/**
 *
 * @author nicholasbartlett
 */

import java.io.IOException;
import java.io.InputStream;

public class PlumpStream extends InputStream {

    public Decoder dec;

    public PlumpStream(InputStream is) throws IOException {
        dec = new Decoder(new SMPredictiveModel(), is);
    }

    @Override
    public int available() {
        throw new RuntimeException("unsupported");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void close() throws IOException {
        dec.close();
    }

    @Override
    public int read(byte[] bs) throws IOException {
        throw new RuntimeException("unsupported");
    }

    @Override
    public int read(byte[] bs, int off, int len) throws IOException {
        throw new RuntimeException("unsupported");
    }

    public int read() throws IOException {
        return dec.read();
    }

    @Override
    public long skip(long n) throws IOException {
        throw new RuntimeException("unsupported");
    }
}
