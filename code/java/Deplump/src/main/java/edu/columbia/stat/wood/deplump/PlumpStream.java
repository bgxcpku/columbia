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

import edu.columbia.stat.wood.sequencememoizer.ByteSequenceMemoizer;
import java.io.IOException;
import java.io.InputStream;

public class PlumpStream extends InputStream {

    private Decoder dec;
    private boolean eos = false;
    private InputStream is;

    public PlumpStream(InputStream is) throws IOException {
        dec = new Decoder(new ByteSequenceMemoizer(1023,1), is);
        this.is = is;
    }

    @Override
    public int available() throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void mark(int readlimit){
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

    @Override
    public int read(byte[] bs) throws IOException {
        return read(bs,0,bs.length);
    }

    @Override
    public int read(byte[] bs, int off, int len) throws IOException {
        int index, upperIndex, b;

        if(eos){
            return -1;
        } else {
        
            index = off;
            upperIndex = off + len;
            while(index < upperIndex && (b = read()) > -1){
                bs[index++] = (byte) b;
            }
        
            return index - off;
        }
    }

    public int read() throws IOException {
        if(eos){
            return -1;
        } else {
            int b;

            b = dec.read();
            if(b == -1){
                eos = true;
            }
            return b;
        }
    }

    @Override
    public long skip(long n) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
