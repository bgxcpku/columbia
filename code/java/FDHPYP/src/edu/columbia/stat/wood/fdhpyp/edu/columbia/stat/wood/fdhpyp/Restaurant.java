/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.fdhpyp;

import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author nicholasbartlett
 */
public class Restaurant extends TreeMap<Integer, Restaurant> {

    private MutableDouble discount;
    private MutableDouble concentration;
    private TreeMap<Integer, int[]> tableMap;
    private MutableInteger customers;
    private MutableInteger tables;
    private Restaurant parent;
    
    public static int restCount = 0;
    public static final Random RNG = new Random(0);

    public Restaurant(Restaurant parent, MutableDouble discount, MutableDouble concentration) {
        this.parent = parent;
        this.discount = discount;
        this.concentration = concentration;
        tableMap = new TreeMap<Integer, int[]>();
        restCount++;
    }

    public void fillSummaryStats() {
        int[] counts;

        clearSummaryStats();
        getCounts(counts = new int[2]);

        customers = new MutableInteger(counts[0]);
        tables = new MutableInteger(counts[1]);
    }

    public void clearSummaryStats() {
        customers = null;
        tables = null;
    }

    public void getCounts(int type, int[] counts) {
        int[] tSeatingArrangment;
        int cust;
        int tbls;

        cust = 0;
        tbls = 0;
        tSeatingArrangment = tableMap.get(type);

        if (tSeatingArrangment != null) {
            for (int c : tSeatingArrangment) {
                cust += c;
            }
            tbls = tSeatingArrangment.length;
        }

        counts[0] = cust;
        counts[1] = tbls;
    }

    public void getCounts(int[] counts) {
        int cust;
        int tbls;

        if (customers == null) {
            cust = 0;
            tbls = 0;

            for (int[] typeSeatingArrangment : tableMap.values()) {
                for (int c : typeSeatingArrangment) {
                    cust += c;
                }
                tbls += typeSeatingArrangment.length;
            }
        } else {
            cust = customers.intVal();
            tbls = tables.intVal();
        }

        counts[0] = cust;
        counts[1] = tbls;
    }

    public double predictiveProbability(int type) {
        double d,c,pp,denom;
        int[] tCounts, counts;

        d = discount.doubleVal();
        c = concentration.doubleVal();
        pp = (parent == null) ? (1.0 / 256.0) : parent.predictiveProbability(type);
        getCounts(type, tCounts = new int[2]);
        getCounts(counts = new int[2]);
        denom = (double) counts[0] + c;

        return ((double) tCounts[0] - (double) tCounts[1] * d) / denom + ((double) counts[1] * d + c) * pp / denom;
    }

    public void seat(int type){
        int[] tSeatingArrangement;
        boolean seatInParent;

        tSeatingArrangement = tableMap.get(type);
        seatInParent = true;

        if(tSeatingArrangement == null){
            tableMap.put(type, new int[]{1});
            seatInParent = true;
        } else {
            double d;
            double c;
            double pp;
            double rand;
            double cuSum;
            int[] tCounts;
            int[] counts;
            double totalWeight;

            d = discount.doubleVal();
            c = concentration.doubleVal();
            pp = (parent == null) ? (1.0 / 256.0) : parent.predictiveProbability(type);
            rand = Restaurant.RNG.nextDouble();
            cuSum = 0.0;
            getCounts(type, tCounts = new int[2]);
            getCounts(counts = new int[2]);
            totalWeight = (double)tCounts[0] - (double)tCounts[1]*d + ((double) counts[1] * d + c) * pp;

            for (int i = 0; i < tSeatingArrangement.length; i++) {
                cuSum += ((double) tSeatingArrangement[i] - d) / totalWeight;
                if (cuSum > rand) {
                    tSeatingArrangement[i]++;
                    seatInParent = false;
                    break;
                }
            }

            if (cuSum <= rand) {
                int[] nSeatingArrangement = new int[tSeatingArrangement.length + 1];
                System.arraycopy(tSeatingArrangement,0,nSeatingArrangement,0,tSeatingArrangement.length);
                nSeatingArrangement[tSeatingArrangement.length] = 1;
                tableMap.put(type, nSeatingArrangement);
                seatInParent = true;
            }
        }

        incrementCustomers();
        if(seatInParent && parent != null){
            incrementTables();
            parent.seat(type);
        }
    }

