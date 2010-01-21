/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package finitedepthhpyp;

import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */
public class Restaurant extends HashMap<Integer, Restaurant> {

    public double concentration;
    public double discount;
    public int[][] state;
    public Restaurant parent;
    public static int numberRest = 0;

    public Restaurant(double concentration, double discount, Restaurant parent) {
        this.concentration = concentration;
        this.discount = discount;
        this.parent = parent;
        numberRest++;
    }

    //method to return the number of tables in the restaurant
    public int getNumberOfTables() {
        int numberOfTables = 0;

        if (state != null) {
            for (int typeIndex = 0; typeIndex < state.length; typeIndex++) {
                numberOfTables += state[typeIndex].length - 1;
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
            for (int typeIndex = 0; typeIndex < state.length; typeIndex++) {
                if (type == state[typeIndex][0]) {
                    for (int table = 1; table < state[typeIndex].length; table++) {
                        custOfType += state[typeIndex][table];
                        totalCust += state[typeIndex][table];
                    }
                    tablesOfType += state[typeIndex].length - 1;
                    totalTables += state[typeIndex].length - 1;
                } else {
                    for (int table = 1; table < state[typeIndex].length; table++) {
                        totalCust += state[typeIndex][table];
                    }
                    totalTables += state[typeIndex].length - 1;
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

    //method to sit a type at a restaurant given the current state of the
    //restaurant.  returns a value of true if a person must be sat at the parent
    //table and false if not.
    public boolean sitAtRest(int type, double probUpper) {
        //if no one is sitting in restaurant
        if (state == null) {
            state = new int[1][2];
            state[0][0] = type;
            state[0][1] = 1;
            return true;
        }

        int typeIndex = -1;
        int custThisType = 0;
        int tablesThisType = 0;
        int custRest = 0;
        int tablesRest = 0;

        for (int tIndex = 0; tIndex < state.length; tIndex++) {
            if (state[tIndex][0] == type) {
                typeIndex = tIndex;
                tablesThisType += state[tIndex].length - 1;
                tablesRest += state[tIndex].length - 1;
                for (int table = 1; table < state[tIndex].length; table++) {
                    custThisType += state[tIndex][table];
                    custRest += state[tIndex][table];
                }
            } else {
                tablesRest += state[tIndex].length - 1;
                for (int table = 1; table < state[tIndex].length; table++) {
                    custRest += state[tIndex][table];
                }
            }
        }

        //if did not find the type in the state already, then will need to
        //create a new row in the state field
        if (typeIndex == -1) {
            int[][] newState = new int[state.length + 1][];
            System.arraycopy(state, 0, newState, 0, state.length);
            newState[state.length] = new int[2];
            newState[state.length][0] = type;
            newState[state.length][1] = 1;
            state = newState;
            return true;
        }

        //else if found will need to add stochastically
        double totalWeight = 1.0 * custThisType - tablesThisType * discount + (discount * tablesRest + concentration) * probUpper;
        double rawRandomSample = Math.random();
        double cumSum = 0.0;

        for (int table = 1; table < state[typeIndex].length; table++) {
            cumSum += (state[typeIndex][table] - discount) / totalWeight;
            if (cumSum > rawRandomSample) {
                state[typeIndex][table]++;
                return false;
            }
        }

        //if need to add new table must again create a new table structure
        int[] newTypeRow = new int[state[typeIndex].length + 1];
        System.arraycopy(state[typeIndex], 0, newTypeRow, 0, state[typeIndex].length);
        newTypeRow[state[typeIndex].length] = 1;
        state[typeIndex] = newTypeRow;
        return true;
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
}
