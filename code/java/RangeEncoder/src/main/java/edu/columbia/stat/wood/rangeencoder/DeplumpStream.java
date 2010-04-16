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

public class DeplumpStream extends OutputStream {

    public Encoder enc;

    public DeplumpStream(OutputStream out) {
        enc =  new Encoder(new FiniteDepthHPYPPredictiveModel(), out);
    }

    @Override
    public void close() throws IOException {
        enc.close();
    }

    @Override
    public void flush() throws IOException {
        //enc.flush();
        throw new RuntimeException("unsuported");
    }

    @Override
    public void write(byte[] byteSequence) throws IOException {
        throw new RuntimeException("unsuported");
    }

    @Override
    public void write(byte[] byteSequence, int off, int len) throws IOException {
        throw new RuntimeException("unsuported");
    }

    public void write(int i) throws IOException {
        enc.encode(i);
    }
}
