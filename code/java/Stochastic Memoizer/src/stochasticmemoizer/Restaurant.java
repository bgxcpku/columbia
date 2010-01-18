/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stochasticmemoizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 * This class is an extension of a HashMap.  The key is an integer, which denotes
 * the most recent context, given that eleents in the restaurant already have a 
 * certain shared context.  The key maps to children restaurant, however those
 * children restaurants may well be some "distance" away.  The actual path is
 * detailed in the child restaurant as parentPath.
 * 
 */
public class Restaurant extends HashMap<Integer, Restaurant> {

    public double discount;
    public HashMap<Integer, int[]> state;
    public Restaurant parentRestaurant;
    public int[] parentPath;

    /* constructor: In order to create a new restaurant, you must feed it the
     * parent restaurant, the path to the parent, and the discount parameter.
     * the only restaurant without a parent or parentPath is the context free
     * restaurant */
    public Restaurant(Restaurant parentRestaurant, int[] parentPath, double discount) {
        super(1);
        this.state = new HashMap<Integer, int[]>(1);
        this.parentRestaurant = parentRestaurant;
        this.parentPath = parentPath;
        this.discount = discount;
    }

    /* method to return number of customers observed or inferred in the given
     * restaurant of a certain type */
    public int getNumberCustAtType(Integer type) {
        int returnVal = 0;
        int[] typeTableStructure = state.get(type) ;

        if(typeTableStructure == null) return returnVal ;

        for (int j = 0; j < typeTableStructure.length; j++) {
            returnVal += typeTableStructure[j];
        }
        return returnVal;
    }

    /* method to return number of customers observed or inferred to be at this
     * restaurant */
    public int getNumberCustAtRest() {
        int returnVal = 0;
        for (Integer j : state.keySet()) {
            returnVal += this.getNumberCustAtType(j);
        }
        return returnVal;
    }

    /* method to return the number of tables inferred to be at this restaurant */
    public int getNumberTablesAtRest() {
        int returnVal = 0;
        for (int[] j : state.values()) {
            returnVal += j.length;
        }
        return returnVal;
    }

    /* method to seat a customer of a given type at a new table in this
     * restaurant */
    public void addNewTable(Integer type){
        int[] typeTableStructure = state.get(type) ;
        if(typeTableStructure != null){
            int[] tableStructureToAdd = new int[typeTableStructure.length + 1] ;
            System.arraycopy(typeTableStructure, 0, tableStructureToAdd, 0, typeTableStructure.length) ;
            tableStructureToAdd[typeTableStructure.length] = 1;
            state.put(type, tableStructureToAdd);
        } else {
            int[] tableStructureToAdd = new int[1];
            tableStructureToAdd[0] = 1;
            state.put(type, tableStructureToAdd);
        }
    }

    /* method to sit a new customer of a given type at an existing table.  This
     * algorithm is stochastic and decides which table to sit them at given
     * the number of customers already at each table and the restaurant discount
     * parameter */
    public void addCustToExistingTable(Integer type) {
        int[] typeTableStructure = state.get(type) ;
        if (typeTableStructure != null) {
            //decide which table to add to stochastically
            double totalWeight = 1.0 * this.getNumberCustAtType(type) - typeTableStructure.length * discount;
            double rawRandomSample = Math.random();

            double cumSum = 0;
            for (int j = 0; j < typeTableStructure.length; j++) {
                cumSum += 1.0 * (typeTableStructure[j] - discount) / totalWeight;
                if (cumSum > rawRandomSample) {
                    typeTableStructure[j]++;
                    break;
                }
            }
        } else {
            throw new RuntimeException("Cannot add customer to an existing " +
                    "table if this type of observation has never been " +
                    "observed in this restaurant");
        }
    }

    /* method to delete a random customer from the restaurant */
    public void deleteCustFromRest() {
        double totalWeight = this.getNumberCustAtRest();
        double rawRandomSample = Math.random();

        Integer typeToLooseCust = 0;
        int tableToLooseCust = 0;

        double cumSum = 0.0;
        //loop through all types of tables
        topFor:
        for (Integer type : state.keySet()) {
            //for each type, loop through tables of that type
            for (int j = 0; j < state.get(type).length; ++j) {
                cumSum += 1.0 * state.get(type)[j] / totalWeight;
                //if deemed that the table should loose a customer, handle
                //appropriately below.
                if (cumSum > rawRandomSample) {
                    typeToLooseCust = type;
                    tableToLooseCust = j;
                    break topFor;
                }
            }
        }

        if (state.get(typeToLooseCust)[tableToLooseCust] == 1) {
            if (state.get(typeToLooseCust).length == 1) {
                state.remove(typeToLooseCust);
            } else {
                int[] newTableStructure = new int[state.get(typeToLooseCust).length - 1];
                for (int k = 0; k < state.get(typeToLooseCust).length - 1; ++k) {
                    if (k < tableToLooseCust) {
                        newTableStructure[k] = state.get(typeToLooseCust)[k];
                    } else {
                        newTableStructure[k] = state.get(typeToLooseCust)[k + 1];
                    }
                }
                state.put(typeToLooseCust, newTableStructure);
            }
        } else {
            --state.get(typeToLooseCust)[tableToLooseCust];
        }
    }
    
