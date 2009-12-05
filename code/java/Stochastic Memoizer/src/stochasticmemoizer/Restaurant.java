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

    public Restaurant(Restaurant parentRestaurant, int[] parentPath, double discount) {
        super();
        state = new HashMap<Integer, int[]>();
        /*first column is total number of customers observed in this restaurant
         * with the given type, second column is number of unique tables of this
         * type */
        this.parentRestaurant = parentRestaurant;
        this.parentPath = parentPath;
        this.discount = discount;
    }

    public int getNumberCustAtType(Integer tableType) {
        int returnVal = 0;
        for (int j = 0; j < state.get(tableType).length; ++j) {
            returnVal += state.get(tableType)[j];
        }
        return returnVal;
    }

    public int getNumberCustAtRest() {
        int returnVal = 0;
        for (Integer j : state.keySet()) {
            returnVal += this.getNumberCustAtType(j);
        }
        return returnVal;
    }

    public int getNumberTablesAtRest() {
        int returnVal = 0;
        for (Integer j : state.keySet()) {
            returnVal += state.get(j).length;
        }
        return returnVal;
    }

    public void addNewTable(Integer type) {
        if (state.get(type) != null) {
            int[] tableStructureToAdd = new int[state.get(type).length + 1];
            for (int j = 0; j < state.get(type).length; ++j) {
                tableStructureToAdd[j] = state.get(type)[j];
            }
            tableStructureToAdd[state.get(type).length] = 1;
            state.put(type, tableStructureToAdd);
        } else {
            int[] tableStructureToAdd = new int[1];
            tableStructureToAdd[0] = 1;
            state.put(type, tableStructureToAdd);
        }
    }

    public void addCustToExistingTable(Integer type) {
        if (state.get(type) != null) {
            //decide which table to add to stochastically
            double totalWeight = 1.0 * this.getNumberCustAtType(type) - state.get(type).length * discount;
            double rawRandomSample = Math.random();

            double cumSum = 0;
            for (int j = 0; j < state.get(type).length; ++j) {
                cumSum += 1.0 * (state.get(type)[j] - discount) / totalWeight;
                if (cumSum > rawRandomSample) {
                    ++state.get(type)[j];
                    break;
                }
            }
        } else {
            throw new RuntimeException("Cannot add customer to an existing " +
                    "table if this type of observation has never been " +
                    "observed in this restaurant");
        }
    }

    public void deleteCustFromRest() {
        double totalWeight = this.getNumberCustAtRest();
        double rawRandomSample = Math.random();

        Integer typeToLooseCust = 0;
        int tableToLooseCust = 0;

        double cumSum = 0.0;
        //loop through all types of tables
        topFor:
        for (Integer type : state.keySet()) {
            //for each typ, loop through tables of that type
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

    /*method to reconfigure restaurant given a new discount parameter with the
     * assumptiont that a new restaurant is being instated between the parent
     * restaurant and this restaurant*/
    /*
    public void reconfigureRest(double newDiscount) {

        double concentration = -1.0 * discount;
        Collection<Integer> currentKeySet = state.keySet();

        for (Integer type : currentKeySet) {
            ArrayList<Integer> newTableStructure = new ArrayList<Integer>();
            for (int t = 0; t < state.get(type).length; ++t) {
                int toSeat = state.get(type)[t];
                ArrayList<Integer> brokenTable = new ArrayList<Integer>(2);
                brokenTable.add(new Integer(1));
                --toSeat;
                double totalWeight = 1.0 + concentration;
                while (toSeat > 0) {
                    double rawRandomSample = Math.random();
                    double cumSum = 0;
                    for (int j = 0; j < brokenTable.size(); ++j) {
                        cumSum += 1.0 * (brokenTable.get(j) - newDiscount) / totalWeight;
                        if (cumSum > rawRandomSample) {
                            brokenTable.set(j, brokenTable.get(j) + 1);
                            break;
                        }
                    }
                    if (cumSum <= rawRandomSample) {
                        brokenTable.add(new Integer(1));
                    }
                    ++totalWeight;
                    --toSeat;
                }
                newTableStructure.addAll(brokenTable);
            }
            int[] newTableStuctureArray = new int[newTableStructure.size()];
            for (int j = 0; j < newTableStuctureArray.length; ++j) {
                newTableStuctureArray[j] = newTableStructure.get(j).intValue();
            }
            state.put(type, newTableStuctureArray);
        }
        
        discount = newDiscount ;
    } */

    /* this method is to take a path such that we have a top restaurant and a bottom
     * restaurant which is the top restaurant's child and we wish to insert an
     * intermediate restaurant.  We will call the top restaurant rest 1 in the
     * comments, and bottom restaurant rest 3 and the intermediate restaurant
     * rest 2. This method is placed in restaurant 3, that is, the assumption is
     * that the intermediate restaurant goes between this restaurant and the current
     * parent restaurant */
    public Restaurant reconfigureRestaurantReturnIntermediateRestaurant(int[] rest2ParentPath, double rest2Discount) {        
        //update the discount for rest3
        this.discount /= rest2Discount ;

        //create rest 2
        Restaurant rest2 = new Restaurant(this.parentRestaurant, rest2ParentPath,rest2Discount) ;

        //update the child reference for rest 1 to point to rest 2
        rest2.parentRestaurant.put(rest2ParentPath[rest2ParentPath.length - 1], rest2) ;

        //update the parent path and parent restaurant field for rest 3 to indicate
        //that rest 2 is now it's parent restaurant.
        this.parentRestaurant = rest2 ;
        int[] newParentPath = new int[this.parentPath.length - rest2ParentPath.length] ;
        for(int j = 0 ; j<newParentPath.length; ++j){
            newParentPath[j] = this.parentPath[j] ;
        }
        this.parentPath = newParentPath ;

        //update rest 2 so that rest 3 is a/the child rest
        rest2.put(this.parentPath[this.parentPath.length -1], this) ;

        //initiate the state of rest 2 to the same number of tables per type
        //as rest 3, but with only one customer per table.  This wil get updated
        //as rest 3 is split.
        for(Integer type : state.keySet()){
            int[] newStructure = new int[state.get(type).length] ;
            for(int j = 0 ; j< newStructure.length; ++j){
                newStructure[j] = 1 ;
            }
            rest2.state.put(type,newStructure) ;
        }

        //Set the concentration parameter for the splitting procedure.  The new
        //discount parameter is used for the breaking procedure as the discount
        //parameter, the old discount is used as the concentration paramter,
        //with a negative attached.
        double concentration = -1.0 * discount*rest2Discount;

        //make a static list of the current key set so can make changes to the
        //state as we cycle through the keys
        Collection<Integer> currentKeySet = state.keySet();

        //loop through the key set to break apart tables in rest 3
        for (Integer type : currentKeySet) {
            ArrayList<Integer> newTableStructure = new ArrayList<Integer>();
            for (int t = 0; t < state.get(type).length; ++t) {
                int toSeat = state.get(type)[t];
                ArrayList<Integer> brokenTable = new ArrayList<Integer>(2);
                brokenTable.add(new Integer(1));
                --toSeat;
                double totalWeight = 1.0 + concentration;
                topwhile:
                while (toSeat > 0) {
                    double rawRandomSample = Math.random();
                    double cumSum = 0;
                    for (int j = 0; j < brokenTable.size(); ++j) {
                        cumSum += 1.0 * (brokenTable.get(j) - discount) / totalWeight;
                        if (cumSum > rawRandomSample) {
                            //sit them at an already existing table in the broken set of tables
                            brokenTable.set(j, brokenTable.get(j) + 1);
                            ++totalWeight ;
                            --toSeat ;
                            continue topwhile;
                        }
                    }
                    
                    //add a new table to number of tables this current
                    //tables is being split into and add a customer to
                    //the corresponding spot in rest 2
                    brokenTable.add(new Integer(1));
                    ++rest2.state.get(type)[t] ;
                    
                    ++totalWeight;
                    --toSeat;
                }
                newTableStructure.addAll(brokenTable);
            }
            int[] newTableStuctureArray = new int[newTableStructure.size()];
            for (int j = 0; j < newTableStuctureArray.length; ++j) {
                newTableStuctureArray[j] = newTableStructure.get(j).intValue();
            }
            state.put(type, newTableStuctureArray);
        }
        return rest2 ;
    }

    public void printRestaurantState() {
        for (Integer type : state.keySet()) {
            System.out.println(type);
            System.out.print("[");
            for (int j = 0; j < state.get(type).length; ++j) {
                if (j < state.get(type).length - 1) {
                    System.out.print(state.get(type)[j] + ", ");
                } else {
                    System.out.print(state.get(type)[j] + "]");
                }
            }
            System.out.println();
        }
    }

    public void printRestaurantChildren() {
        for (Integer child : this.keySet()) {
            System.out.print(child + ", ") ;
            System.out.print("[") ;
            for(int j = 0; j<this.get(child).parentPath.length; ++j){
                if(j == 0){
                    System.out.print(this.get(child).parentPath[j]) ;
                } else {
                    System.out.print(", " + this.get(child).parentPath[j]) ;
                }
            }
            System.out.print("]") ;
            System.out.println() ;
        }
    }
}
