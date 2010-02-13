/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.ldadhpyplm;

/**
 *
 * @author nicholasbartlett
 */
public class BagOfWords implements ObservationInterface {
    
    public int[] value ;
    public BagOfWords(int[] obs){
        value = obs ;
        checkValue() ;
    }

    public BagOfWords(int m){
        int[] zeroArray = new int[m] ;
        for(int j=0; j<m; ++j){
            zeroArray[j] = 0 ;
        }
        value = zeroArray ;
        checkValue() ;
    }
    
    public int get(int i) {
        return value[i];
    }

    public void plus(BagOfWords obsToAdd) {
        for(int j = 0; j<value.length; ++j){
            value[j] += obsToAdd.value[j] ;
        }
    }

    public void minus(BagOfWords obsToAdd) {
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