    public void unseat(int type){
        int[] tSeatingArrangement;
        double random;
        int c;
        int ind;
        double cuSum;
        boolean unseatInParent = true;

        tSeatingArrangement = tableMap.get(type);
        random = Restaurant.RNG.nextDouble();
        c = 0;
        ind = -1;
        cuSum = 0.0;
        assert(tSeatingArrangement != null);

        for(int cust:tSeatingArrangement){
            c += cust;
        }

        for(int i = 0; i<tSeatingArrangement.length; i++){
            cuSum += (double) tSeatingArrangement[i] / (double) c;
            if(cuSum > random){
                tSeatingArrangement[i]--;
                ind = i;
                break;
            }
        }

        if(tSeatingArrangement[ind] == 0){
            unseatInParent = true;
            if(tSeatingArrangement.length == 1){
                tableMap.remove(type);
            } else {
                int[] nSeatingArrangement = new int[tSeatingArrangement.length - 1];
                System.arraycopy(tSeatingArrangement,0,nSeatingArrangement,0,ind);
                System.arraycopy(tSeatingArrangement,ind+1,nSeatingArrangement,ind,tSeatingArrangement.length - 1-ind);
                tableMap.put(type, nSeatingArrangement);
            }
        } else {
            unseatInParent = false;
        }

        decrementCustomers();
        if(unseatInParent && parent != null){
            decrementTables();
            parent.unseat(type);
        }
    }

    public void sampleSeatingArrangements(){
        Set<Integer> keySet;
        int[] t;
        int[] c;
        int[] n;
        int ind;

        keySet = tableMap.keySet();
        for(Integer i:keySet){
            t = tableMap.get(i);
            if(t.length==1 && t[0] == 1){
                continue;
            }

            c = new int[t.length];
            System.arraycopy(t, 0, c, 0, t.length);

            ind = 0;
            for(int cust:c){
                for(int j = 0; j<cust; j++){
                    t[ind]--;
                    if(t[ind] == 0){
                        if(parent!=null){
                            parent.unseat(i);
                        }
                        n = new int[t.length-1];
                        System.arraycopy(t, 0, n, 0, ind);
                        System.arraycopy(t,ind+1,n,ind,t.length-1-ind);
                        tableMap.put(i, n);
                        ind--;
                    }
                    seat(i);
                    t = tableMap.get(i);
                }
                ind++;
            }
        }
    }

    public double logLik(){
        int c;
        int t;
        double logLik;

        c = 0;
        t = 0;
        logLik = 0.0;

        for(int[] tSeatingArrangement:tableMap.values()){
            for(int custs:tSeatingArrangement){
                logLik += logLikTable(custs,c,t);
                t++;
                c+=custs;
            }
        }
        
        return logLik;
    }

    private double logLikTable(int tableSize, int existingCust, int existingTables) {
        double logLik;
        double p;

        logLik = 0.0;

        if(parent != null) {
            p = ((double) existingTables * discount.doubleVal() + concentration.doubleVal()) / ((double) existingCust + concentration.doubleVal());
        } else {
            p = ((double) existingTables * discount.doubleVal() + concentration.doubleVal()) * (1.0 / 256.0) / ((double) existingCust + concentration.doubleVal());
        }
        logLik += Math.log(p);
        existingCust++;

        for (int i = 1; i < tableSize; i++) {
            p = ((double) i - discount.doubleVal()) / ((double) existingCust + concentration.doubleVal());
            logLik += Math.log(p);
            existingCust++;
        }

        return logLik;
    }

    public void printTableMap(){
        int[] tSeatingArrangement;
        for(Integer i:tableMap.keySet()){
            tSeatingArrangement = tableMap.get(i);
            System.out.print(i + " : [");
            for(int j : tSeatingArrangement){
                System.out.print(j + ", ");
            }
            System.out.println("]");
        }
    }

    private void incrementTables(){
        if(tables!= null){
            tables.increment();
        }
    }

    private void decrementTables(){
        if(tables!= null){
            tables.decrement();
        }
    }

    private void incrementCustomers(){
        if(customers!= null){
            customers.increment();
        }
    }

    private void decrementCustomers(){
        if(customers!=null){
            customers.decrement();
        }
    }

    public static void main(String[] args) {
        Restaurant r = new Restaurant(null, new MutableDouble(.3), null);

        r.fillSummaryStats();
        r.fillSummaryStats();

        System.out.println(r.customers.intVal());
        System.out.println(r.tables.intVal());
    }
}
