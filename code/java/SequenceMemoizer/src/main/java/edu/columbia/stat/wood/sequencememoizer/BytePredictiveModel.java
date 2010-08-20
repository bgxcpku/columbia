/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import edu.columbia.stat.wood.sequencememoizer.ConstantSpaceSequenceMemoizer.ByteRange;

/**
 *
 * @author nicholasbartlett
 */

public abstract class BytePredictiveModel {

    public ByteRange range;

    public abstract void continueSequenceEncode(byte type);

    public abstract void continueSequenceDecode(double pointOnCdf);

    public abstract void endOfStream();
}
