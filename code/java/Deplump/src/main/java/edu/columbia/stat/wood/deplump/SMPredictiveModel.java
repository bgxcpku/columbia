/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.deplump;

import edu.columbia.stat.wood.sequencememoizer.FiniteAlphabetSequenceMemoizerParameters;
import edu.columbia.stat.wood.sequencememoizer.FiniteAlphabetSequenceMemoizer;

/**
 * Sequence memoizer predictive model for compression.
 * 
 * @author nicholasbartlett
 */
public class SMPredictiveModel extends FiniteAlphabetSequenceMemoizer {

    /**
     * Initializes this as a Sequence Memoizer object with an alphabet size of
     * 257, inifinite depth, and a random seed of 0.  Discount parameters and the
     * alpha parameter are set to the default values.
     */
    public SMPredictiveModel(){
        super(new FiniteAlphabetSequenceMemoizerParameters(257, -1, 1));
    } 
}
