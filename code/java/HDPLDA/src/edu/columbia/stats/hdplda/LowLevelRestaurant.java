/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;
import java.util.HashMap ;
import java.util.HashSet;

/**
 *
 * @author nicholasbartlett
 */
public class LowLevelRestaurant extends HashMap<TableLabel,HashSet<Word>> {
    public HashMap<Integer,TableLabel> inverseMap = null ;
    public TopLevelRestaurant topLevelRestaurant ;

    public LowLevelRestaurant(TopLevelRestaurant topLevelRestaurant){
        super() ;
        this.topLevelRestaurant = topLevelRestaurant ;
    }
    
    //method to remove word
    public void removeWord(int word){
    }

    //method to add word
    public void addWord(){}

}
