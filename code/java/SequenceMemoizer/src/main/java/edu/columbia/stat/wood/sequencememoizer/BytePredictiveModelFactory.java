/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public abstract class BytePredictiveModelFactory {

    public abstract BytePredictiveModel get();

    public abstract BytePredictiveModel get(String serializedModelKey);
}
