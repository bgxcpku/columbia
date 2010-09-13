/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.net.URL;

/**
 * Factory class to enable different versions of the sequence memoizer classes
 * to be loaded by the compression software.
 * @author nicholasbartlett
 */
public abstract class BytePredictiveModelFactory {

    /**
     * Gets a BytePredictiveModel with the specified parameters.
     * @param depth depth of model
     * @param maxNumberRestaurants max number of restaurants allowable in model
     * @param maxSequenceLength max length of underlying sequence allowable in model
     * @param url url of a serialized pre-trained model
     * @return a BytePredictiveModel
     */
    public abstract BytePredictiveModel get(int depth, long maxNumberRestaurants, long maxSequenceLength, URL url);
    
}
