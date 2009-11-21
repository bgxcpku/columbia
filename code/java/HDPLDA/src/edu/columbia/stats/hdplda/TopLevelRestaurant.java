/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;
import java.util.HashMap ;
import java.util.HashSet ;

/**
 *
 * @author nicholasbartlett
 */
public class TopLevelRestaurant {
    public HashMap<DiscreteDistrib,Double> betaMap;
    public HashMap<DiscreteDistrib,HashSet<TableLabel>> tableMap ;
    public HashMap<DiscreteDistrib,HashSet<Word>> phiMap ;

    //constructor
    
    //method to return prob of sitting at new table and potential phi at which
    //you sat
    public Pair<Double,DiscreteDistrib> getMeasure(int word){return null ;}

    //method to return map of active discrete distributions to current beta value
    public HashMap<DiscreteDistrib,Double> getBetaMap(){
        return betaMap ;
    }

    //method to return map of active discrete distributions to tables of tabel
    //labels
    public HashMap<DiscreteDistrib,HashSet<TableLabel>> getTableMap() {
        return tableMap ;
    }
    
}
