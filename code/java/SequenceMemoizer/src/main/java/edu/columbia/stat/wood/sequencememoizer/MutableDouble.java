/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public class MutableDouble {

    private double value;

    public MutableDouble(double value){
        this.value = value;
    }

    public void set(double value){
        this.value = value;
    }

    public void times(double multFactor){
        value *= multFactor;
    }

    public void decrement(double decrementValue){
        value -= decrementValue;
    }

    public double doubleVal(){
        return value;
    }
}
