/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 * Container object for two integers.
 *
 * @author nicholasbartlett
 */
public class IntegerPair {

    private int first;
    private int second;

    /**
     * Creates the object with specified underlying values.
     * @param c
     * @param t
     */
    public IntegerPair(int c, int t){
        this.first = c;
        this.second = t;
    }

    /**
     * Gets the first value.
     * @return first value
     */
    public int first(){
        return first;
    }

    /**
     * Gets the second value.
     * @return second value
     */
    public int second(){
        return second;
    }

    /**
     * Overides equals for use in a hash map.
     * @param obj comparison object
     * @return true if same class and same underying fields
     */
    @Override
    public boolean equals(Object obj){
        if(obj != null && obj.getClass() == getClass()){
            if(first == ((IntegerPair) obj).first() && second == ((IntegerPair) obj).second()){
                return true;
            }
        }

        return false;
    }

    /**
     * Overides hash code method to use both underlying fields.
     * @return hash code
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.first;
        hash = 97 * hash + this.second;
        return hash;
    }    
}
