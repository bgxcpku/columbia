/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.io.Serializable;

/**
 * Interface for a Byte Predictive model.  This is the interface for models used
 * in compression with an arithmetic or range encoder.
 * @author nicholasbartlett
 */

public abstract class BytePredictiveModel implements Serializable {

    /**
     * After calling one of the continue sequence methods low will be updated
     * to be the value of the predictive cdf of type - 1.
     */
    public double low;
    
    /**
     * After calling one of the continue sequence methods high will be updated
     * to be the value of the predictive cdf of type.
     */
    public double high;

    /**
     * After calling one of the Decode methods the value of decode will be updated
     * to be the value of type such that CDF(decode) is less than the pointOnCDF
     * which is in turn less than CDF(decode).
     */
    public int decode;

    /**
     * Updates low and high based on the assumption that this type is a continuation
     * of the current sequence.  The type is then incorporated into the model.
     * @param type
     */
    public abstract void continueSequenceEncode(byte type);

    /**
     * Updates low, high, and decode based on the assumption that this type is a continuation
     * of the current sequence.  The type is then incorporated into the model.
     * @param pointOnCDF
     */
    public abstract void continueSequenceDecode(double pointOnCDF);

    /**
     * Updates low and high based on the assumption that this type is a continuation
     * of the current sequence.
     * @param type
     */
    public abstract void continueSequenceEncodeWithoutInsertion(byte type);

    /**
     * Updates low, high, and decode based on the assumption that this type is a continuation
     * of the current sequence.
     * @param pointOnCDF
     */
    public abstract void continueSequenceDecodeWithoutInsertion(double pointOnCDF);

    /**
     * Updates low and high in such a way as to encode the eos character.
     */
    public abstract void endOfStream();

}
