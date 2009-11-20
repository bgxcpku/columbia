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
public class SetableDouble implements Serializable {
    private double value;

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
    
    public SetableDouble(double d) {
        this.value = d;
    }

}
