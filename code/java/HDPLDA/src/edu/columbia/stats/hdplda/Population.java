/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;
import java.util.HashSet ;

/**
 *
 * @author nicholasbartlett
 */
public class Population extends HashSet  {
    public Document sufficientStatisic;

    //method to initizialize a population with an index and a multinomial/
    //BagOfWords observation.
    public Population(Integer obsIndex, Document obs){
        sufficientStatisic = new Document(obs.value.length) ;
        add(obsIndex,obs) ;
    }

    //overiding method to add observations to the population
    public boolean add(Integer obsIndex, Document obs) {
        boolean returnVal ;
        returnVal = super.add(obsIndex) ;
        if(obs.value.length == sufficientStatisic.value.length){
            for(int j=0; j<obs.value.length; ++j){
                sufficientStatisic.value[j] += obs.value[j] ;
            }
        } else returnVal = false ;
        return returnVal ;
    }

    //overiding method to remove observations from the population
    public boolean remove(Integer obsIndex, Document obs) {
        boolean returnVal ;
        returnVal = super.remove(obsIndex) ;
        if(obs.value.length == sufficientStatisic.value.length){
            for(int j=0; j<obs.value.length; ++j){
                sufficientStatisic.value[j] -= obs.value[j] ;
            }
        } else returnVal = false ;
        return returnVal ;
    }
}
