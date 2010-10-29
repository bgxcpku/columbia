/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer.v1.util;


import edu.columbia.stat.wood.sequencememoizer.v1.ByteSequenceMemoizer;
import edu.columbia.stat.wood.sequencememoizer.v1.util.ByteSeq.ByteSeqNode;
import edu.columbia.stat.wood.sequencememoizer.v1.ByteSequenceMemoizer.SeatReturn;
import edu.columbia.stat.wood.util.ByteMap;
import edu.columbia.stat.wood.util.SampleMultinomial;
import edu.columbia.stat.wood.util.SeatingArranger;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author nicholasbartlett
 */
public class ByteRestaurant extends ByteMap<ByteRestaurant> implements Serializable{

    static final long serialVersionUID = 1;

    public byte[] types;
    public int[] customersAndTables;
    public int customers, tables, edgeStart, edgeLength, numLeafNodesAtOrBelow;
    public ByteRestaurant parent;
    public ByteSeqNode edgeNode;

    public static int count = 0;

    public ByteRestaurant(ByteRestaurant parent, int edgeStart, int edgeLength, ByteSeqNode edgeNode, int numLeafNodesAtOrBelow) {
        this.parent = parent;
        this.edgeStart = edgeStart;
        this.edgeLength = edgeLength;
        this.edgeNode = edgeNode;
        this.numLeafNodesAtOrBelow = numLeafNodesAtOrBelow;

        if(edgeNode != null){
            edgeNode.add(this);
        }
        customers = 0;
        tables = 0;
        count++;
    }

    public void setTableConfig(byte[] types, int[] customersAndTables, int customers, int tables) {
        this.types = types;
        this.customersAndTables = customersAndTables;
        this.customers = customers;
        this.tables = tables;
    }

    public double getPP(byte type, double p, double discount, SeatReturn sr) {
        int index, tc, tt, tci, tti;

        if(type > types[types.length -1]){
            tt = 0;
            tc = 0;
        } else {
            index = getIndex(type);
            if(types[index] == type){
                tci = 2 * index;
                tti = tci + 1;

                tc = customersAndTables[tci];
                tt = customersAndTables[tti];
            } else {
                tt = 0;
                tc = 0;
            }
        }

        sr.set(false, tt, customers, tables);
        p -= ((double) tc - (double) tt * discount) / (double) customers;

        return p * (double) customers / ((double) tables * discount);
    }

    public void deleteCustomers(int nDelete, double discount) {
        int[] c = new int[types.length];

        for (int t = 0; t < types.length; t++) {
            c[t] = customersAndTables[2 * t];
        }

        int[] toDelete = SampleMultinomial.deleteCustomersAtRandom(nDelete, c, customers, ByteSequenceMemoizer.RNG);
        int number_zeros = 0;
        for (int t = 0; t < types.length; t++) {
            if (toDelete[t] > 0) {
                if(toDelete[t] == customersAndTables[2*t]){
                    customers -= toDelete[t];
                    tables -= customersAndTables[2 * t + 1];
                    
                    customersAndTables[2 * t] = 0;
                    customersAndTables[2 * t + 1] = 0;

                    number_zeros++;
                } else {

                    int[] sa = SeatingArranger.getSeatingArrangement(customersAndTables[2 * t], customersAndTables[2 * t + 1], discount);
                    int[] cToDelete = SampleMultinomial.deleteCustomersAtRandom(toDelete[t], sa, customersAndTables[2 * t], ByteSequenceMemoizer.RNG);

                    customersAndTables[2 * t] -= toDelete[t];
                    customers -= toDelete[t];

                    for (int i = 0; i < sa.length; i++) {
                        if (sa[i] == cToDelete[i]) {
                            tables--;
                            customersAndTables[2 * t + 1]--;
                            assert customersAndTables[2 * t + 1] > 0;
                        }
                    }
                    assert customersAndTables[2*t] >= customersAndTables[2*t +1];
                }
            }
        }

        if(number_zeros > 0){
            byte[] new_types = new byte[types.length - number_zeros];
            int[] new_customersAndTables = new int[customersAndTables.length - 2 * number_zeros];

            int j = 0, k = 0;
            for(int i = 0; i < types.length; i++){
                if(customersAndTables[2*i] > 0){
                    new_types[j++] = types[i];
                    new_customersAndTables[k++] = customersAndTables[2*i];
                    new_customersAndTables[k++] = customersAndTables[2*i + 1];
                    if(new_customersAndTables[k-2] < new_customersAndTables[k-1] || new_customersAndTables[k-2] == 0 || new_customersAndTables[k-1] == 0){
                        throw new RuntimeException("new_customersAndTables[k-1] = " + new_customersAndTables[k-1] + ", new_customersAndTables[k-2] = " + new_customersAndTables[k-2]);
                    }
                }
            }

            assert k == 2*(types.length - number_zeros) : "k = " + k + ", 2*(types.length - number_zeros) = "  + 2*(types.length - number_zeros);

            types = new_types;
            customersAndTables = new_customersAndTables;
        }
    }

