/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.fdhpyp;

/**
 *
 * @author nicholasbartlett
 */
public class MutableDouble {
    private double d = 0.0;

    public MutableDouble(double d){
        this.d = d;
    }

    public double set(double newD){
        return (d = newD);
    }

    public double doubleVal(){
        return d;
    }
    
    public void print(){
        System.out.println(d);
    }

    public static void  main(String[] args){
        MutableDouble md = new MutableDouble(1);
        md.print();

        md.set(30);
        md.print();
    }
}
