/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stochasticmemoizer3;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */

public class Restaurant extends HashMap<Integer, Restaurant> {

    public double logDiscount;
    public int[][] state;
    public Restaurant parent;
    public int[] parentPath;
    public int mostRecentTimeUsed = 0 ;
    public static int numberRest = 0;

    public Restaurant(Restaurant parent, int[] parentPath, double logDiscount, int depth) {
        super(2); //FIX this should be more smart, initialization should be based on depth
        this.parent = parent;
        this.parentPath = parentPath;
        this.logDiscount = logDiscount;
        numberRest++;
    }

    //method to update the most recent time used
    public void updateMostRecentTimeUsed(int j){
        mostRecentTimeUsed = j ;
    }
    
    //method to return the number of tables in the restaurant
    public int getNumberOfTables() {
        int numberOfTables = 0;

        if (state != null) {
            for (int j = 0; j < state.length; j++) {
                numberOfTables += state[j].length - 1;
            }
        }
        return numberOfTables;
    }

    //method to return the number of people in the restaurant
    public int getNumberOfCustomers() {
        int numberOfCustomers = 0;
        for (int typeIndex = 0; typeIndex < state.length; typeIndex++) {
            for (int table = 1; table < state[typeIndex].length; table++) {
                numberOfCustomers += state[typeIndex][table];
            }
        }
        return numberOfCustomers;
    }

    //method to retun an array of length four
    //entry 0 : total number of customers of specified type
    //entry 1 : total tables of customers of specified type
    //entry 2 : total customers in restaurant
    //entry 3 : total number of tables in restaurant
    public int[] getRestCounts(int type) {
        int custOfType = 0;
        int tablesOfType = 0;
        int totalCust = 0;
        int totalTables = 0;

        if (state != null) {
            for (int j = 0; j < state.length; j++) {
                if (type == state[j][0]) {
                    for (int i = 1; i < state[j].length; i++) {
                        custOfType += state[j][i];
                        totalCust += state[j][i];
                    }
                    tablesOfType = state[j].length - 1;
                    totalTables += state[j].length - 1;
                } else {
                    for (int i = 1; i < state[j].length; i++) {
                        totalCust += state[j][i];
                    }
                    totalTables += state[j].length - 1;
                }
            }
        }

        int[] restCounts = new int[4];
        restCounts[0] = custOfType;
        restCounts[1] = tablesOfType;
        restCounts[2] = totalCust;
        restCounts[3] = totalTables;

        return restCounts;
    }

    //method to add a child rest in restaurant hashmap
    //the argument seq is the entire sequence being seated
    public void putChild(Restaurant child, int[] seq) {
        int newKeyIndex = child.parentPath[1] - 1;
        Integer newKey = new Integer(seq[newKeyIndex]);
        this.put(newKey, child);
    }

    /* this method sits a type at a given restaurant and returns a value of true
     * or false.  true indicates that the table the type was seated at was drawn
     * from the upper level restaurant/distribution and thus a customer must be
     * also seated in the upper level restaurant.  the method takes as its
     * arguements, a type to sit, and a probability value corresponding to the
     * proportional probability of drawing from the upper level restaurant. This
     * probability is calculated on the way down the tree and thus represents
     * a cumulative effect of the succession of parent nodes. */
    public boolean sitAtRest(int type, double probUpper) {
        //if no one is sitting in restaurant
        if (state == null) {
            state = new int[1][2];
            state[0][0] = type;
            state[0][1] = 1;
            return true;
        }

        //else loop through state to see if type is already represented
        for (int j = 0; j < state.length; j++) {
            if (state[j][0] == type) {
                int numCustAtType = 0;
                int numTablesAtType = state[j].length - 1;
                for (int i = 1; i < state[j].length; i++) {
                    numCustAtType += state[j][i];
                }

                double cumSum = 0.0;
                double rawRandomSample = Math.random();
                double totalWeight = 1.0 * (numCustAtType - Math.exp(Math.log(numTablesAtType) + logDiscount)) + Math.exp(Math.log(this.getNumberOfTables()) + logDiscount + Math.log(probUpper));

                for (int i = 1; i < state[j].length; i++) {
                    cumSum += (state[j][i] - Math.exp(logDiscount)) / totalWeight;
                    if (cumSum > rawRandomSample) {
                        state[j][i]++;
                        return false;
                    }
                }

                int[] newTypeTableStructure = new int[state[j].length + 1];
                System.arraycopy(state[j], 0, newTypeTableStructure, 0, state[j].length);
                newTypeTableStructure[state[j].length] = 1;
                state[j] = newTypeTableStructure;
                return true;
            }
        }

        //if not found then will need to create a row for this type
        int[][] newState = new int[state.length + 1][];
        System.arraycopy(state, 0, newState, 0, state.length);

        newState[state.length] = new int[2];
        newState[state.length][0] = type;
        newState[state.length][1] = 1;

        state = newState;
        return true;
    }

