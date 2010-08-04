/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public class Range {
    private double low;
    private double high;

    public Range(double low, double high){
        this.low = low;
        this.high = high;
    }

    public double low(){
        return low;
    }

    public double high(){
        return high;
    }

    public void setLow(double low){
        this.low = low;
    }

    public void setHigh(double high){
        this.high = high;
    }

    public void set(double low, double high){
        this.low = low;
        this.high = high;
    }
}
