/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import edu.columbia.stat.wood.util.ByteFiniteDiscreteDistribution;
import edu.columbia.stat.wood.util.SampleWithoutReplacement;
import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */
public class ByteSamplingNode {

    private HashMap<Byte, TypeSeatingArrangement> seatingArrangement;
    private ByteSamplingNode parent;
    private int tables, customers;
    private double discount;
    private ByteFiniteDiscreteDistribution baseDistribution;

    public ByteSamplingNode(ByteSamplingNode parent, double discount, ByteFiniteDiscreteDistribution baseDistribution) {
        this.discount = discount;
        seatingArrangement = new HashMap<Byte, TypeSeatingArrangement>();
        tables = 0;
        customers = 0;
        this.baseDistribution = baseDistribution;
    }

    public void setTypeSeatingArrangement(byte type, int[] seatingArrangement, int typeCustomers, int typeTables) {
        this.seatingArrangement.put(type, new TypeSeatingArrangement(seatingArrangement, typeCustomers, typeTables));
        customers += typeCustomers;
        tables += typeTables;
    }

    public double predictiveProbability(byte type) {
        double p;

        if (parent == null) {
            p = baseDistribution.probability(type);
        } else {
            p = parent.predictiveProbability(type);
        }

        if (customers > 0) {
            TypeSeatingArrangement tsa;

            p *= (double) tables * discount / (double) customers;

            tsa = seatingArrangement.get(type);
            if (tsa != null) {
                p += ((double) tsa.typeCustomers - (double) tsa.typeTables * discount) / (double) customers;
            }
        }

        return p;
    }

    public void seat(byte type) {
        double pp;
        TypeSeatingArrangement tsa;

        pp = parent.predictiveProbability(type);

        tsa = seatingArrangement.get(type);

        assert tsa != null : "Should not be null since I'm not removing types during sampling";

        if (tsa.seat(pp)) {
            customers++;
            tables++;
            if (parent != null) {
                parent.seat(type);
            }
        } else {
            customers++;
        }
    }

    public void unseat(byte type) {
        TypeSeatingArrangement tsa;

        tsa = seatingArrangement.get(type);

        assert tsa != null : "Should not be null since I'm not removing types during sampling";

        if (tsa.unseat()) {
            customers--;
            tables--;
            if (parent != null) {
                parent.unseat(type);
            }
        } else {
            customers--;
        }
    }

    public void sample(){
        Pair<byte[], int[]> randomCustomers;
        byte[] types;
        int[] tables;

        assert check();

        randomCustomers = randomCustomersToSample();
        types = randomCustomers.first();
        tables = randomCustomers.second();

        for(int i = 0; i < types.length; i++){
            sampleCustomer(types[i], tables[i]);
        }

        assert check();
    }

    public void sampleCustomer(byte type, int table){
        TypeSeatingArrangement tsa;
        double tw, r, cuSum;
        int zeroIndex;

        tsa = seatingArrangement.get(type);

        tsa.sa[table]--;
        tsa.typeCustomers--;
        customers--;

        assert tsa.sa[table] >= 0 : "If negative something went wrong since we should only" +
                "be removing the correct number of customers from each table";

        if(tsa.sa[table] == 0){
            tsa.typeTables--;
            tables--;
            if(parent != null){
                parent.unseat(type);
            }
        }

        if(parent != null){
            tw = (double) tsa.typeCustomers - (double) tsa.typeTables * discount + (double) tables * discount * parent.predictiveProbability(type);
        } else {
            tw = (double) tsa.typeCustomers - (double) tsa.typeTables * discount + (double) tables * discount * baseDistribution.probability(type);
        }

        r = ByteSequenceMemoizer.RNG.nextDouble();
        cuSum = 0.0;
        zeroIndex = -1;
        for(int i = 0; i < tsa.sa.length; i++){
            if(tsa.sa[i] == 0){
                zeroIndex = i;
            }

            cuSum += ((double) tsa.sa[i] - discount) / tw;
            if(cuSum > r){
                tsa.sa[i]++;
                tsa.typeCustomers++;
                customers++;
                return;
            }
        }

        tsa.typeCustomers++;
        tsa.typeTables++;
        customers++;
        tables++;

        if(zeroIndex > -1){
            tsa.sa[zeroIndex] = 1;
        } else {
            int[] newsa;

            newsa = new int[tsa.sa.length + 1];
            System.arraycopy(tsa.sa, 0, newsa, 0, tsa.sa.length);
            newsa[tsa.sa.length] = 1;

            tsa.sa = newsa;
        }

        if(parent != null){
            parent.seat(type);
        }
    }

