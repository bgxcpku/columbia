/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

/**
 * Container object for integer.  Allows the underlying value of the object to
 * be changed and manipulated within the same object instance.
 *
 * @author nicholasbartlett
 */
public class MutableInteger {

    private int value;

    /**
     * @param value value of instantiated MutableInteger
     */
    public MutableInteger(int value) {
        this.value = value;
    }

    /**
     * Allows the value of the object to be changed to the specified value.
     *
     * @param value new value for object
     */
    public void set(int value) {
        this.value = value;
    }

    /**
     * Increases the value of the object by 1.
     */
    public void increment() {
        value++;
    }

    /**
     * Decreases the value of the object by 1.
     */
    public void decrement() {
        value--;
    }

    /**
     * Gets the value of the object.
     *
     * @return int value of object
     */
    public int intVal() {
        return value;
    }
}
