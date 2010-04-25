/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.finitedepthhpyp;

import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */
public abstract class Restaurant extends HashMap<Integer, Restaurant> {

    public static int numberRest = 0;

    public Restaurant get(Integer childKey){
        return super.get(childKey);
    }
    
    @Override
    public Restaurant put(Integer childKey, Restaurant child){
        return super.put(childKey, child);
    }

    public abstract Restaurant getParent();
    public abstract boolean hasNoCustomers();

    //method to retun an array of length four
    //entry 0 : total number of customers of specified type
    //entry 1 : total tables of customers of specified type
    //entry 2 : total customers in restaurant
    //entry 3 : total number of tables in restaurant
    public abstract void getRestCounts(int type, int[] counts);
    public abstract Restaurant sitAtRest(int type, double probUpper, double discount, double concentration);
    public abstract void fillPredictiveCounts(double discount, double concentration, PredictiveCounts pc);
    public abstract double getLogLik(double discount, double concentration);
}
