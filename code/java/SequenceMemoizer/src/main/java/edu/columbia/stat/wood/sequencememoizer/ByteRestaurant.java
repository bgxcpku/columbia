/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import edu.columbia.stat.wood.sequencememoizer.ConstantSpaceSequenceMemoizer.SeatReturn;
import edu.columbia.stat.wood.util.ArraySet;
import edu.columbia.stat.wood.util.BigInt;
import edu.columbia.stat.wood.util.ByteMap;
import edu.columbia.stat.wood.util.SeatingArranger;


/**
 *
 * @author nicholasbartlett
 */
public class ConstantSpaceRestaurant extends ByteMap<ConstantSpaceRestaurant> {

    public byte[] types;
    public int[] customersAndTables;
    public int customers, tables, edgeStart, edgeLength;
    public byte key;
    public BigInt edgeKey;
    public ConstantSpaceRestaurant parent;
    public static int count = 0;

    public boolean added = false;

    public static ArraySet<ConstantSpaceRestaurant> restaurants;

    public ConstantSpaceRestaurant(ConstantSpaceRestaurant parent, int edgeStart, int edgeLength, BigInt edgeKey, byte key) {
        this.parent = parent;
        this.edgeStart = edgeStart;
        this.edgeLength = edgeLength;
        this.edgeKey = edgeKey;
        this.key = key;
        customers = 0;
        tables = 0;
        count++;

        if(restaurants != null){
            restaurants.add(this);
        }
    }

    public void checkKeys(){
        byte[] childKeys = new byte[size()];

        int index = 0;
        for(ConstantSpaceRestaurant child : values()){
            childKeys[index++] = child.key;
        }

        for(byte b1 : childKeys){
            int dup = 0;
            for(byte b2 : childKeys){
                if(b1 == b2){
                    dup++;
                }
            }
            assert dup == 1;
        }

        for(byte b : childKeys){
            int dup = 0;
            for(byte k : keys()){
                if(b == k){
                    dup++;
                }
            }
            assert dup == 1;
        }
/*
        for(int i = 0; i< childKeys.length; i++){
            System.out.print(childKeys[i] + ", ");
        }
        print();
        System.out.println(); */
    }

    public void setTableConfig(byte[] types, int[] customersAndTables, int customers, int tables) {
        this.types = types;
        this.customersAndTables = customersAndTables;
        this.customers = customers;
        this.tables = tables;
    }

    public double getPP(byte type, double p, double discount, SeatReturn sr) {
        int index, tc, tt, tci, tti;

        index = getIndex(type);

        assert types[index] == type;

        tci = 2 * index;
        tti = tci + 1;

        tc = customersAndTables[tci];
        tt = customersAndTables[tti];

        sr.set(false, tt, customers, tables);
        p -= ((double) tc - (double) tt * discount) / (double) customers;

        return p * (double) customers / ((double) tables * discount);
    }

    public double seat(byte type, double p, double discount, SeatReturn sr) {
        if (customers == 0) {
            sr.set(true, 0, customers, tables);

            types = new byte[]{type};
            customersAndTables = new int[]{1, 1};
            customers++;
            tables++;
        } else if (type > types[types.length - 1]) {
            p *= (double) customers / ((double) tables * discount);
            sr.set(true, 0, customers, tables);

            insertNewType(type, types.length);
            customers++;
            tables++;
        } else {
            int index;

            index = getIndex(type);
            if (types[index] != type) {
                p *= (double) customers / ((double) tables * discount);
                sr.set(true, 0, customers, tables);

                insertNewType(type, index);
                customers++;
                tables++;
            } else {
                double numerator, denominator;
                int tci, tti, tc, tt;

                tci = index * 2;
                tti = tci + 1;

                tc = customersAndTables[tci];
                tt = customersAndTables[tti];

                numerator = (double) tc - (double) tt * discount;

                p -= numerator / (double) customers;
                p *= (double) customers / ((double) tables * discount);

                denominator = numerator + (double) tables * discount * p;

                if (numerator / denominator > ConstantSpaceSequenceMemoizer.RNG.nextDouble()) {
                    sr.set(false, customersAndTables[tti], customers, tables);

                    customersAndTables[tci]++;
                    customers++;
                } else {
                    sr.set(true, customersAndTables[tti], customers, tables);

                    customersAndTables[tci]++;
                    customersAndTables[tti]++;
                    customers++;
                    tables++;
                }
            }
        }

        return p;
    }

