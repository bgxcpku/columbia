/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DPMixtureModel;

/**
 *
 * @author nicholasbartlett
 */

public class Table {
    //number of people at this given table
    public int n ;
    private java.util.HashMap obs ;
    private Object rc ;

    //constructor
    public Table(){
        this.n = 0 ;
        obs = new java.util.HashMap() ;
    }

    //method to sit a person at the table
    public void sitAtTable(int j, double observation){
        // increment n
        ++n ;
        //add person to table
        this.rc = this.obs.put(j ,observation) ;
    }

    //method to remove a person from the table
    public void removeFromTable(int j){
        if(this.obs.containsKey(j)) {
            --n ;
            this.rc = this.obs.remove(j) ;
        }
        else System.out.println("THAT PERSON IS NOT SITTING AT THIS TABLE");
    }

    //method to get value of individual at table
    public Object getValue(int j){
        this.rc = this.obs.get(j) ;
        if(this.rc == null) System.out.println("PERSON NO SITTING AT THIS TABLE") ;
        return this.rc ;
    }

    //method to see people at the table ;
    public Object getPeople(){
        this.rc = this.obs.keySet() ;
        return this.rc ;
    }

    //method to return all values at this table
    public Object getValues(){
        this.rc = this.obs.values() ;
        return this.rc ;
    }
}
