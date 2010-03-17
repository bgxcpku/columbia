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
import java.io.OutputStream;

public class DeplumpStream extends OutputStream {
    
    public Encoder enc;

    public DeplumpStream(OutputStream out) {
        enc =  new Encoder(new FiniteDepthHPYPPredictiveModel(), out);
    }

    @Override
    public void close() throws IOException {
        enc.endEncoding();
    }

    @Override
    public void flush() throws IOException {
        enc.flush();
    }

    @Override
    public void write(byte[] byteSequence) throws IOException {
        this.write(byteSequence, 0, byteSequence.length);
    }

    @Override
    public void write(byte[] byteSequence, int off, int len) throws IOException {
        while (off < len) {
            write(byteSequence[off++]);
        }
    }

    public void write(int i) throws IOException {
        enc.encode((int) (i + 128));
    }
}
