/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.hpyp;

/**
 * Container object for int[] containing the values of the sequence being memoized. 
 * Object implements specific methods useful in the data manipulation needed
 * for the sequence memoizer.
 *
 * @author nicholasbartlett
 */
public class Sequence {

    private int[] value;
    private int length;

    /**
     * Initializes the underlying fields.
     */
    public Sequence() {
        value = new int[100000];
        length = 0;
    }

    /**
     * Initializes the underlying value field to a given int[].
     *
     * @param value int[] specifying initial value of underlying array
     */
    public Sequence(int[] value) {
        this.value = value;
        this.length = value.length;
    }

    /**
     * Gets the underlying full sequence.
     *
     * @return an int[] which is the full sequence, possibly with 0's filling in
     * the end
     */
    public int[] fullSeq() {
        return value;
    }

    /**
     * True length of the full sequnce.  This can be used to differentiate between
     * the true sequence and the 0 fill at the end.
     *
     * @return int length value
     */
    public int length() {
        return length;
    }

    /**
     * Adds token to the sequence.  Automatically makes the underlying int[] longer
     * if need be.  Increments are made in batches of 100k.
     *
     * @param token int value to be added
     */
    public void add(int token) {
        int[] newValue;

        if (length == value.length) {
            newValue = new int[value.length + 100000];
            System.arraycopy(value, 0, newValue, 0, value.length);
            value = newValue;
        }

        value[length++] = token;
    }
}