    public double seat(byte type, double p, double discount, SeatReturn sr, ByteSequenceMemoizer sm) {

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

                if (numerator / denominator > ByteSequenceMemoizer.RNG.nextDouble()) {
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

        if (customers >= sm.maxCustomersInRestaurant) {
            deleteCustomers((int) (sm.maxCustomersInRestaurant * .1),discount);
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

    public ByteRestaurant fragmentForInsertion(ByteRestaurant irParent, int irEdgeStart, int irEdgeLength, ByteSeqNode irEdgeNode, double discount, double irDiscount) {
        double fragDiscount, fragConcentration, numerator, denominator;
        ByteRestaurant intermediateRestaurant;
        byte[] irTypes;
        int[] irCustomersAndTables, tsa;
        int l, tci, tti, fc, ft, tc, tt, irc, irt;

        int start_customers = customers;
        int[] start_customersAndTables = new int[customersAndTables.length];
        System.arraycopy(customersAndTables,0, start_customersAndTables, 0, customersAndTables.length);

        
        customers = 0;
        tables = 0;

        intermediateRestaurant = new ByteRestaurant(irParent, irEdgeStart, irEdgeLength, irEdgeNode, numLeafNodesAtOrBelow);

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
                    if (numerator / denominator > ByteSequenceMemoizer.RNG.nextDouble()) {
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

        /*int c = 0;
        int t = 0;
        for(int i = 0; i < start_customersAndTables.length; i++){
            assert start_customersAndTables[i] == customersAndTables[i];
            assert customersAndTables[i] >= customersAndTables[i + 1];
            c += start_customersAndTables[i++];
            assert start_customersAndTables[i] <= customersAndTables[i];
            t += customersAndTables[i];
            assert customersAndTables[i] > 0;
        }
        assert c == customers;
        assert t == tables;*/

        intermediateRestaurant.setTableConfig(irTypes, irCustomersAndTables, irc, irt);
        parent = intermediateRestaurant;
        edgeStart += irEdgeLength;
        edgeLength -= irEdgeLength;

        return intermediateRestaurant;
    }
    
    public ByteRestaurant fragmentForPrediction(ByteRestaurant irParent, double discount, double irDiscount){
        ByteRestaurant intermediateRestaurant;
        double fragDiscount, fragConcentration, numerator, denominator;
        byte[] irTypes;
        int[] irCustomersAndTables, tsa;
        int l, irc, irt, tci, tti, tc, tt, fc, ft;

        intermediateRestaurant = new ByteRestaurant(irParent, 0, 0, null, 0);
        count--;
        
        if(types != null){
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
                        if (numerator / denominator > ByteSequenceMemoizer.RNG.nextDouble()) {
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
            }

            intermediateRestaurant.setTableConfig(irTypes, irCustomersAndTables, irc, irt);
        }
        return intermediateRestaurant;
    }

    public final void removeFromTree(){
        parent.remove(edgeNode.byteChunk()[edgeStart]);
        if(!parent.isEmpty()){
            parent.decrementLeafNodeCount();
        }
        count--;
    }

    public final void removeFromTreeAndEdgeNode(){
        edgeNode.remove(this);
        parent.remove(edgeNode.byteChunk()[edgeStart]);
        if(!parent.isEmpty()){
            parent.decrementLeafNodeCount();
        }
        count--;
    }

    public void incrementLeafNodeCount(){
        numLeafNodesAtOrBelow++;
        if(parent != null) parent.incrementLeafNodeCount();
    }

    public void decrementLeafNodeCount(){
        numLeafNodesAtOrBelow--;
        if(parent != null) parent.decrementLeafNodeCount();
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        out.writeObject(types);
        out.writeObject(customersAndTables);
        out.writeInt(customers);
        out.writeInt(tables);
        out.writeInt(edgeStart);
        out.writeInt(edgeLength);
        out.writeInt(numLeafNodesAtOrBelow);
        out.writeObject(parent);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        types = (byte[]) in.readObject();
        customersAndTables = (int[]) in.readObject();
        customers = in.readInt();
        tables = in.readInt();
        edgeStart = in.readInt();
        edgeLength = in.readInt();
        numLeafNodesAtOrBelow = in.readInt();
        parent = (ByteRestaurant) in.readObject();
    }

    public void check(){
        int c = 0;
        int t = 0;

        for(int i = 0 ; i < types.length; i++){
            assert customersAndTables[2*i] >= customersAndTables[2*i + 1] : printCustomersAndTables();
            assert customersAndTables[2*i] > 0;
            assert customersAndTables[2*i + 1] > 0;
            if(i > 0){
                assert types[i] > types[i-1];
            }
            c += customersAndTables[2*i];
            t += customersAndTables[2*i + 1];
        }

        assert customers == c;
        assert tables == t;
    }

    public String printCustomersAndTables(){
        String c_and_t = "[" + customersAndTables[0];
        for(int i = 1; i < customersAndTables.length; i++){
            c_and_t += ", " + customersAndTables[i];
        }
        c_and_t += "]";
        return c_and_t;
    }
}