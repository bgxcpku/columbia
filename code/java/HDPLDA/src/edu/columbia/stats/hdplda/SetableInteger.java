/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;

import java.io.Serializable;

/**
 *
 * @author fwood
 */
public class SetableInteger implements Serializable {
    private int value;

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void add(SetableInteger s) {
        this.value += s.getValue();
    }

     public void decrement() {
        value--;
    }


    public void increment() {
        value++;
    }

    public SetableInteger(int d) {
        this.value = d;
    }

}
