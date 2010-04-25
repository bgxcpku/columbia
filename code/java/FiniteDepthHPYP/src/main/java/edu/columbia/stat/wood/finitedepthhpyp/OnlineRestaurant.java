/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.finitedepthhpyp;

import java.util.Arrays;

/**
 *
 * @author nicholasbartlett
 */
public class OnlineRestaurant extends Restaurant {

    private Restaurant parent;
    private int[][] state = null;

    public OnlineRestaurant(Restaurant parent) {
        this.parent = parent;
        numberRest++;
    }

    @Override
    public Restaurant getParent() {
        return this.parent;
    }

    @Override
    public boolean hasNoCustomers() {
        return (state == null);
    }

    @Override
    public void getRestCounts(int type, int[] counts) {
        Arrays.fill(counts, 0);
        if (state != null){
            for (int typeIndex = 0; typeIndex < state.length; typeIndex++) {
                if (state[typeIndex][0] == type) {
                    counts[0] = state[typeIndex][1];
                    counts[1] = state[typeIndex][2];
                }
                counts[2] += state[typeIndex][1];
                counts[3] += state[typeIndex][2];
            }
        }
    }

    @Override
    public Restaurant sitAtRest(int type, double probUpper, double discount, double concentration) {
        Restaurant returnVal = null;
        if (state == null) {
            this.insertStateRow(type);
            returnVal = parent;
        } else {
            int typeIndex = -1;

            int typeCust = 0;
            int typeTables = 0;

            int cust = 0;
            int tables = 0;

            for (int tIndex = 0; tIndex < state.length; tIndex++) {
                if (state[tIndex][0] == type) {
                    typeCust = state[tIndex][1];
                    typeTables = state[tIndex][2];
                    typeIndex = tIndex;
                }
                cust += state[tIndex][1];
                tables += state[tIndex][2];
            }

            if (typeIndex == -1) {
                this.insertStateRow(type);
                returnVal = parent;
            } else {
                double inRestWeight = (double) typeCust - discount * typeTables;
                double totalWeight = inRestWeight + (tables * discount + concentration) * probUpper;
                if (HPYTree.RNG.nextDouble() >= inRestWeight / totalWeight) {
                    state[typeIndex][2]++;
                    returnVal = parent;
                }
                state[typeIndex][1]++;
            }
        }
        return returnVal;
    }

    private void insertStateRow(int type) {
        if (state == null) {
            state = new int[1][3];
            state[0] = new int[]{type, 1, 1};
        } else {
            int[][] newState = new int[state.length + 1][3];

            int typeIndex = 0;
            for(int tIndex = 0; tIndex<state.length; tIndex++){
                if(state[tIndex][0]>type){
                    typeIndex = tIndex;
                    break;
                }
            }

            System.arraycopy(state, 0, newState, 0, typeIndex);
            newState[typeIndex] = new int[]{type, 1, 1};
            System.arraycopy(state, typeIndex, newState, typeIndex + 1, state.length - typeIndex);

            state = newState;
        }
    }

    @Override
    public void fillPredictiveCounts(double discount, double concentration, PredictiveCounts pc) {
        int cust = 0;
        int tables = 0;
        for (int tIndex = 0; tIndex < state.length; tIndex++) {
            pc.typeNum[state[tIndex][0]] = state[tIndex][1] - discount * state[tIndex][2];
            cust += state[tIndex][1];
            tables += state[tIndex][2];
        }
        pc.concentration = concentration;
        pc.discount = discount;
        pc.cust = cust;
        pc.tables = tables;
    }

    @Override
    public double getLogLik(double discount, double concentration) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void printState() {
        for (int tIndex = 0; tIndex < state.length; tIndex++) {
            for (int i = 0; i < 3; i++) {
                System.out.print(state[tIndex][i] + ", ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        OnlineRestaurant r = new OnlineRestaurant(null);
        r.insertStateRow(4);
        r.printState();
        r.insertStateRow(2);
        r.printState();
        r.insertStateRow(3);
        r.printState();


    }
}