    /* this method is to take a path such that we have a top restaurant and a bottom
     * restaurant which is the top restaurant's child and we wish to insert an
     * intermediate restaurant.  We will call the top restaurant rest 1 in the
     * comments, and bottom restaurant rest 3 and the intermediate restaurant
     * rest 2. This method is placed in restaurant 3, that is, the assumption is
     * that the intermediate restaurant goes between this restaurant and the current
     * parent restaurant */
    public Restaurant reconfigureRestaurantReturnIntermediateRestaurant(int[] rest2ParentPath, double rest2Discount) {
        //update the discount for rest3
        this.discount /= rest2Discount;

        //create rest 2
        Restaurant rest2 = new Restaurant(this.parentRestaurant, rest2ParentPath, rest2Discount);

        //update the child reference for rest 1 to point to rest 2
        rest2.parentRestaurant.put(rest2ParentPath[rest2ParentPath.length - 1], rest2);

        //update the parent path and parent restaurant field for rest 3 to indicate
        //that rest 2 is now it's parent restaurant.
        this.parentRestaurant = rest2;
        int[] newParentPath = new int[this.parentPath.length - rest2ParentPath.length];

        System.arraycopy(parentPath, 0, newParentPath, 0, newParentPath.length) ;
        this.parentPath = newParentPath;

        //update rest 2 so that rest 3 is a/the child rest
        rest2.put(this.parentPath[this.parentPath.length - 1], this);

        //initiate the state of rest 2 to the same number of tables per type
        //as rest 3, but with only one customer per table.  This wil get updated
        //as rest 3 is split.
        for (Integer type : state.keySet()) {
            int[] newStructure = new int[state.get(type).length];
            for (int j = 0; j < newStructure.length; j++) {
                newStructure[j] = 1;
            }
            rest2.state.put(type, newStructure);
        }

        //Set the concentration parameter for the splitting procedure.  The new
        //discount parameter is used for the breaking procedure as the discount
        //parameter, the old discount is used as the concentration paramter,
        //with a negative attached.
        double concentration = -1.0 * discount * rest2Discount;

        //make a static list of the current key set so can make changes to the
        //state as we cycle through the keys
        Collection<Integer> currentKeySet = state.keySet();

        //loop through the key set to break apart tables in rest 3
        for (Integer type : currentKeySet) {
            ArrayList<Integer> newTableStructure = new ArrayList<Integer>(); //FIX initiate to something reasonable, like current size plus some
            //FIX same damn thing
            for (int t = 0; t < state.get(type).length; t++) {
                int toSeat = state.get(type)[t];
                ArrayList<Integer> brokenTable = new ArrayList<Integer>(2);
                brokenTable.add(new Integer(1));
                toSeat--;
                double totalWeight = 1.0 + concentration;
                topwhile:
                while (toSeat > 0) {
                    double rawRandomSample = Math.random();
                    double cumSum = 0;
                    for (int j = 0; j < brokenTable.size(); j++) {
                        cumSum += 1.0 * (brokenTable.get(j) - discount) / totalWeight;
                        if (cumSum > rawRandomSample) {
                            //sit them at an already existing table in the broken set of tables
                            brokenTable.set(j, brokenTable.get(j) + 1);
                            totalWeight++;
                            toSeat--;
                            continue topwhile;
                        }
                    }

                    //add a new table to number of tables this current
                    //tables is being split into and add a customer to
                    //the corresponding spot in rest 2
                    brokenTable.add(new Integer(1));
                    rest2.state.get(type)[t]++;

                    totalWeight++;
                    toSeat--;
                }
                newTableStructure.addAll(brokenTable);
            }
            //FIX use toArray method
            int[] newTableStuctureArray = new int[newTableStructure.size()];
            for (int j = 0; j < newTableStuctureArray.length; ++j) {
                newTableStuctureArray[j] = newTableStructure.get(j).intValue();
            }
            state.put(type, newTableStuctureArray);
        }
        return rest2;
    }

    public void printRestaurantState() {
        for (Integer type : state.keySet()) {
            System.out.print(type);
            System.out.print("[");
            for (int j = 0; j < state.get(type).length; ++j) {
                if (j < state.get(type).length - 1) {
                    System.out.print(state.get(type)[j] + ", ");
                } else {
                    System.out.print(state.get(type)[j] + "]");
                }
            }
            System.out.print(", ");
        }
        System.out.println() ;
    }

    public void printRestaurantChildren() {
        for (Integer child : this.keySet()) {
            System.out.print(child + ", ");
            System.out.print("[");
            for (int j = 0; j < this.get(child).parentPath.length; ++j) {
                if (j == 0) {
                    System.out.print(this.get(child).parentPath[j]);
                } else {
                    System.out.print(", " + this.get(child).parentPath[j]);
                }
            }
            System.out.print("]");
            System.out.println();
        }
    }

    public void printRestaurantParentPath(){
        System.out.print("[") ;
        if(this.parentPath != null){
        for(int j = 0 ; j< this.parentPath.length-1; j++){
            System.out.print(this.parentPath[j] + ", ") ;
        }
        System.out.println(this.parentPath[this.parentPath.length-1] + "]") ;
        } else System.out.println("]") ;
    }
}
