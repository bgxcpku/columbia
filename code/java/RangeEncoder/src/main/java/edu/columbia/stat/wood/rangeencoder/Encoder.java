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
public class Encoder {

    private static long START_RANGE = Long.MAX_VALUE ;
    private static long FD_CONSTANT = Long.MAX_VALUE / 256 + 1;
    private static long MIN_RANGE = (long) Math.pow(2, 40);

    private long low = 0;
    private long range = START_RANGE;
    private PredictiveModel pm;

    public double logLoss = 0.0;
    public double maxLogLoss = 0.0;
    public int bitsEmitted = 0;

    private int index = 0;
    private int bufferLength = 1000;
    private byte[] bytesToEmitBuffer = new byte[bufferLength];
    private OutputStream out;

    public Encoder(PredictiveModel pm, OutputStream out){
        this.pm = pm;
        this.out = out;
    }

    public void encode(int nextByte) throws IOException{
        double[] startSize = pm.cumulativeDistributionInterval(nextByte);
        this.encode(startSize[0], startSize[1] - startSize[0]);
        double ll = pm.continueSequence(nextByte)/Math.log(2);
        logLoss -= ll;
        maxLogLoss = (-ll > maxLogLoss)?-ll:maxLogLoss;
    }

    //assuming that start and size refer to an interval in 0 - INT_RANGE
    public void encode(double start, double size) throws IOException {
        low += start * range;
        range *=  size;

        long firstByteLow = low / FD_CONSTANT;
        long firstByteHigh = (low + range) / FD_CONSTANT;

        while (firstByteLow == firstByteHigh) {
            this.emitByte((byte) (firstByteLow - 128));
            low = (low % FD_CONSTANT) * 256;
            range*=256;

            firstByteLow = low / FD_CONSTANT;
            firstByteHigh = (low + range) / FD_CONSTANT;
        }

        while (range < MIN_RANGE) {
            this.emitByte((byte) (firstByteLow - 128));
            low = (low % FD_CONSTANT) * 256;
            range = START_RANGE - low;
            firstByteLow = low / FD_CONSTANT;
        }
    }

    public void endEncoding() throws IOException {
        this.encode((int) 256);
        
        long firstByteLow = low / FD_CONSTANT;
        long firstByteHigh = (low + range) / FD_CONSTANT;
        
        while (firstByteLow + 1 >= firstByteHigh) {
            
            this.emitByte((byte) (firstByteLow - 128));
            range = (range % FD_CONSTANT) * 256;
            low = (low % FD_CONSTANT) * 256;

            firstByteLow = low / FD_CONSTANT;
            firstByteHigh = (low + range) / FD_CONSTANT;

        } 

        this.emitByte((byte) (firstByteLow + 1 - 128));
        this.flush();
        out.close();
    }

    private void emitByte(byte emission) throws IOException {
        bytesToEmitBuffer[index++] = emission;
        if(index == bufferLength){
            this.flush();
        }
        bitsEmitted += 8;
    }

    public void flush() throws IOException{
        out.write(bytesToEmitBuffer,0,index);
        index = 0;
    }
}
