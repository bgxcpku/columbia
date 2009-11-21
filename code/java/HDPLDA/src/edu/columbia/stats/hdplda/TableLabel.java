/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;

/**
 *
 * @author nicholasbartlett
 */
public class TableLabel {
    private DiscreteDistrib value ;
    public TableLabel(DiscreteDistrib value){
        this.value = value ;
    }
    public void set(DiscreteDistrib newVal){
        value = newVal ;
    }
    public DiscreteDistrib get(){
        return value ;
    }
}
