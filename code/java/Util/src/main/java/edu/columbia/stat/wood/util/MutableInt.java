/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

/**
 *
 * @author nicholasbartlett
 */
public class MutableInt {
    private int i;

    public MutableInt(int value){
        i = value;
    }

    public void set(int value){
        i = value;
    }

    public int intValue(){
        return i;
    }

}
