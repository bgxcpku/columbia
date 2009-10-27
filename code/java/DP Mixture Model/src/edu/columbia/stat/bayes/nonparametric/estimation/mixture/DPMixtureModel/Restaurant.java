/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel;

/**
 *
 * @author nicholasbartlett
 */

public class Restaurant implements RestaurantInterface {
    public int numTables ;
    public java.util.ArrayList numAtTables ;

    private java.util.ArrayList tables ;
    private Object rc = new Object() ;

    public Restaurant(){
        numTables = 0 ;
        this.tables = new java.util.ArrayList() ;
        this.numAtTables = new java.util.ArrayList() ;

    }

    //Method to add table
    public void addTable() {
        this.rc = tables.add(new java.util.HashMap()) ;
        this.rc = numAtTables.add(0) ;
        //++numTables ;
        this.numTables = tables.size() ;
    }

    //Method to remove table
    public void removeTable(int t){
        this.rc = tables.remove(t) ;
        this.rc = numAtTables.remove(t) ;
        --numTables ;
    }
    
    //Method to return an ArrayList of the observations at each table
    public java.util.ArrayList obsAtTables(){
        java.util.ArrayList obsAtTables = new java.util.ArrayList() ;
        for(int t=0; t < this.numTables;++t){
            java.util.HashMap thisTable = (java.util.HashMap)tables.get(t) ;
            obsAtTables.add(thisTable.values()) ;
        }
        return obsAtTables ;
    }

    //Method to find index of table at which individual j is sitting
    public int indexTable(int j) {
        int table = 0 ;
        for(int t=0; t < this.numTables;++t){
            java.util.HashMap thisTable = (java.util.HashMap)tables.get(t) ;
            boolean ist = thisTable.containsKey(j) ;
            if(ist) return t ;
        }
        System.out.println("COULD NOT FIND INDIVIDUAL AT ANY TABLE") ;
        return 0 ;
    }

    //Method to seat person j at table t
    public void seatAtTable(int j, double obs, int t){
        java.util.HashMap thisTable = (java.util.HashMap)tables.get(t) ;
        this.rc = tables.remove(t) ;
        this.rc = thisTable.put(j, obs) ;
        tables.add(t,thisTable) ;
    }

    //Method to remove person j from table t ;
    public void removeFromTable(int j, int t){
        java.util.HashMap thisTable = (java.util.HashMap)tables.get(t) ;
        this.rc = tables.remove(t) ;
        this.rc = thisTable.remove(j) ;
        tables.add(t,thisTable) ; 
    }

}
