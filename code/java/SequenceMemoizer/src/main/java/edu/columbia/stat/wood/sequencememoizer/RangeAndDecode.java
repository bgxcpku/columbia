/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 * Container object to extend Range to also hold an integer.
 *
 * @author nicholasbartlett
 */
public class RangeAndDecode extends Range{
    private int decode;

    /**
     * Instantiates the container with underlying values
     *
     * @param decode
     * @param low
     * @param high
     */
    public RangeAndDecode(int decode, double low, double high){
        super(low,high);
        this.decode = decode;
    }

    /**
     * Gets the decode value.
     *
     * @return decode value
     */
    public int decode(){
        return decode;
    }

    /**
     * Sets the decode value.
     *
     * @param decode decode value
     */
    public void setDecode(int decode){
        this.decode = decode;
    }

    /**
     * Sets all three underlying values.
     * 
     * @param decode
     * @param low
     * @param high
     */
    public void set(int decode, double low, double high){
        this.decode = decode;
        super.set(low, high);
    }
}
