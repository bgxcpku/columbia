/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import edu.columbia.stat.wood.util.ArraySet;
import edu.columbia.stat.wood.util.BigInt;
import edu.columbia.stat.wood.util.ByteSequence2;
import edu.columbia.stat.wood.util.ByteSequence2.ByteSequenceBackwardIterator;
import edu.columbia.stat.wood.util.DoubleStack;
import edu.columbia.stat.wood.util.MersenneTwisterFast;
import edu.columbia.stat.wood.util.SeatingArranger;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

/**
 *
 * @author nicholasbartlett
 */
public class ConstantSpaceSequenceMemoizer extends BytePredictiveModel implements ByteSequenceMemoizer {

    public static MersenneTwisterFast RNG;

    public double minP = 5.01 / (double) Integer.MAX_VALUE;
    public double adj;

    private int depth, rDepth;
    private ConstantSpaceRestaurant ecr;
    public static ByteSequence2 bs;
    private Discounts discounts;
    private DoubleStack ds;
    private double[] mostOfPDF;
    private double p;
    private SeatReturn sr;
    private edu.columbia.stat.wood.util.FiniteDiscreteDistribution baseDistribution;

    public ConstantSpaceSequenceMemoizer(int depth, long seed) {
        this.depth = depth;
        RNG = new MersenneTwisterFast(seed);
        ecr = new ConstantSpaceRestaurant(null, 0, 0, null, (byte) 0);
        bs = new ByteSequence2(1024);
        discounts = new Discounts(new double[]{0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95}, 0.5);
        ds = new DoubleStack();
        sr = new SeatReturn();
        range = new ByteRange();
        SeatingArranger.rng = RNG;
        baseDistribution = new edu.columbia.stat.wood.util.UniformDiscreteDistribution(0, 257);
        adj= 1.0 + (double) baseDistribution.alphabetSize() * minP;
    }

    /*************Memory Management********************************************/

    public void limitMemory(int maxNumberRestaurants){
        ConstantSpaceRestaurant.restaurants = new ArraySet(maxNumberRestaurants);
        //throw new RuntimeException("not supported yet");
    }

    public int getRestaurantCount(ConstantSpaceRestaurant r){
        int count;

        count = 0;
        for(ConstantSpaceRestaurant child : r.values()){
            count += getRestaurantCount(child);
        }

        r.added = true;
        return ++count;
    }

    private void monitorMemory(){
        if(ConstantSpaceRestaurant.restaurants != null){
            if(ConstantSpaceRestaurant.count >= ConstantSpaceRestaurant.restaurants.maxIndex()){
                ConstantSpaceRestaurant.removeRestaurants(2);
            }
        }
    }

    /************Sequential Estimation*****************************************/


    public double continueSequence(byte b) {
        ConstantSpaceRestaurant r;
        int index;
        double multFactor;

        monitorMemory();

        r = getWithInsertion();

        index = ds.index();
        multFactor = predictiveProbability(r, b);
        
        p += multFactor * baseDistribution.probability((int) b & 0xFF);

        ds.setIndex(index);
        seatAndUpdateDiscount(b, r, p);

        bs.append(b);

        return Math.log((p + minP) / adj);
    }

    public void continueSequenceEncode(byte b) {
        ConstantSpaceRestaurant r;
        double multFactor, low, high;
        int type, index;

        r = getWithInsertion();
        index = ds.index();
        multFactor = fillMostOfPDF(r);

        high = 0.0;
        type = (int) b & 0xFF;
        for (int i = 0; i <= type; i++) {
            high += (multFactor * baseDistribution.probability(i) + mostOfPDF[i] + minP) / adj;
        }
        low = high - (multFactor * baseDistribution.probability(type) + mostOfPDF[type] + minP) / adj;

        range.low = low;
        range.high = high;

        ds.setIndex(index);
        seatAndUpdateDiscount(b, r, (high - low) * adj - minP);

        bs.append(b);
    }

    public void continueSequenceDecode(double pointOnCDF) {
        ConstantSpaceRestaurant r;
        int index, type;
        double multFactor, low, high;

        r = getWithInsertion();
        index = ds.index();
        multFactor = fillMostOfPDF(r);

        high = 0.0;
        type = -1;
        while (high <= pointOnCDF && type < 255) {
            type++;
            high += (multFactor * baseDistribution.probability(type) + mostOfPDF[type] + minP) / adj;
        }

        if (high <= pointOnCDF){
            range.decode = 256;
        } else {
            low = high - (multFactor * baseDistribution.probability(type) + mostOfPDF[type] + minP) / adj;

            range.low = low;
            range.high = high;
            range.decode = type;

            ds.setIndex(index);
            seatAndUpdateDiscount((byte) type, r, (high - low) * adj - minP);

            bs.append((byte) type);
        }
    }

