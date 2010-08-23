/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import edu.columbia.stat.wood.util.ByteFiniteDiscreteDistribution;

/**
 *
 * @author nicholasbartlett
 */
public class ByteSequenceMemoizerParameters extends SequenceMemoizerParameters{

    /**
     * Base distribution of the model.  Since only 256 bytes available it is not
     * a restriction to require this to be finite.
     */
    public ByteFiniteDiscreteDistribution baseDistribution;

    

    


}
