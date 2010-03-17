/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.rangeencoder;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author nicholasbartlett
 */

public class Decoder {

    private static long START_RANGE = Long.MAX_VALUE ;
    private static long FD_CONSTANT = Long.MAX_VALUE / 256 + 1;
    private static long MIN_RANGE = (long) Math.pow(2, 40);

    private long low = 0;
    private long range = START_RANGE;
    private PredictiveModel pm;
    
    private InputStream in;
    private int bufferLength = 1000;
    private int leftIndex = bufferLength;
    private int rightIndex = bufferLength;
    private int[] bytesToEmitBuffer = new int[bufferLength];

    public Decoder(PredictiveModel pm, InputStream in) throws IOException{
        this.pm = pm;
        this.in = in;
    }

    private byte[] inputByteBuffer = new byte[bufferLength];
    private int leftInputBufferIndex = bufferLength;
    private int rightInputBufferIndex = bufferLength;

    public int getNextByte() throws IOException {
        if(leftInputBufferIndex == rightInputBufferIndex){
            if(rightInputBufferIndex == bufferLength){
                rightInputBufferIndex = in.read(inputByteBuffer);
                leftInputBufferIndex = 0;
            } else{
                return 0;
            }
        }
        return (int) (inputByteBuffer[leftInputBufferIndex++] + 128);
    }

    public int read() throws IOException{
        if(leftIndex < rightIndex){
            return bytesToEmitBuffer[leftIndex++];
        } else if (rightIndex == bufferLength){
            this.decode();
            leftIndex = 0;
            return bytesToEmitBuffer[leftIndex++];
        } else{
            return -1;
        }
    }

    int[] currentListOfBytes = new int[0];
    private void decode() throws IOException {
        rightIndex = 0;
        this.addOnByte();
        while (rightIndex < bufferLength) {
            double[] pointsOnCDF = this.getPointsOnCdf();

            int[] lowHighDecodedByte = pm.predDist(pointsOnCDF[0], pointsOnCDF[1]);

            if (lowHighDecodedByte[0] != lowHighDecodedByte[1]) {
                this.addOnByte();
                continue;
            }

            if(lowHighDecodedByte[0] == 256){
                break;
            }
            this.emitByte(lowHighDecodedByte[0]);

            double[] startAndSize = pm.cumulativeDistributionInterval(lowHighDecodedByte[0]);

            low += startAndSize[0] * range;
            range *= (startAndSize[1] - startAndSize[0]);

            long firstByteLow = low / FD_CONSTANT;
            long firstByteHigh = (low + range) / FD_CONSTANT;

            while (firstByteLow == firstByteHigh) {
                this.shiftOutByte();
                low = (low % FD_CONSTANT) * 256;
                range *= 256;

                firstByteLow = low / FD_CONSTANT;
                firstByteHigh = (low + range) / FD_CONSTANT;
            }

            while (range < MIN_RANGE) {
                this.shiftOutByte();
                low = (low % FD_CONSTANT) * 256;
                range = START_RANGE - low;
                firstByteLow = low / FD_CONSTANT;
            }

            pm.continueSequence(lowHighDecodedByte[0]);
        }
    }

    private void shiftOutByte() {
        int[] newCurrentListOfBytes = new int[currentListOfBytes.length - 1];
        System.arraycopy(currentListOfBytes, 1, newCurrentListOfBytes, 0, currentListOfBytes.length - 1);
        currentListOfBytes = newCurrentListOfBytes;
    }

    private void addOnByte() throws IOException {
        int[] newCurrentListOfBytes = new int[currentListOfBytes.length + 1];
        System.arraycopy(currentListOfBytes, 0, newCurrentListOfBytes, 0, currentListOfBytes.length);
        newCurrentListOfBytes[currentListOfBytes.length] = this.getNextByte();
        currentListOfBytes = newCurrentListOfBytes;
    }

    private double[] getPointsOnCdf() throws IOException {
        double[] retVal = new double[2];
        
        long lowNum = 0;
        for (int i = 0; i < currentListOfBytes.length; i++) {
            lowNum += currentListOfBytes[i]*(long) (FD_CONSTANT / (long) Math.pow(256,i));
        }

        long highNum = lowNum + (FD_CONSTANT / (long) Math.pow(256,currentListOfBytes.length-1)) - 1;

        retVal[0] = (double) (lowNum - low)/range;
        retVal[1] = (double) (highNum - low)/range;
        
        return retVal;
    }

    private void emitByte(int emission) throws IOException {
        bytesToEmitBuffer[rightIndex++] = emission;
    }

    public void close() throws IOException{
        in.close();
    }
}
