/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel;

/**
 *
 * @author nicholasbartlett
 */
public interface RestaurantInterface {
    //Method to return an ArrayList of the observations at each table
    java.util.ArrayList obsAtTables() ;

    //Method to find index of table at which individual j is sitting
    int indexTable(int t) ;

    //Method to add table
    void addTable() ;

    //Method to remove table
    void removeTable(int t) ;

    //Method to seat person j at table t
    void seatAtTable(int j, double obs, int t) ;

    //Method to remove person j from table t ;
    void removeFromTable(int j, int t) ;

}
