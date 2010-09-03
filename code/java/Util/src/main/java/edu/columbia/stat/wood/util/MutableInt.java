/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import java.io.Serializable;

/**
 * Container class for int which allows the underlying value to be set.
 * @author nicholasbartlett
 */

public class MutableInt implements Serializable{

    static final long serialVersionUID = 1;

    private int i;

    /**
     * @param value value of underlying int
     */
    public MutableInt(int value){
        i = value;
    }

    /**
     * Allows the underlying value to be set.
     * @param value int value to assign this object
     */
    public void set(int value){
        i = value;
    }

    /**
     * Gets the int value of this object.
     * @return int value of this object
     */
    public int intValue(){
        return i;
    }
}
