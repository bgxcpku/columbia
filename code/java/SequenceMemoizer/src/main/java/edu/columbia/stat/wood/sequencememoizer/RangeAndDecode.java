/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public class RangeAndDecode extends Range{
    private int decode;

    public RangeAndDecode(int decode, double low, double high){
        super(low,high);
        this.decode = decode;
    }

    public int decode(){
        return decode;
    }

    public void setDecode(int decode){
        this.decode = decode;
    }

    public void set(int decode, double low, double high){
        this.decode = decode;
        super.set(low, high);
    }
}
