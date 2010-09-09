/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer.v1;

import edu.columbia.stat.wood.util.ByteDiscreteDistribution;
import edu.columbia.stat.wood.util.Pair;
import edu.columbia.stat.wood.util.SampleWithoutReplacement;
import gnu.trove.iterator.TByteObjectIterator;
import gnu.trove.map.hash.TByteObjectHashMap;

/**
 *
 * @author nicholasbartlett
 */
public class ByteSamplingNode {
    
    private TByteObjectHashMap<TypeSeatingArrangement> seatingArrangement;
    private ByteSamplingNode parent;
    private int tables, customers;
    private double discount;
    private ByteDiscreteDistribution baseDistribution;

    public ByteSamplingNode(ByteSamplingNode parent, double discount, ByteDiscreteDistribution baseDistribution) {
        this.parent = parent;
        this.discount = discount;
        seatingArrangement = new TByteObjectHashMap<TypeSeatingArrangement>();
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
            
            p *= (double) tables * discount / (double) customers;
            
            TypeSeatingArrangement tsa = seatingArrangement.get(type);
            
            if (tsa != null) {
                p += ((double) tsa.typeCustomers - (double) tsa.typeTables * discount) / (double) customers;
            }
        }

        return p;
    }

    public void seat(byte type) {
        double pp;
        if(parent == null){
            pp = baseDistribution.probability(type);
        } else {
            pp = parent.predictiveProbability(type);
        }
        
        TypeSeatingArrangement tsa = seatingArrangement.get(type);

        if (tsa.seat(pp)) {
            tables++;
            if (parent != null) {
                parent.seat(type);
            }
        }
        
        customers++;
    }

    public void unseat(byte type) {
        TypeSeatingArrangement tsa = seatingArrangement.get(type);

        assert tsa != null : "Should not be null since I'm not removing types during sampling";

        if (tsa.unseat()) {
            tables--;
            if (parent != null) {
                parent.unseat(type);
            }
        }

        customers--;
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

    public void fillRestaurant(ByteRestaurant r){
        populateCustomersAndTables(r.types, r.customersAndTables);
        r.customers = customers;
        r.tables = tables;
    }

    private void populateCustomersAndTables(byte[] types, int[] customersAndTables) {
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

        assert check(types.length, customersAndTables);
    }

    private boolean check(int l, int[] customersAndTables) {
        int c = 0, t = 0, tci = 0, tti = 1;
        for(int i = 0; i < l; i++){
            c += customersAndTables[tci];
            t += customersAndTables[tti];

            tci += 2;
            tti += 2;
        }

        return c == customers && t == tables;
    }

    private Pair<byte[], int[]> randomCustomersToSample() {
        int n, index;
        byte[] types;
        int[] tables, randomOrder;
        TypeSeatingArrangement ts;
        byte type;

        n = 0;
        TByteObjectIterator<TypeSeatingArrangement> iterator = seatingArrangement.iterator();
        while(iterator.hasNext()){
            iterator.advance();
            if(iterator.value().typeCustomers != 1){
                n += iterator.value().typeCustomers;
            }
        }

        types = new byte[n];
        tables = new int[n];
        randomOrder = SampleWithoutReplacement.sampleWithoutReplacement(n, ByteSequenceMemoizer.RNG);

        n = 0;
        iterator = seatingArrangement.iterator();
        while(iterator.hasNext()){
            iterator.advance();
            ts = iterator.value();
            type = iterator.key();
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

        TByteObjectIterator<TypeSeatingArrangement> iterator = seatingArrangement.iterator();
        while(iterator.hasNext()){
            iterator.advance();

            iterator.value().check();
            c += iterator.value().typeCustomers;
            t += iterator.value().typeTables;
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
            if (typeCustomers == 0) {
                typeCustomers = 1;
                typeTables = 1;
                sa = new int[]{1};
                return true;
            }

            double tw = (double) typeCustomers - (double) typeTables * discount + (double) tables * discount * pp;
            double r = ByteSequenceMemoizer.RNG.nextDouble();
            double cuSum = 0.0;

            assert typeTables == sa.length;

            for (int i = 0; i < typeTables; i++) {
                cuSum += ((double) sa[i] - discount) / tw;
                if (cuSum > r) {
                    sa[i]++;
                    typeCustomers++;
                    return false;
                }
            }

            int[] newsa = new int[sa.length + 1];
            System.arraycopy(sa, 0, newsa, 0, typeTables);
            newsa[typeTables] = 1;

            sa = newsa;
            typeCustomers++;
            typeTables++;

            return true;
        }

        public boolean unseat() {
            if (typeCustomers <= 0) {
                throw new RuntimeException("unseating in an empty seating arrangment");
            } else if (typeCustomers == 1) {
                typeCustomers = 0;
                typeTables = 0;
                sa = null;
                return true;
            }

            double r = ByteSequenceMemoizer.RNG.nextDouble();
            double cuSum = 0.0;
            for (int i = 0; i < typeTables; i++) {
                cuSum += ((double) sa[i]) / (double) typeCustomers;
                if (cuSum > r) {
                    typeCustomers--;
                    if (sa[i] == 1) {
                        int[] newsa;

                        newsa = new int[sa.length - 1];
                        System.arraycopy(sa, 0, newsa, 0, i);
                        System.arraycopy(sa, i + 1, newsa, i, sa.length - 1 - i);

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
