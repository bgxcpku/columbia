/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel;

/**
 *
 * @author nicholasbartlett
 */
public class BagOfWordsObservation implements ObservationInterface {
    
    public int[] value ;
    public BagOfWordsObservation(int[] obs){
        value = obs ;
        checkValue() ;
    }

    public BagOfWordsObservation(int m){
        int[] zeroArray = new int[m] ;
        for(int j=0; j<m; ++j){
            zeroArray[j] = 0 ;
        }
        value = zeroArray ;
        checkValue() ;
    }

    public void plus(BagOfWordsObservation obsToAdd) {
        for(int j = 0; j<value.length; ++j){
            value[j] += obsToAdd.value[j] ;
        }
    }

    public void minus(BagOfWordsObservation obsToAdd) {
        for(int j = 0; j<value.length; ++j){
            value[j] -= obsToAdd.value[j] ;
        }
        checkValue() ;
    }

    private void checkValue() {
        for(int j = 0; j<value.length; ++j){
            if(value[j]<0){
                throw new RuntimeException("Illegal BagOfWordsObservation value " +
                        "cannot have negative values") ;
            }
        }
    }
}
