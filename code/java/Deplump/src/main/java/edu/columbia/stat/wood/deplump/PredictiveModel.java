/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

import edu.columbia.stat.wood.sequencememoizer.Range;
import edu.columbia.stat.wood.sequencememoizer.RangeAndDecode;

/**
 * Predictive model used during compression and decompression.
 *
 * @author nicholasbartlett
 */
public interface PredictiveModel {

    /**
     * Incorporates the observation in the model with the assumption that this observation
     * is the next in a continuing sequence. Observations are restricted to the interval [0,alphabetSize).
     *
     * @param observation integer value of observation
     * @return predictive predictive CDF prior to incorporating the observation into the model
     */
    public void continueSequenceRange(int observation, Range range);

    /**
     * Finds the observation on the predictive CDF with the assumption that the next observation
     * is the next in a continuing sequence.  The observation is then incorporated into
     * the model.
     *
     * @param pointOnCdf point on cdf, must be in [0.0,1.0)
     * @return Pair containing type of observation seated and predictive cdf prior to incorporating the
     * type into the model
     */
    public void continueSequenceRangeAndDecode(double pointOnCdf, RangeAndDecode rad);
}
