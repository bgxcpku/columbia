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

import edu.columbia.stat.wood.sequencememoizer.BytePredictiveModel;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PlumpStream extends InputStream {

    private Decoder dec;
    private boolean eos = false;
    private InputStream is;

    public PlumpStream(InputStream is) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        //get version
        int version = is.read();
        
        ClassLoader cl = ClassLoader.getSystemClassLoader();

        dec = (Decoder) cl.loadClass("edu.columbia.stat.wood.deplump.v" + version + ".Decoder" ).newInstance();
        Constructor ct = cl.loadClass("edu.columbia.stat.wood.sequencememoizer.v" + version + ".ByteSequenceMemoizer" ).getConstructor();

        Object[] argList = new Object[0];
        BytePredictiveModel bm = (BytePredictiveModel) ct.newInstance(argList);

        //BytePredictiveModel bm = (BytePredictiveModel) cl.loadClass("edu.columbia.stat.wood.sequencememoizer.v" + version + ".ByteSequenceMemoizer" ).newInstance();

        dec.set(bm, is, true);

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
