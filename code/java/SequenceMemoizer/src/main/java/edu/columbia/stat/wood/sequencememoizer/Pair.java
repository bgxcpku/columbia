/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 * Container object for two objects.
 *
 * @author nicholasbartlett
 */
public class Pair<F,S> {
    private F first;
    private S second;

    /**
     * @param first first object
     * @param second second object
     */
    public Pair(F first, S second){
        this.first = first;
        this.second = second;
    }

    /**
     * Gets the first object.
     *
     * @return first object
     */
    public F first(){
        return first;
    }

    /**
     * Gets the seond object.
     *
     * @return second object
     */
    public S second(){
        return second;
    }
}