    public Restaurant reconfigureRestaurantReturnIntermediateRestaurant(int[] rest2ParentPath, double logRest2Discount, int rest2Depth, int[] seq) {
        //update rest3 discount
        logDiscount -= logRest2Discount;

        //create rest2
        Restaurant rest2 = new Restaurant(parent, rest2ParentPath, logRest2Discount, rest2Depth);

        //update the child reference for rest1 to point to rest2 as it's child
        parent.putChild(rest2, seq);

        //update the parent path for rest3
        parentPath[1] -= (rest2.parentPath[1] - rest2.parentPath[0]);

        //update rest 2 so that rest 3 is a/the child rest
        rest2.putChild(this, seq);

        //update the parent of rest3 to be rest2
        parent = rest2;

        //instantiate the state of rest 2
        rest2.state = new int[state.length][];

        //Set the concentration parameter for the splitting procedure.  The new
        //discount parameter is used for the breaking procedure as the discount
        //parameter, the old discount is used as the concentration paramter,
        //with a negative attached.
        double concentration = -1.0 * Math.exp(logDiscount + logRest2Discount);

        //now do the fragmenting of this restaurant
        for (int typeIndex = 0; typeIndex < state.length; typeIndex++) {

            rest2.state[typeIndex] = new int[state[typeIndex].length];
            rest2.state[typeIndex][0] = state[typeIndex][0];

            //create arraylist to hold new seating arrangement
            ArrayList<SetableInteger> newTableStructure = new ArrayList<SetableInteger>(state[typeIndex].length);

            for (int table = 1; table < state[typeIndex].length; table++) {
                ArrayList<SetableInteger> fragmentedTable = new ArrayList<SetableInteger>(2);
                fragmentedTable.add(new SetableInteger(1));
                double totalWeight = 1.0 + concentration;

                topFor:
                for (int person = 0; person < state[typeIndex][table] - 1; person++) {
                    double rawRandomSample = Math.random();
                    double cumSum = 0.0;
                    for (SetableInteger fragWeight : fragmentedTable) {
                        cumSum += (fragWeight.getVal() - Math.exp(logDiscount)) / totalWeight;
                        if (cumSum > rawRandomSample) {
                            fragWeight.increment();
                            totalWeight++;
                            continue topFor;
                        }
                    }
                    fragmentedTable.add(new SetableInteger(1));
                    totalWeight++;
                }
                rest2.state[typeIndex][table] = fragmentedTable.size();
                newTableStructure.addAll(fragmentedTable);
            }

            int[] newTypeState = new int[newTableStructure.size() + 1];
            newTypeState[0] = state[typeIndex][0];
            int index = 1;
            for (SetableInteger table : newTableStructure) {
                newTypeState[index++] = table.getVal();
            }
            state[typeIndex] = newTypeState;

        }

        return rest2;
    }

