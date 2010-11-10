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

import edu.columbia.stat.wood.sequencememoizer.BytePredictiveModelFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

public class PlumpStream extends InputStream {

    private Decoder dec;
    private boolean eos = false;
    private InputStream is;
    
    public PlumpStream(InputStream is, int depth, long maxNumberRestaurants, long maxSequenceLength, boolean insert, URL serializedModel) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        //get version
        int version = is.read();
        
        ClassLoader cl = ClassLoader.getSystemClassLoader();

        dec = (Decoder) cl.loadClass("edu.columbia.stat.wood.deplump.v" + version + ".Decoder" ).newInstance();
        BytePredictiveModelFactory factory = (BytePredictiveModelFactory) cl.loadClass("edu.columbia.stat.wood.sequencememoizer.v" + version + ".BytePredictiveModelFactory").newInstance();

        dec.set(factory.get(depth, maxNumberRestaurants, maxSequenceLength, serializedModel), is, insert);
        this.is = is;
    }

    public PlumpStream(InputStream is) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        //get version
        int version = is.read();

        ClassLoader cl = ClassLoader.getSystemClassLoader();

        dec = (Decoder) cl.loadClass("edu.columbia.stat.wood.deplump.v" + version + ".Decoder" ).newInstance();
        BytePredictiveModelFactory factory = (BytePredictiveModelFactory) cl.loadClass("edu.columbia.stat.wood.sequencememoizer.v" + version + ".BytePredictiveModelFactory").newInstance();

        dec.set(factory.get(-1, -1, -1, null), is, true);
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
