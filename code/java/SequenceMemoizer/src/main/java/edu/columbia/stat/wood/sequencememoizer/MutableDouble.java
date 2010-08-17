/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 * Container object for native double.
 * 
 * @author nicholasbartlett
 */
public class MutableDouble {

    private double value;

    /**
     * @param value value of underlying double
     */
    public MutableDouble(double value){
        this.value = value;
    }

    /**
     * Sets value of object.
     *
     * @param value value to set object
     */
    public void set(double value){
        this.value = value;
    }

    /**
     * Multiplies object by multiplication factor.
     *
     * @param multFactor factor to multiply by
     */
    public void times(double multFactor){
        value *= multFactor;
    }

    /**
     * Decreases value of object.
     *
     * @param decrementValue amount to subtract from object value
     */
    public void decrement(double decrementValue){
        value -= decrementValue;
    }

    /**
     * Gets the value of object as native double.
     *
     * @return value of object as native double
     */
    public double doubleVal(){
        return value;
    }
}