    public void endOfStream() {
        ConstantSpaceRestaurant r;
        double multFactor, low;

        r = getWithInsertion();
        multFactor = fillMostOfPDF(r);

        low = 0.0;

        for (int i = 0; i < mostOfPDF.length; i++) {
            low += (multFactor * baseDistribution.probability(i) + mostOfPDF[i] + minP) / adj;
        }

        range.low = low;
        range.high = 1.0;
    }

    public ConstantSpaceRestaurant getWithInsertion() {
        int overlap, el;
        double discount;
        ConstantSpaceRestaurant r, c, nc;
        byte key;
        ByteSequenceBackwardIterator bi;

        assert ds.index() == -1;

        r = ecr;
        rDepth = 0;
        bi = bs.backwardsIterator();

        ds.push(discounts.get(0));
        while (rDepth < depth && bi.hasNext()) {
            key = bi.peek();
            c = r.get(key);

            if (c == null) {
                el = depth - rDepth < bi.available() ? depth - rDepth : bi.available();
                ds.push(discounts.get(rDepth, rDepth + el));
                c = new ConstantSpaceRestaurant(r, bi.index(), el, bi.key(), key);
                r.put(key, c);

                Collection t = r.values();
                int l = t.size();

                rDepth += el;

                return c;
            } else {
                ByteSequenceBackwardIterator biEdge;
                BigInt currentKey;
                int currentEdgeStart;

                currentKey = bi.key();
                currentEdgeStart = bi.index();

                biEdge = bs.backwardsIterator(c.edgeKey, c.edgeStart);
                if (biEdge == null) {
                    throw new RuntimeException("not implemented yet");
                } else {
                    
                    overlap = 0;
                    while (biEdge.hasNext() && overlap < c.edgeLength && biEdge.peek() == bi.peek()) {
                        biEdge.next();
                        bi.next();
                        overlap++;
                    }

                    assert overlap > 0;

                    if (overlap == c.edgeLength) {
                        ds.push(discounts.get(rDepth, rDepth + overlap));
                        rDepth += overlap;

                        c.edgeKey = currentKey;
                        c.edgeStart = currentEdgeStart;

                        r = c;
                    } else {
                        discount = discounts.get(rDepth, rDepth + overlap);
                        ds.push(discount);

                        nc = c.fragmentForInsertion(r, currentEdgeStart, overlap, currentKey, discounts.get(rDepth, rDepth + c.edgeLength), discount);
                        rDepth += overlap;

                        r.put(key, nc);
                        if (biEdge.hasNext()) {
                            nc.put(biEdge.peek(), c);
                            c.key = biEdge.peek();
                            if (c.edgeStart >= bs.blockSize()) {
                                c.edgeStart %= bs.blockSize();
                                c.edgeKey = c.edgeKey.previous();
                            }
                        }

                        r = nc;
                    }
                }
            }
        }

        return r;
    }

    private double fillMostOfPDF(ConstantSpaceRestaurant r) {
        int tci, tti, customers;
        double multFactor, discount;
        byte[] types;
        int[] cAndT;

        multFactor = 1.0;
        mostOfPDF = new double[baseDistribution.alphabetSize() < 256 ? baseDistribution.alphabetSize() : 256];
        while (ds.hasNext()) {
            discount = ds.pop();

            if (r.customers > 0) {
                types = r.types;
                cAndT = r.customersAndTables;
                customers = r.customers;

                tci = 0;
                tti = 1;

                for (int i = 0; i < types.length; i++) {
                    mostOfPDF[(int) types[i] & 0xFF] += multFactor * ((double) cAndT[tci] - (double) cAndT[tti] * discount) / (double) customers;

                    tci += 2;
                    tti += 2;
                }

                multFactor *= (double) r.tables * discount / (double) r.customers;
            }

            r = r.parent;
        }

        return multFactor;
    }

