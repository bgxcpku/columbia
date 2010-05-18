/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.fdhpyp;

/**
 *
 * @author nicholasbartlett
 */
public class Concentrations {
    private MutableDouble[] concentrations;

    public Concentrations(double[] cs){
        concentrations = new MutableDouble[cs.length];
        for(int i = 0; i<cs.length; i++){
            concentrations[i] = new MutableDouble(cs[i]);
        }
    }

    public Concentrations(){
        this(new double[]{0.001});
    }

    public MutableDouble get(int d){
        if(d<concentrations.length){
            return concentrations[d];
        } else {
            return concentrations[concentrations.length-1];
        }
    }

    public boolean inRange(){
        for(MutableDouble c:concentrations){
            if(c.doubleVal()<=0){
                return false;
            }
        }
        return true;
    }

    public int length(){
        return concentrations.length;
    }

}
