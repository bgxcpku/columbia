/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */

public abstract class BytePredictiveModel {
    
    public double low, high;
    public int decode;

    public abstract void continueSequenceEncode(byte type);

    public abstract void continueSequenceDecode(double pointOnCDF);

    public abstract void continueSequenceEncodeWithoutInsertion(byte type);

    public abstract void continueSequenceDecodeWithoutInsertion(double pointOnCDF);

    public abstract void endOfStream();

}