    private void seatAndUpdateDiscount(byte type, ConstantSpaceRestaurant r, double p) {
        double discount, multFactor;
        double keepP;

        keepP = p;

        sr.seatInParent = true;
        multFactor = 1.0;
        while (ds.hasNext() && sr.seatInParent) {
            discount = ds.pop();
            p = r.seat(type, p, discount, sr);

            discounts.updateGradient(rDepth - r.edgeLength, rDepth, sr.typeTables, sr.customers, sr.tables, p, discount, multFactor);
            if (sr.customers > 0) {
                multFactor *= (double) sr.tables * discount / (double) sr.customers;
            }

            rDepth -= r.edgeLength;
            r = r.parent;
        }

        while (ds.hasNext()) {
            discount = ds.pop();
            p = r.getPP(type, p, discount, sr);

            discounts.updateGradient(rDepth - r.edgeLength, rDepth, sr.typeTables, sr.customers, sr.tables, p, discount, multFactor);
            multFactor *= (double) sr.tables * discount / (double) sr.customers;

            rDepth -= r.edgeLength;
            r = r.parent;
        }

        discounts.stepDiscounts(0.0001, keepP);

        assert rDepth == 0;
        assert r == null;
    }

    private double predictiveProbability(ConstantSpaceRestaurant r, byte type) {
        double discount, multFactor;
        int index, tci, tti;

        multFactor = 1.0;
        p = 0.0;
        while (ds.hasNext()) {
            discount = ds.pop();
            if (r.customers > 0) {
                if (type <= r.types[r.types.length - 1]) {
                    index = r.getIndex(type);
                    if (r.types[index] == type) {
                        tci = 2 * index;
                        tti = tci + 1;
                        p += multFactor * ((double) r.customersAndTables[tci] - (double) r.customersAndTables[tti] * discount) / (double) r.customers;
                    }
                }
                multFactor *= (double) r.tables * discount / (r.customers);
            }

            r = r.parent;
        }

        return multFactor;
    }

    public void printStarts(ConstantSpaceRestaurant r){

        for(ConstantSpaceRestaurant child : r.values()){
            printStarts(child);
        }
        
        if(r.edgeKey != null){
            System.out.println(r.edgeKey.intValue());
        }
    }

    public void checkKeys(ConstantSpaceRestaurant r){
        for(ConstantSpaceRestaurant child : r.values()){
            checkKeys(child);
        }

        if(r.edgeKey != null){
            ByteSequenceBackwardIterator bi = bs.backwardsIterator(r.edgeKey, r.edgeStart);
            assert bi.peek() == r.key;
        }
    }

    public void checkKeys2(ConstantSpaceRestaurant r){
        for(ConstantSpaceRestaurant child : r.values()){
            checkKeys2(child);
        }

        r.checkKeys();
    }

    public static void main(String[] args) throws IOException {
        File f;
        BufferedInputStream bis;
        int b, index;
        double logLik;

        ConstantSpaceSequenceMemoizer sm;
        sm = new ConstantSpaceSequenceMemoizer(15, 1);
        sm.limitMemory(2000000);
        
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/AliceInWonderland.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/pride_and_prejudice.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/calgary_corpus/geo");
        f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/wikipedia/first10m.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/wikipedia/enwik8");

        bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(f));

            logLik = 0.0;
            index = 0;
            while ((b = bis.read()) > -1) {
                if(index++ % 1000000 == 0){
                    System.out.println("Bytes = " + (index-1) + " : Restaurants = " + ConstantSpaceRestaurant.count);
                }
                //System.out.println(b);
                //index++;
                logLik -= sm.continueSequence((byte) b);
                
                //sm.checkKeys2(sm.ecr);
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
        }

        System.out.println();
        System.out.println(ConstantSpaceRestaurant.count);
        System.out.println(logLik / Math.log(2.0) / f.length());
        //sm.checkKeys2(sm.ecr);
        //sm.printStarts(sm.ecr);
    }

    public void limitMemory(long maxNumberRestaurants, long maxSequenceLength) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double continueSequence(byte[] types) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public byte[] generate(byte[] context, int numSamples) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public byte[] generateSequence(byte[] context, int sequenceLength) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DiscreteDistribution predictiveDistribution(byte[] context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double predictiveProbability(byte[] context, byte token) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double sequenceProbability(byte[] context, byte[] sequence) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double sample(int numSweeps) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double score() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SequenceMemoizerParameters getParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public class SeatReturn {

        public boolean seatInParent;
        public int typeTables, customers, tables;

        public void set(boolean seatInParent, int typeTables, int customers, int tables) {
            this.seatInParent = seatInParent;
            this.typeTables = typeTables;
            this.customers = customers;
            this.tables = tables;
        }
    }

    public static class ByteRange {

        public double low, high;
        public int decode;

        public void set(double low, double high) {
            this.low = low;
            this.high = high;
        }

        public void set(double low, double high, int decode) {
            this.low = low;
            this.high = high;
            this.decode = decode;
        }
    }
}
