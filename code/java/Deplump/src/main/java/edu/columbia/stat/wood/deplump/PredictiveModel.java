/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

import edu.columbia.stat.wood.sequencememoizer.ConstantSpaceSequenceMemoizer.Range;


/**
 * Predictive model used during compression and decompression.
 *
 * @author nicholasbartlett
 */
public abstract class PredictiveModel {

    public Range range;

    public abstract void continueSequenceEncode(int observation);

    public abstract void continueSequenceDecode(double pointOnCdf);
}