    private void insertNewType(byte type, int index) {
        byte[] newTypes;
        int[] newCustomersAndTables;
        int l;

        l = types.length;
        newTypes = new byte[l + 1];
        newCustomersAndTables = new int[2 * l + 2];

        System.arraycopy(types, 0, newTypes, 0, index);
        System.arraycopy(customersAndTables, 0, newCustomersAndTables, 0, index * 2);

        newTypes[index] = type;
        newCustomersAndTables[2 * index] = 1;
        newCustomersAndTables[2 * index + 1] = 1;

        System.arraycopy(types, index, newTypes, index + 1, l - index);
        System.arraycopy(customersAndTables, index * 2, newCustomersAndTables, index * 2 + 2, 2 * l - 2 * index);

        types = newTypes;
        customersAndTables = newCustomersAndTables;
    }

    public int getIndex(byte type) {
        int l, r, midPoint;

        assert type <= types[types.length - 1];

        l = 0;
        r = types.length - 1;

        while (l < r) {
            midPoint = (l + r) / 2;
            if (type > types[midPoint]) {
                l = midPoint + 1;
            } else {
                r = midPoint;
            }
        }
        return l;
    }

    public ConstantSpaceRestaurant fragmentForInsertion(ConstantSpaceRestaurant irParent, int irEdgeStart, int irEdgeLength, BigInt irEdgeKey, double discount, double irDiscount) {
        double fragDiscount, fragConcentration, numerator, denominator;
        ConstantSpaceRestaurant intermediateRestaurant;
        byte[] irTypes;
        int[] irCustomersAndTables, tsa;
        int l, tci, tti, fc, ft, tc, tt, irc, irt;

        customers = 0;
        tables = 0;

        intermediateRestaurant = new ConstantSpaceRestaurant(irParent, irEdgeStart, irEdgeLength, irEdgeKey, key);
        if (types == null) {
            edgeLength -= irEdgeLength;
            parent = intermediateRestaurant;
            return intermediateRestaurant;
        }
        fragDiscount = discount / irDiscount;
        fragConcentration = -1 * discount;

        l = types.length;
        irTypes = new byte[l];
        System.arraycopy(types, 0, irTypes, 0, l);
        irCustomersAndTables = new int[2 * l];

        irc = 0;
        irt = 0;

        for (int typeIndex = 0; typeIndex < l; typeIndex++) {
            tci = 2 * typeIndex;
            tti = tci + 1;
            tc = customersAndTables[tci];
            tt = customersAndTables[tti];

            tsa = SeatingArranger.getSeatingArrangement(tc, tt, discount);

            fc = 0;
            ft = 0;
            for (int tableSize : tsa) {
                fc++;
                ft++;

                numerator = 1.0 - fragDiscount;
                denominator = 1.0 + fragConcentration;
                for (int customer = 1; customer < tableSize; customer++) {
                    if (numerator / denominator > ConstantSpaceSequenceMemoizer.RNG.nextDouble()) {
                        fc++;
                        numerator += 1.0;
                        denominator += 1.0;
                    } else {
                        fc++;
                        ft++;
                        numerator += 1.0 - fragDiscount;
                        denominator += 1.0;
                    }
                }
            }

            irc += ft;
            irt += tt;

            irCustomersAndTables[tci] = ft;
            irCustomersAndTables[tti] = tt;

            customers += fc;
            tables += ft;

            customersAndTables[tci] = fc;
            customersAndTables[tti] = ft;
        }

        intermediateRestaurant.setTableConfig(irTypes, irCustomersAndTables, irc, irt);
        parent = intermediateRestaurant;
        edgeStart += irEdgeLength;
        edgeLength -= irEdgeLength;

        return intermediateRestaurant;
    }

    public static void removeRestaurants(int n){
        ConstantSpaceRestaurant r;
        int index;
        boolean remove;

        for (int i = 0; i < n; i++) {
            r = null;
            index = -1;
            remove = false;

            while (!remove) {
                index = (int) (ConstantSpaceSequenceMemoizer.RNG.nextDouble() * (double) restaurants.maxIndex());
                r = restaurants.get(index);
                remove = (r != null) && r.isEmpty();
            }

            r.parent.remove(r.key);
            restaurants.remove(index);
            count--;
        }
    }
}
