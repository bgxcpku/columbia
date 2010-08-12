/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 * Container object for two doubles.
 *
 * @author nicholasbartlett
 */
public class Range {

    private double low;
    private double high;

    /**
     * Instantiates the object with specified double values.
     *
     * @param low
     * @param high
     */
    public Range(double low, double high){
        this.low = low;
        this.high = high;
    }

    /**
     * Gets the low value.
     *
     * @return low value
     */
    public double low(){
        return low;
    }

    /**
     * Gets the high value.
     *
     * @return high value
     */
    public double high(){
        return high;
    }

    /**
     * Sets the low value.
     *
     * @param low low value
     */
    public void setLow(double low){
        this.low = low;
    }

    /**
     * Set the high value.
     *
     * @param high high value
     */
    public void setHigh(double high){
        this.high = high;
    }

    /**
     * Sets both low and high value.
     *
     * @param low low value
     * @param high high value
     */
    public void set(double low, double high){
        this.low = low;
        this.high = high;
    }
}
