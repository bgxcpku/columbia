/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public class SetableInteger {

    private int value ;

    public SetableInteger(int value){
        this.value = value ;
    }

    public void setValue(int newValue){
        this.value = newValue ;
    }

    public void increment(){
        this.value++ ;
    }

    public void decrement(){
        this.value-- ;
    }

    public int getVal(){
        return this.value ;
    }

}
