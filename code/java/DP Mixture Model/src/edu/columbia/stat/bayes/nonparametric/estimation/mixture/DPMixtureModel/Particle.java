/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel;
import java.util.ArrayList;
import java.util.HashSet ;

/**
 *
 * @author nicholasbartlett
 */
public class Particle extends ArrayList<HashSet<Integer>> {
    public Particle(Integer j) {
        //initialize particle by creating a single table with integer j sitting
        //at it
        super() ;
        HashSet<Integer> tableToAdd = new HashSet<Integer>(2) ;
        tableToAdd.add(j) ;
        add(tableToAdd) ;
    }

    public Particle copy(){
        Particle returnVal = new Particle(0) ;
        returnVal.clear();
        for(HashSet<Integer> table:this){
            HashSet<Integer> tableToAdd = new HashSet<Integer>(table.size()) ;
            for(Integer cust:table){
                tableToAdd.add(new Integer(cust)) ;
            }
            returnVal.add(tableToAdd) ;
        }
        return returnVal ;
    }
}