    public void populateCustomersAndTables(byte[] types, int[] customersAndTables) {
        assert customersAndTables.length == 2 * types.length;
        int tci, tti;
        TypeSeatingArrangement tsa;

        tci = 0;
        tti = 1;
        for (byte type : types) {
            tsa = seatingArrangement.get(type);
            customersAndTables[tci] = tsa.typeCustomers;
            customersAndTables[tti] = tsa.typeTables;

            tci += 2;
            tti += 2;
        }
    }

    private Pair<byte[], int[]> randomCustomersToSample() {
        int n, index;
        byte[] types;
        int[] tables, randomOrder;
        TypeSeatingArrangement ts;

        n = 0;
        for (TypeSeatingArrangement tsa : seatingArrangement.values()) {
            if (tsa.typeCustomers != 1) {
                n += tsa.typeCustomers;
            }
        }

        types = new byte[n];
        tables = new int[n];
        randomOrder = SampleWithoutReplacement.SampleWithoutReplacement(n, ByteSequenceMemoizer.RNG);

        n = 0;
        for (Byte type : seatingArrangement.keySet()) {
            ts = seatingArrangement.get(type);
            if (ts.typeCustomers > 1) {
                for (int i = 0; i < ts.sa.length; i++) {
                    for (int c = 0; c < ts.sa[i]; c++) {
                        index = randomOrder[n++];
                        types[index] = type;
                        tables[index] = i;
                    }
                }
            }
        }

        return new Pair(types, tables);
    }

    public boolean check() {
        int t, c;
        t = 0;
        c = 0;
        for (TypeSeatingArrangement tsa : seatingArrangement.values()) {
            tsa.check();

            c += tsa.typeCustomers;
            t += tsa.typeTables;
        }
        assert c == customers;
        assert t == tables;
        return true;
    }

    private class TypeSeatingArrangement {

        public int typeCustomers, typeTables;
        private int[] sa;

        public TypeSeatingArrangement(int[] seatingArrangement, int typeCustomers, int typeTables) {
            sa = seatingArrangement;
            this.typeCustomers = typeCustomers;
            this.typeTables = typeTables;
        }

        public boolean seat(double pp) {
            int[] newsa;
            double r, cuSum, tw;

            if (typeCustomers == 0) {
                typeCustomers = 1;
                typeTables = 1;
                sa = new int[]{1};
                return true;
            }

            tw = (double) typeCustomers - (double) typeTables * discount + (double) tables * discount * pp;
            r = ByteSequenceMemoizer.RNG.nextDouble();
            cuSum = 0.0;
            for (int i = 0; i < typeTables; i++) {
                cuSum += ((double) sa[i] - discount) / tw;
                if (cuSum > r) {
                    sa[i]++;
                    typeCustomers++;
                    return false;
                }
            }

            newsa = new int[sa.length + 1];
            System.arraycopy(sa, 0, newsa, 0, sa.length);
            newsa[sa.length] = 1;

            sa = newsa;
            typeCustomers++;
            typeTables++;

            return true;
        }

        public boolean unseat() {
            double r, cuSum;

            if (typeCustomers <= 0) {
                throw new RuntimeException("unseating in an empty seating arrangment");
            } else if (typeCustomers == 1) {
                typeCustomers = 0;
                typeTables = 0;
                sa = null;
                return true;
            }

            r = ByteSequenceMemoizer.RNG.nextDouble();
            cuSum = 0.0;
            for (int i = 0; i < typeTables; i++) {
                cuSum += (double) sa[i] / (double) typeCustomers;
                if (cuSum > r) {
                    typeCustomers--;
                    if (sa[i] == 1) {
                        int[] newsa;

                        newsa = new int[sa.length - 1];
                        System.arraycopy(sa, 0, newsa, 0, i);
                        System.arraycopy(sa, i + 1, newsa, 0, sa.length - 1 - i);

                        sa = newsa;
                        typeTables--;

                        return true;
                    } else {
                        sa[i]--;
                        return false;
                    }
                }
            }

            throw new RuntimeException("should not get to here since we need to delete someone");
        }

        public boolean check() {
            int c = 0, t = 0;
            for (int cust : sa) {
                c += cust;
                if(cust > 0){
                    t++;
                }
            }
            assert c == typeCustomers : "c = " + c + " typeCustomers = " + typeCustomers;
            assert t == typeTables;
            return true;
        }
    }
}
