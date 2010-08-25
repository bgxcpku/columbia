/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

/**
 *
 * @author nicholasbartlett
 */
public class MutableByte {
    private int b;

    public MutableByte(int value){
        b = value;
    }

    public int byteValue(){
        return b;
    }

    public void set(int value){
        b = value;
    }
}
