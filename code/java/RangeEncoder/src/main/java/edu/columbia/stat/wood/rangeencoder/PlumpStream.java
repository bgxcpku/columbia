/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.rangeencoder;

/**
 *
 * @author nicholasbartlett
 */
import java.io.IOException;
import java.io.InputStream;

public class PlumpStream extends InputStream {

    public Decoder dec;

    public PlumpStream(InputStream in) throws IOException {
        dec = new Decoder(new FiniteDepthHPYPPredictiveModel(), in);
    }

    @Override
    public int available() {
        //unsupported;
        return 0;
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
        return read(bs, 0, bs.length);
    }

    @Override
    public int read(byte[] bs, int off, int len) throws IOException {
        for (int i = off; i < len; ++i) {
            int nextByte = read();
            if (nextByte == -1) {
                return (i - off);
            }
            bs[i] = (byte) (nextByte & 0xFF);
        }
        return len > 0 ? len : 0;
    }

    public int read() throws IOException {
        return dec.read();
    }

    @Override
    public long skip(long n) throws IOException {
        for (long i = 0; i < n; ++i) {
            if (read() == -1) {
                return i;
            }
        }
        return n;
    }
}
