/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import edu.columbia.stat.wood.util.ByteSequence;
import edu.columbia.stat.wood.util.DoubleStack;
import edu.columbia.stat.wood.util.MersenneTwisterFast;
import edu.columbia.stat.wood.util.SeatingArranger;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */
public class ConstantSpaceSequenceMemoizer extends BytePredictiveModel {

    public static MersenneTwisterFast RNG;
    public double minP = 5.01 / (double) Integer.MAX_VALUE;
    public double adj = 1.0 + 257.0 * minP;
    private int depth;
    private ConstantSpaceRestaurant ecr;
    private ByteSequence bs;
    private Discounts discounts;
    private DoubleStack ds;
    private double[] mostOfPDF;
    private double p;
    private SeatReturn sr;
    private int rDepth;
    
    private DiscreteDistribution baseDistribution;


    public ConstantSpaceSequenceMemoizer(int depth, long seed) {
        this.depth = depth;
        RNG = new MersenneTwisterFast(seed);
        ecr = new ConstantSpaceRestaurant(null, 0, 0);
        bs = new ByteSequence(Integer.MAX_VALUE);
        discounts = new Discounts(new double[]{0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95}, 0.5);
        ds = new DoubleStack();
        sr = new SeatReturn();
        range = new ByteRange();
        SeatingArranger.rng = RNG;
    }

    public ConstantSpaceSequenceMemoizer(int depth, long seed, int maxByteSeqLength) {
        this(depth, seed);
        bs = new ByteSequence(maxByteSeqLength);
    }

    public double continueSequence(byte b) {
        ConstantSpaceRestaurant r;
        int index;
        double multFactor;

        r = getWithInsertion();

        index = ds.index();
        multFactor = predictiveProbability(r, b);
        p += multFactor * 1.0 / 257.0;

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
            high += (multFactor * (1.0 / 257.0) + mostOfPDF[i] + minP) / adj;
        }
        low = high - (multFactor * (1.0 / 257.0) + mostOfPDF[type] + minP) / adj;

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
            high += (multFactor * (1.0 / 257.0) + mostOfPDF[++type] + minP) / adj;
        }

        if (high <= pointOnCDF) {
            range.decode = 256;
        } else {
            low = high - (multFactor * (1.0 / 257.0) + mostOfPDF[type] + minP) / adj;

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
        double multFactor, low, high;

        r = getWithInsertion();
        multFactor = fillMostOfPDF(r);

        low = 0.0;

        for (int i = 0; i < mostOfPDF.length; i++) {
            low += multFactor * 1.0 / 257.0 + mostOfPDF[i];
        }

        low += minP * 256.0;
        range.low = low / adj;
        range.high = 1.0;
    }

    private ConstantSpaceRestaurant getWithInsertion() {
        int index, overlap, el;
        double discount;
        ConstantSpaceRestaurant r, c, nc;
        byte key;

        assert ds.index() == -1;

        r = ecr;
        rDepth = 0;
        index = bs.lastIndex;
        ds.push(discounts.get(0));

        while (true) {

            if (index == -1 || rDepth == depth) {
                return r;
            }

            key = bs.get(index);

            c = r.get(key);

            if (c == null) {
                el = depth - rDepth < index + 1 ? depth - rDepth : index + 1;
                ds.push(discounts.get(rDepth, rDepth + el));
                c = new ConstantSpaceRestaurant(r, index - el + 1, el);
                r.put(key, c);

                index -= el;
                rDepth += el;
                r = c;
            } else {

                overlap = bs.getOverlap(c.edgeStart, c.edgeLength, index);
                if (overlap == c.edgeLength) {
                    ds.push(discounts.get(rDepth, rDepth + overlap));
                    index -= overlap;
                    rDepth += overlap;
                    c.edgeStart = index + 1;
                    r = c;
                } else {
                    discount = discounts.get(rDepth, rDepth + overlap);
                    ds.push(discount);
                    nc = c.fragmentForInsertion(r, index - overlap + 1, overlap, discounts.get(rDepth, rDepth + c.edgeLength), discount);

                    index -= overlap;
                    rDepth += overlap;

                    r.put(key, nc);
                    nc.put(bs.get(c.edgeStart + c.edgeLength - 1), c);
                    r = nc;
                }
            }
        }
    }

    public double fillMostOfPDF(ConstantSpaceRestaurant r) {
        int tci, tti, customers;
        double multFactor, discount;
        byte[] types;
        int[] cAndT;

        multFactor = 1.0;
        mostOfPDF = new double[256];
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

    public static void main(String[] args) throws IOException {
        File f;
        BufferedInputStream bis;
        int b, index;
        double logLik;

        ConstantSpaceSequenceMemoizer sm;
        sm = new ConstantSpaceSequenceMemoizer(15, 1);

        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/AliceInWonderland.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/pride_and_prejudice.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/calgary_corpus/geo");
        f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/wikipedia/first2m.txt");

        bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(f));

            logLik = 0.0;
            index = 0;
            while ((b = bis.read()) > -1) {
                //if(index++ % 100000 == 0){
                //    System.out.println("Bytes = " + (index-1) + " : Restaurants = " + ConstantSpaceRestaurant.count);
                //}
                logLik -= sm.continueSequence((byte) b);
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
        }

        System.out.println();

        System.out.println(ConstantSpaceRestaurant.count);
        System.out.println(logLik / Math.log(2.0) / f.length());
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
