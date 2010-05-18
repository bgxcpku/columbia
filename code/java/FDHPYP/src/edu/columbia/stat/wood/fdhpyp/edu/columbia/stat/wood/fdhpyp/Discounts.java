/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.fdhpyp;

/**
 *
 * @author nicholasbartlett
 */
public class Discounts {
    private MutableDouble[] discounts;
    
    public Discounts(double[] ds){
        discounts = new MutableDouble[ds.length];
        
        for(int i = 0; i<ds.length;i++){
            discounts[i] = new MutableDouble(ds[i]);
        }
    }
    
    public Discounts() {
        this(new double[]{0.5});
    }

    public MutableDouble get(int d){
        if(d<discounts.length){
            return discounts[d];
        } else {
            return discounts[discounts.length-1];
        }
    }

    public boolean inRange() {
        for(MutableDouble d:discounts){
            if(d.doubleVal()<= 0 || d.doubleVal()>=1){
                return false;
            }
        }
        return true;
    }

    public boolean inRange(int i){
        if(discounts[i].doubleVal() <= 0 || discounts[i].doubleVal() >= 1){
            return false;
        }
        return true;
    }

    public int length(){
        return discounts.length;
    }

    public void print(){
        System.out.print("[" + discounts[0].doubleVal());
        for(int i = 1; i<discounts.length; i++){
            System.out.print(", " + discounts[i].doubleVal());
        }
        System.out.println("]");
    }
}