    public boolean unseatTypeInRest(int type) {
        boolean unSeatInParent = true;
        int typeIndex = -1;

        for (int j = 0; j < state.length; j++) {
            if (state[j][0] == type) {
                typeIndex = j;
                continue;
            }
        }

        if (typeIndex == -1) {
            throw new RuntimeException("trying to delete a type not present in " +
                    "this restaurant");
        }

        double totalWeight = 0.0;
        for (int j = 1; j < state[typeIndex].length; j++) {
            totalWeight += state[typeIndex][j];
        }

        double cumSum = 0.0;
        double rawRandomSample = Math.random();
        for (int j = 1; j < state[typeIndex].length; j++) {
            cumSum += state[typeIndex][j] / totalWeight;
            if (cumSum > rawRandomSample) {
                if (state[typeIndex][j] == 1) {
                    int[] newTypeState = new int[state[typeIndex].length - 1];
                    System.arraycopy(state[typeIndex], 0, newTypeState, 0, j);
                    System.arraycopy(state[typeIndex], j + 1, newTypeState, j, state[typeIndex].length - j - 1);
                    continue;
                } else {
                    state[typeIndex][j]--;
                    unSeatInParent = false;
                    continue;
                }
            }
        }
        return unSeatInParent;
    }

    public void unseatType(int type) {
        if (this.parent == null) {
            this.unseatTypeInRest(type);
            return;
        }

        boolean indicator = true;
        Restaurant currentRest = this;
        while (indicator && (currentRest.parent != null)) {
            indicator = currentRest.unseatTypeInRest(type);
            currentRest = currentRest.parent;
        }

        if (indicator) {
            currentRest.unseatType(type);
        }
    }

    public double getProbUpper(int type, int alphabetSize) {
        double probUpper = 0.0;

        if (this.parent == null) {
            probUpper = 1.0 / alphabetSize;
            return probUpper;
        }

        Restaurant currentRest = this.parent;

        double multFactor = 1.0;
        while (currentRest.parent != null) {
            int[] currentRestCounts = currentRest.getRestCounts(type);
            probUpper += multFactor * (currentRestCounts[0] - Math.exp(Math.log(currentRestCounts[1]) + currentRest.logDiscount)) / currentRestCounts[2];
            multFactor *= Math.exp(Math.log(currentRestCounts[3]) + currentRest.logDiscount - Math.log(currentRestCounts[2]));
            currentRest = currentRest.parent;
        }

        probUpper += multFactor * 1.0 / alphabetSize;
        return probUpper;
    }

    public void sitAtRest(int type, int alphabetSize) {
        if (this.parent == null) {
            this.sitAtRest(type, 1.0 / alphabetSize);
            return;
        }

        boolean indicator = true;
        Restaurant currentRest = this;
        while (indicator && (currentRest.parent != null)) {
            indicator = currentRest.sitAtRest(type, currentRest.getProbUpper(type, alphabetSize));
            currentRest = currentRest.parent;
        }

        if (indicator) {
            currentRest.sitAtRest(type, 1.0 / alphabetSize);
        }
    }

    //method to reseat the entire restaurant
    public void reSeat(int alphabetSize) {
        for (int typeIndex = 0; typeIndex < state.length; typeIndex++) {
            if (state[typeIndex].length == 2 && state[typeIndex][1] == 1) {
                continue;
            }
            //copy the current state into a new array
            int[] copyOfArray = new int[state[typeIndex].length];
            System.arraycopy(state[typeIndex], 0, copyOfArray, 0, state[typeIndex].length);

            //go through and reseat each person at each table
            for (int table = 1; table < copyOfArray.length; table++) {
                for (int person = 0; person < copyOfArray[table]; person++) {
                    state[typeIndex][table]--;
                    if (state[typeIndex][table] == 0 && this.parent != null) {
                        this.parent.unseatType(copyOfArray[0]);
                    }
                    this.sitAtRest(copyOfArray[0], alphabetSize);
                }
            }

            //go through and get rid of zero elements
            ArrayList<Integer> newTypeState = new ArrayList(state[typeIndex].length);
            newTypeState.add(state[typeIndex][0]);
            for (int j = 1; j < state[typeIndex].length; j++) {
                if (state[typeIndex][j] > 0) {
                    newTypeState.add(state[typeIndex][j]);
                }
            }

            int[] newTypeStateArray = new int[newTypeState.size()];
            int index = 0;
            for (Integer j : newTypeState) {
                newTypeStateArray[index++] = j.intValue();
            }
            state[typeIndex] = newTypeStateArray;
        }
    }

