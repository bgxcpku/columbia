/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.net.URL;

/**
 *
 * @author nicholasbartlett
 */
public abstract class BytePredictiveModelFactory {

    public abstract BytePredictiveModel get(int depth, long maxNumberRestaurants, long maxSequenceLength, URL url);
    
}
