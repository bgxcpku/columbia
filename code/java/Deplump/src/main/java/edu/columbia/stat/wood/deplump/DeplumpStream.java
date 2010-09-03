/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

import edu.columbia.stat.wood.deplump.v1.Encoder;
import edu.columbia.stat.wood.sequencememoizer.v1.BytePredictiveModelFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 * Compression stream using Deplump predictive model.
 *
 * @author nicholasbartlett
 */

public class DeplumpStream extends OutputStream {

    private Encoder enc;
    private OutputStream out;

    public DeplumpStream(OutputStream out, int depth, long maxNumberRestaurants, long maxSequenceLength, boolean insert, URL serializedModel) throws IOException {
        //version
        out.write(1);

        BytePredictiveModelFactory factory = new BytePredictiveModelFactory();

        this.out = out;
        enc = new Encoder(factory.get(depth, maxNumberRestaurants, maxSequenceLength, serializedModel),out,insert);
    }

    @Override
    public void close() throws IOException {
        enc.close();
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void write(byte[] byteSequence) throws IOException {
        for(byte b : byteSequence){
            enc.encode(b);
        }
    }

    @Override
    public void write(byte[] byteSequence, int off, int len) throws IOException {
        int upperIndex = off + len;
        
        for(int i = off; i < upperIndex; i++){
            enc.encode(byteSequence[i]);
        }
    }
    
    public void write(int i) throws IOException {
        enc.encode((byte) i);
    }
}