    //method to return log likelihood of current state of restaurant
    public double getLogLikelihood(Double logDisc, double[] logParentDistrib) {
        double restLogLikelihood = 0.0;
        int totalTablesSatAt = 0;
        int totalPeopleSat = 0;
        for (int typeIndex = 0; typeIndex < state.length; typeIndex++) {
            for (int table = 1; table < state[typeIndex].length; table++) {
                for (int cust = 0; cust < state[typeIndex][table]; cust++) {
                    //first person in restaurant
                    if (totalTablesSatAt == 0) {
                        restLogLikelihood += logParentDistrib[state[typeIndex][0]];
                        totalTablesSatAt++;
                        totalPeopleSat++;
                    } //first person at a new table
                    else if (cust == 0) {
                        restLogLikelihood += Math.log(totalTablesSatAt++) + logDisc - Math.log(totalPeopleSat++) + logParentDistrib[state[typeIndex][0]];
                    } //sitting at already existing occupied table
                    else {
                        restLogLikelihood += Math.log(cust - Math.exp(logDisc)) - Math.log(totalPeopleSat++);
                    }
                }
            }
        }

        return restLogLikelihood;
    }

    public void delete(int[] seq) {
        int keyIndex = parentPath[1] - 1;
        Integer key = new Integer(seq[keyIndex]);
        if (parent.remove(key) == null) {
            throw new RuntimeException("trying to delete a restaurant incorrectly");
        }
        numberRest--;
    }

    public void deleteTable(int[] seq) {
        //if only one table then we will delete this table no matter what
        if (state.length == 1 && state[0].length == 2) {
            this.delete(seq);
            return;
        }

        int peopleAtRest = this.getNumberOfCustomers();
        if(peopleAtRest ==0){
            throw new RuntimeException("Cannot delete table from this rest" +
                    " since there are no people in the restaurant") ;
        }
        double rawRandomSample = Math.random();
        double cumSum = 0.0;
        int deleteIndex = 0;
        int deleteTable = 0;
        topFor:
        for (int typeIndex = 0; typeIndex < state.length; typeIndex++) {
            for (int table = 1; table < state[typeIndex].length; table++) {
                cumSum += table / peopleAtRest;
                if (cumSum > rawRandomSample) {
                    deleteIndex = typeIndex;
                    deleteTable = table;
                    break topFor;
                }
            }
        }

        //if table is only table of type, then may remove the entire row from
        //the state
        if (state[deleteIndex].length == 2) {
            int[][] newState = new int[state.length - 1][];
            System.arraycopy(state, 0, newState, 0, deleteIndex);
            System.arraycopy(state, 0, newState, deleteIndex, state.length - deleteIndex - 1);
            state = newState ;
            return ;
        }

        //otherwise the table is not the only table in the rest of the type and
        //the rest will not need to be deleted
        int[] newTypeState = new int[state[deleteIndex].length-1] ;
        System.arraycopy(state[deleteIndex], 0, newTypeState, 0, deleteTable);
        System.arraycopy(state[deleteIndex], 0, newTypeState, deleteTable, state[deleteIndex].length - deleteTable-1);
    }

    public void printState() {
        if (state == null) {
            return;
        }
        System.out.print("[");
        for (int i = 0; i < state.length; i++) {
            System.out.print("['" + state[i][0] + "', ");
            for (int j = 1; j < state[i].length - 1; j++) {
                System.out.print(state[i][j] + ", ");
            }
            System.out.print(state[i][state[i].length - 1] + "]");
        }
        System.out.println("]");
    }

    public void printParentPath() {
        System.out.println("[" + parentPath[0] + ", " + parentPath[1] + "]");
    }
}
