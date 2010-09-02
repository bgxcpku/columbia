/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import java.io.Serializable;

/**
 *
 * @author nicholasbartlett
 */

public class MutableInt implements Serializable{

    static final long serialVersionUID = 1;

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
