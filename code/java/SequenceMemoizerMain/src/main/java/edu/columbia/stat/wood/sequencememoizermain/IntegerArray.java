/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizermain;

import java.util.Arrays;

/**
 *
 * @author nicholasbartlett
 */
public class IntegerArray {
    private int[] value;

    public IntegerArray(int[] value){
        this.value = value;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final IntegerArray other = (IntegerArray) obj;
        if (!Arrays.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }
}
