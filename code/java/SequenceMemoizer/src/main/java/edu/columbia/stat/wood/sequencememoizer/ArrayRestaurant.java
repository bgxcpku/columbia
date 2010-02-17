/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */

import java.util.ArrayList;
import java.util.HashMap;

public class ArrayRestaurant extends HashMap<Integer, Restaurant> {

    public int[][] state;
    public Restaurant parent;
    public int[] parentPath;
    public int[][] deletionState = null;
    public PredictiveCounts predictiveCounts = null;

    public static int numberRest = 0;


    public ArrayRestaurant(Restaurant parent, int[] parentPath, int depth) {
        super(2); //FIX this should be more smart, initialization should be based on depth
        this.parent = parent;
        this.parentPath = parentPath;
        numberRest++;
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
    public Restaurant sitAtRest(int type, double probUpper, double logDiscount) {
        //if no one is sitting in restaurant
        if (state == null) {
            state = new int[1][2];
            state[0][0] = type;
            state[0][1] = 1;
            return parent;
        }

        //else loop through state to see if type is already represented
        int typeIndex = -1;
        int numCustAtType = 0;
        int numTablesAtType = 0;
        int numTablesInRest = 0;
        for(int tIndex = 0; tIndex < state.length; tIndex++){
            if(state[tIndex][0] == type){
                for(int table = 1; table<state[tIndex].length; table++){
                    numCustAtType += state[tIndex][table];
                }
                numTablesAtType += state[tIndex].length - 1;
                numTablesInRest += state[tIndex].length - 1;
                typeIndex = tIndex;
            } else {
                numTablesInRest += state[tIndex].length - 1;
            }
        }

        //if found the type in the state then add
        if(typeIndex > -1){
            double cumSum = 0.0;
            double rawRandomSample = ByteSeater.utils.RNG.nextDouble();
            double totalWeight = 1.0 * (numCustAtType - Math.exp(Math.log(numTablesAtType) + logDiscount)) + Math.exp(Math.log(numTablesInRest) + logDiscount + Math.log(probUpper));

            for (int table = 1; table < state[typeIndex].length; table++) {
                    cumSum += (1.0*state[typeIndex][table] - Math.exp(logDiscount)) / totalWeight;
                    if (cumSum > rawRandomSample) {
                        state[typeIndex][table]++;
                        return null;
                    }
            }

            int[] newTypeTableStructure = new int[state[typeIndex].length + 1];
            System.arraycopy(state[typeIndex], 0, newTypeTableStructure, 0, state[typeIndex].length);
            newTypeTableStructure[state[typeIndex].length] = 1;
            state[typeIndex] = newTypeTableStructure;
            return parent;
        }

        //if not found then will need to create a row for this type
        int[][] newState = new int[state.length + 1][];
        System.arraycopy(state, 0, newState, 0, state.length);

        newState[state.length] = new int[2];
        newState[state.length][0] = type;
        newState[state.length][1] = 1;

        state = newState;
        return parent;
    }

    public Restaurant reconfigureRestaurantReturnIntermediateRestaurant(int[] rest2ParentPath, int rest2Depth, int[] seq, Discounts discounts) {
        //get log discount parameters
        double rest3LogDiscount = discounts.getLog(rest2Depth, rest2Depth + parentPath[1] - parentPath[0] - rest2ParentPath[1] + rest2ParentPath[0]);
        double rest2LogDiscount = discounts.getLog(rest2Depth - rest2ParentPath[1] + rest2ParentPath[0], rest2Depth);

        //create rest2
        Restaurant rest2 = new Restaurant(parent, rest2ParentPath, rest2Depth);

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
        double concentration = -1.0 * Math.exp(rest3LogDiscount + rest2LogDiscount);

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
                    double rawRandomSample = ByteSeater.utils.RNG.nextDouble();
                    double cumSum = 0.0;
                    for (SetableInteger fragWeight : fragmentedTable) {
                        cumSum += (fragWeight.getVal() - Math.exp(rest3LogDiscount)) / totalWeight;
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

    /*******************************SAMPLING***********************************/

    public Restaurant unseat(int type){
        int rowIndex = -1;

        for(int typeIndex = 0; typeIndex<state.length; typeIndex++){
            if(state[typeIndex][0] == type){
                rowIndex = typeIndex;
                break;
            }
        }

        assert(rowIndex > -1);

        if(state[rowIndex].length ==2){
            if(state[rowIndex][1] == 1){
                int[][] newState = new int[state.length-1][];
                System.arraycopy(state, 0, newState, 0, rowIndex);
                System.arraycopy(state, rowIndex+1, newState, rowIndex, state.length - rowIndex -1);
                return parent;
            } else {
                state[rowIndex][1]--;
                return null;
            }
        }

        int custOfType = 0;
        for(int table = 1; table<state[rowIndex].length; table++){
            custOfType += state[rowIndex][table];
        }

        double cumSum = 0.0;
        double rawRandomSample = Math.random();
        for (int table = 1; table < state[rowIndex].length; table++) {
            cumSum += 1.0*state[rowIndex][table] / custOfType;
            if (cumSum > rawRandomSample) {
                if (state[rowIndex][table] == 1) {
                    int[] newTypeState = new int[state[rowIndex].length - 1];
                    System.arraycopy(state[rowIndex], 0, newTypeState, 0, table);
                    System.arraycopy(state[rowIndex], table + 1, newTypeState, table, state[rowIndex].length - table - 1);
                    state[rowIndex] = newTypeState;
                    return parent;
                } else {
                    state[rowIndex][table]--;
                    return null;
                }
            }
        }
        throw new RuntimeException("should not get to this point in the code " +
                "since the person should already be deleted at this point");
    }

    public void fillPredictiveCounts(int as, double d, double c){
        predictiveCounts = new PredictiveCounts();

        predictiveCounts.discount = d;
        predictiveCounts.concentration = c;

        predictiveCounts.cust = 0;
        predictiveCounts.tables = 0;
        predictiveCounts.typeNum = new double[as];

        int type;
        int custAtTable;
        int tablesOfType;
        for(int typeIndex = 0; typeIndex<state.length; typeIndex++){
            type = state[typeIndex][0];
            tablesOfType = state[typeIndex].length - 1;
            predictiveCounts.tables += tablesOfType;
            predictiveCounts.typeNum[type] -= d*(tablesOfType);
            for(int table = 1; table<state[typeIndex].length; table++){
                custAtTable = state[typeIndex][table];
                predictiveCounts.cust += custAtTable;
                predictiveCounts.typeNum[type] += custAtTable;
            }
        }
    }

    /******************************DELETION OPERATIONS*************************/

    public void delete(int[] seq) {
        int keyIndex = parentPath[1] - 1;
        Integer key = new Integer(seq[keyIndex]);
        if (parent.remove(key) == null) {
            throw new RuntimeException("trying to delete a restaurant incorrectly");
        }
        numberRest--;
    }

    //method to remove restaurant and update the deletion state of the parent
    //restaurant
    public void deleteWithDeletionStateUpdate(int[] seq) {
        HashMap<Integer, Integer> deletionUpdate = new HashMap(20);

        //populate HashMap with current state of deletion state
        Integer key;
        Integer val;
        if (deletionState != null) {
            for (int typeIndex = 0; typeIndex < deletionState.length; typeIndex++) {
                key = new Integer(deletionState[typeIndex][0]);
                val = new Integer(deletionState[typeIndex][1]);
                deletionUpdate.put(key, val);
            }
        }

        //add in elements contributed to parent deletion state by this restaurant
        int valValue;
        for (int typeIndex = 0; typeIndex < state.length; typeIndex++) {
            key = new Integer(state[typeIndex][0]);
            valValue = 0;
            for (int table = 1; table < state[typeIndex].length; table++) {
                valValue += (state[typeIndex][table] - 1);
            }
            if ((val = deletionUpdate.get(key)) != null) {
                val = new Integer(valValue + val.intValue());
                deletionUpdate.put(key, val);
            } else {
                val = new Integer(valValue);
                deletionUpdate.put(key, val);
            }
        }

        //now go up and add in current state of parent deletion state if it exists
        if (parent.deletionState != null) {
            for (int typeIndex = 0; typeIndex < parent.deletionState.length; typeIndex++) {
                key = new Integer(parent.deletionState[typeIndex][0]);
                if ((val = deletionUpdate.get(key)) != null) {
                    val = new Integer(parent.deletionState[typeIndex][1] + val.intValue());
                    deletionUpdate.put(key, val);
                } else {
                    val = new Integer(parent.deletionState[typeIndex][1]);
                    deletionUpdate.put(key, val);
                }
            }
        }

        //create parent.deletionState by exporting hashMap to int[][]
        parent.deletionState = new int[deletionUpdate.size()][2];
        int index = 0;
        for (Integer type : deletionUpdate.keySet()) {
            parent.deletionState[index][0] = type.intValue();
            parent.deletionState[index++][1] = deletionUpdate.get(type).intValue();
        }

        //actually delete restaurant
        this.delete(seq);
    }

    //method to return logProbOfSeq difference that would result in deleting this
    //restaurant.  Method assumes this is a leaf node
    public double getLogProbDiffIfDelete(double[] logProbDistInParent, int depth, Discounts discounts) {
        double logDiscount;
        if(this.parent != null){
            logDiscount = discounts.getLog(depth - parentPath[1] + parentPath[0], depth);
        } else {
            logDiscount = discounts.getLog(0);
        }

        if (this.size() != 0) {
            throw new RuntimeException("Cannot calculate the difference in log" +
                    " probability of this string if this is deleted because the" +
                    " node must be a leaf node");
        }

        double startLogProb = 0.0;
        double endLogProb = 0.0;

        int tablesInRest = 0;
        int custInRest = 0;
        for (int typeIndex = 0; typeIndex < state.length; typeIndex++) {
            tablesInRest += state[typeIndex].length - 1;
            for (int table = 1; table < state[typeIndex].length; table++) {
                custInRest += state[typeIndex][table];
            }
        }

        double[] logPredDist = new double[logProbDistInParent.length];

        int custOfType;
        int tablesOfType;
        for (int typeIndex = 0; typeIndex < state.length; typeIndex++) {
            custOfType = 0;
            tablesOfType = state[typeIndex].length - 1;
            for (int table = 1; table < state[typeIndex].length; table++) {
                custOfType += state[typeIndex][table];
            }
            logPredDist[state[typeIndex][0]] = Math.log((custOfType - Math.exp(Math.log(tablesOfType) + logDiscount)) / custInRest + Math.exp(Math.log(tablesInRest) + logDiscount + logProbDistInParent[state[typeIndex][0]]) / custInRest);

            startLogProb += custOfType * logPredDist[state[typeIndex][0]];
            endLogProb += custOfType * logProbDistInParent[state[typeIndex][0]];
        }

        if (deletionState != null) {
            for (int typeIndex = 0; typeIndex < deletionState.length; typeIndex++) {
                startLogProb += deletionState[typeIndex][1] * logPredDist[state[typeIndex][0]];
                endLogProb += deletionState[typeIndex][1] * logProbDistInParent[state[typeIndex][0]];
            }
        }

        return (startLogProb - endLogProb);
    }

    /******************************PRINTING************************************/

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