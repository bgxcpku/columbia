/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import edu.columbia.stat.wood.sequencememoizer.ByteSeq.BackwardsIterator;
import edu.columbia.stat.wood.sequencememoizer.ByteSeq.ByteSeqNode;
import edu.columbia.stat.wood.util.ByteArrayFiniteDiscreteDistribution;
import edu.columbia.stat.wood.util.ByteCompleteUniformDiscreteDistribution;
import edu.columbia.stat.wood.util.ByteDiscreteDistribution;
import edu.columbia.stat.wood.util.ByteFiniteDiscreteDistribution;
import edu.columbia.stat.wood.util.DoubleStack;
import edu.columbia.stat.wood.util.LogBracketFunction;
import edu.columbia.stat.wood.util.LogGeneralizedSterlingNumbers;
import edu.columbia.stat.wood.util.MersenneTwisterFast;
import edu.columbia.stat.wood.util.MutableInt;
import edu.columbia.stat.wood.util.SeatingArranger;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author nicholasbartlett
 */

public class ByteSequenceMemoizer extends BytePredictiveModel implements ByteSequenceMemoizerInterface {

    public static MersenneTwisterFast RNG;
    public double minP = 5.01 / (double) Integer.MAX_VALUE;
    public double adj;
    private int depth, rDepth;
    private ByteRestaurant ecr;
    public ByteSeq bs;
    private Discounts discounts;
    private DoubleStack ds;
    private double[] mostOfPDF;
    private SeatReturn sr;
    private ByteFiniteDiscreteDistribution baseDistribution;
    private MutableInt newKey = new MutableInt(-1);
    private long maxNumberRestaurants = Long.MAX_VALUE, maxSequenceLength = Long.MAX_VALUE;

    public ByteSequenceMemoizer(int depth, long seed) {
        this.depth = depth;
        RNG = new MersenneTwisterFast(seed);
        ecr = new ByteRestaurant(null, 0, 0, null,1);
        bs = new ByteSeq(1024);
        discounts = new Discounts(new double[]{0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95}, 0.5);
        ds = new DoubleStack();
        sr = new SeatReturn();
        SeatingArranger.rng = RNG;
        baseDistribution = new ByteCompleteUniformDiscreteDistribution();

        adj = 1.0 + (double) (baseDistribution.alphabetSize() + 1) * minP;
    }

    public int getRestaurantCount(ByteRestaurant r) {
        int count;

        count = 0;
        for (ByteRestaurant child : r.values()) {
            count += getRestaurantCount(child);
        }

        return ++count;
    }

    public void limitMemory(long maxNumberRestaurants, long maxSequenceLength) {
        this.maxNumberRestaurants = maxNumberRestaurants;
        this.maxSequenceLength = maxSequenceLength;
    }

    public double continueSequence(byte[] types) {
        double logLik;

        logLik = 0.0;
        for (byte b : types) {
            logLik += continueSequence(b);
        }

        return logLik;
    }

    public byte[] generate(byte[] context, int numSamples) {
        ByteDiscreteDistribution dist;
        Iterator<edu.columbia.stat.wood.util.Pair<Byte, Double>> iter;
        double r, cuSum;
        byte[] samples;
        edu.columbia.stat.wood.util.Pair<Byte, Double> pair;

        if (context == null) {
            context = new byte[0];
        }

        dist = predictiveDistribution(context);
        samples = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            iter = dist.iterator();

            r = RNG.nextDouble();
            cuSum = 0.0;
            while (true) {
                pair = iter.next();
                cuSum += pair.second().doubleValue();
                if (cuSum > r) {
                    break;
                }
            }

            samples[i] = pair.first().byteValue();
        }

        return samples;
    }

    public byte[] generateSequence(byte[] context, int sequenceLength) {
        byte[] fullSequence, c, r;
        int index;

        if (context == null) {
            context = new byte[0];
        }

        fullSequence = new byte[context.length + sequenceLength];
        index = context.length;

        for (int i = 0; i < sequenceLength; i++) {
            c = new byte[index];
            System.arraycopy(fullSequence, 0, c, 0, index);
            fullSequence[index++] = generate(c, 1)[0];
        }

        assert index == fullSequence.length;

        r = new byte[sequenceLength];
        System.arraycopy(fullSequence, context.length, r, 0, sequenceLength);

        return r;
    }

    public ByteDiscreteDistribution predictiveDistribution(byte[] context) {
        double[] pdf;
        double multFactor;

        if (context == null) {
            context = new byte[0];
        }

        pdf = new double[256];
        multFactor = fillMostOfPDF(getWithoutInsertion(context));
        for (int i = 0; i < 256; i++) {
            pdf[i] = mostOfPDF[i] + multFactor * baseDistribution.probability((byte) i);
        }

        return new ByteArrayFiniteDiscreteDistribution(pdf);
    }

    public double predictiveProbability(byte[] context, byte token) {
        if (context == null) {
            context = new byte[0];
        }
        return predictiveProbability(getWithoutInsertion(context), token);
    }

    public double sequenceProbability(byte[] context, byte[] sequence) {
        byte[] fullSequence, c;
        int index, l;
        double logLik;

        if (context == null) {
            context = new byte[0];
        }

        fullSequence = new byte[context.length + sequence.length];
        System.arraycopy(context, 0, fullSequence, 0, context.length);
        System.arraycopy(sequence, 0, fullSequence, context.length, sequence.length);

        index = context.length;

        logLik = 0.0;
        for (int i = 0; i < sequence.length; i++) {
            l = index < depth ? index : depth;
            c = new byte[l];
            System.arraycopy(fullSequence, index - l, c, 0, l);
            logLik += Math.log(predictiveProbability(c, fullSequence[index]));
            index++;
        }

        return logLik;
    }

    public void sampleSeatingArrangements(int numSweeps) {
        for(int i = 0; i < numSweeps; i++){
            sampleSeatingArrangements(ecr, null, 0);
        }
    }

    public double sampleDiscounts(int numSweeps){
        if(numSweeps > 0){
            double score = 0.0;
            for(int i = 0; i < numSweeps; i++){
                score = sampleDiscounts(0.07);
            }
            return score;
        } else {
            return score();
        }
    }

    public void sampleSeatingArrangements(ByteRestaurant r, ByteSamplingNode parentbsn, int d) {
        ByteSamplingNode bsn;
        double discount;
        int tci, tti, c, t;

        discount = discounts.get(d - r.edgeLength, d);
        bsn = new ByteSamplingNode(parentbsn, discount, baseDistribution);

        tci = 0;
        tti = 1;
        for (byte type : r.types) {
            c = r.customersAndTables[tci];
            t = r.customersAndTables[tti];

            bsn.setTypeSeatingArrangement(type, SeatingArranger.getSeatingArrangement(c, t, discount), c, t);

            tci += 2;
            tti += 2;
        }

        for (ByteRestaurant child : r.values()) {
            sampleSeatingArrangements(child, bsn, d + child.edgeLength);
        }

        bsn.sample();
        bsn.populateCustomersAndTables(r.types, r.customersAndTables);
    }

     private double sampleDiscounts(double stdProposal){
        double logLik, pLogLik, currentValue, proposal;
        boolean accept;

        logLik = score();

        for(int dIndex = 0; dIndex < discounts.length(); dIndex++){
            currentValue = discounts.get(dIndex);
            proposal = currentValue + stdProposal * RNG.nextGaussian();

            if(proposal > 0.0 && proposal < 1.0){
                discounts.set(dIndex, proposal);
                pLogLik = score();

                accept = RNG.nextDouble() < Math.exp(pLogLik - logLik);
                if(accept){
                    logLik = pLogLik;
                } else {
                    discounts.set(dIndex,currentValue);
                }
            }
        }

        currentValue = discounts.getdInfinity();
        proposal = currentValue + stdProposal * RNG.nextGaussian();

        if(proposal > 0.0 && proposal < 1.0){
            discounts.setDInfinity(proposal);
            pLogLik = score();

            accept = RNG.nextDouble() < Math.exp(pLogLik - logLik);
            if(accept){
                logLik = pLogLik;
            } else {
                discounts.setDInfinity(currentValue);
            }
        }

        return logLik;
    }

    public double score() {
        double logLik;
        int tti;

        logLik = 0.0;
        tti = 1;
        for (int i = 0; i < ecr.types.length; i++) {
            logLik += ecr.customersAndTables[tti] * Math.log(baseDistribution.probability(ecr.types[i]));
            tti += 2;
        }

        return logLik += score(ecr, 0);
    }

    public double score(ByteRestaurant r, int restaurantDepth) {
        double logLik, discount;
        LogGeneralizedSterlingNumbers lgsn;
        int tci, tti;

        logLik = 0.0;
        if(!r.isEmpty()){
            for(Object child : r.arrayValues()){
                logLik += score((ByteRestaurant)child ,restaurantDepth + ((ByteRestaurant) child).edgeLength);
            }
        }

        discount = discounts.get(restaurantDepth - r.edgeLength, restaurantDepth);
        lgsn = new LogGeneralizedSterlingNumbers(discount);

        logLik += LogBracketFunction.logBracketFunction(discount, r.tables - 1, discount);
        logLik -= LogBracketFunction.logBracketFunction(1, r.customers - 1, 1.0);

        tci = 0;
        tti = 1;
        for (int i = 0; i < r.types.length; i++) {
            logLik += lgsn.get(r.customersAndTables[tci], r.customersAndTables[tti]);
            tci += 2;
            tti += 2;
        }

        return logLik;
    }

    public SequenceMemoizerParameters getParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double continueSequence(byte b) {
        ByteRestaurant r;
        int index;
        double p;

        while(bs.length() > maxSequenceLength - 1){
            bs.shorten();
        }

        while(ByteRestaurant.count > maxNumberRestaurants - 2){
            deleteRandomRestaurant();
        }

        r = getWithInsertion();

        index = ds.index();
        p = predictiveProbability(r, b);

        ds.setIndex(index);
        seatAndUpdateDiscount(b, r, p);

        bs.append(b);

        return Math.log(p);
    }

    public void continueSequenceEncode(byte b) {
        ByteRestaurant r;
        double multFactor, l, h;
        int type, index;

        while(bs.length() > maxSequenceLength - 1){
            bs.shorten();
        }

        while(ByteRestaurant.count > maxNumberRestaurants - 2){
            deleteRandomRestaurant();
        }

        r = getWithInsertion();
        index = ds.index();
        multFactor = fillMostOfPDF(r);

        h = 0.0;
        type = (int) b & 0xFF;
        for (int i = 0; i <= type; i++) {
            h += (multFactor * baseDistribution.probability((byte) i) + mostOfPDF[i] + minP) / adj;
        }
        l = h - (multFactor * baseDistribution.probability(b) + mostOfPDF[type] + minP) / adj;

        low = l;
        high = h;

        ds.setIndex(index);
        seatAndUpdateDiscount(b, r, (h - l) * adj - minP);

        bs.append(b);
    }

    public void continueSequenceDecode(double pointOnCDF) {
        ByteRestaurant r;
        int index, type;
        double multFactor, l, h;

        while(bs.length() > maxSequenceLength - 1){
            bs.shorten();
        }

        while(ByteRestaurant.count > maxNumberRestaurants - 2){
            deleteRandomRestaurant();
        }

        r = getWithInsertion();
        index = ds.index();
        multFactor = fillMostOfPDF(r);

        h = 0.0;
        type = -1;
        while (h <= pointOnCDF && type < 255) {
            type++;
            h += (multFactor * baseDistribution.probability((byte) type) + mostOfPDF[type] + minP) / adj;
        }

        if (h <= pointOnCDF) {
            decode = 256;
        } else {
            l = h - (multFactor * baseDistribution.probability((byte) type) + mostOfPDF[type] + minP) / adj;

            low = l;
            high = h;
            decode = type;

            ds.setIndex(index);
            seatAndUpdateDiscount((byte) type, r, (h - l) * adj - minP);

            bs.append((byte) type);
        }
    }

    public void endOfStream() {
        ByteRestaurant r;
        double multFactor, l;

        while(bs.length() > maxSequenceLength - 1){
            bs.shorten();
        }

        while(ByteRestaurant.count > maxNumberRestaurants - 2){
            deleteRandomRestaurant();
        }

        r = getWithInsertion();
        multFactor = fillMostOfPDF(r);

        l = 0.0;

        for (int i = 0; i < mostOfPDF.length; i++) {
            l += (multFactor * baseDistribution.probability((byte) i) + mostOfPDF[i] + minP) / adj;
        }

        low = l;
        high = 1.0;
    }

    public ByteRestaurant getWithInsertion() {
        int el;
        double discount;
        ByteRestaurant r, c, nc;
        byte key;
        BackwardsIterator bi;

        assert ds.index() == -1;

        r = ecr;
        rDepth = 0;
        bi = bs.backwardsIterator();

        ds.push(discounts.get(0));
        while (rDepth < depth && bi.hasNext()) {
            key = bi.peek();
            c = r.get(key);

            if (c == null) {
                el = bi.available(depth - rDepth);
                ds.push(discounts.get(rDepth, rDepth + el));
                c = new ByteRestaurant(r, bi.ind, el, bi.node,1);
                
                if(!r.isEmpty()){
                    r.incrementLeafNodeCount();
                }

                r.put(key, c);

                rDepth += el;

                return c;
            } else {
                int currentEdgeStart = bi.ind;
                ByteSeqNode currentNode = bi.node;

                int overlap = bi.overlap(c.edgeNode, c.edgeStart, c.edgeLength, newKey);

                assert overlap > 0;

                if (overlap == c.edgeLength) {
                    ds.push(discounts.get(rDepth, rDepth + overlap));
                    rDepth += overlap;
                    
                    c.edgeNode.remove(c);
                    c.edgeNode = currentNode;
                    c.edgeNode.add(c);
                    c.edgeStart = currentEdgeStart;
                    r = c;
                } else {
                    discount = discounts.get(rDepth, rDepth + overlap);
                    ds.push(discount);

                    nc = c.fragmentForInsertion(r, currentEdgeStart, overlap, currentNode, discounts.get(rDepth, rDepth + c.edgeLength), discount);
                    rDepth += overlap;

                    r.put(key, nc);
                    if(newKey.intValue() > -1){
                        nc.put((byte) newKey.intValue(), c);
                        if(c.edgeStart >= bs.blockSize()){
                            c.edgeStart %= bs.blockSize();
                            c.edgeNode.remove(c);
                            c.edgeNode = c.edgeNode.previous();
                            c.edgeNode.add(c);
                        }
                    } else {
                        c.edgeNode.remove(c);
                    }
         
                    r = nc;
                }
            }   
        }

        return r;
    }

    public ByteRestaurant getWithoutInsertion(byte[] context) {
        int index;
        double discount;
        ByteRestaurant r, c;
        byte key;

        assert ds.index() == -1;

        r = ecr;
        rDepth = 0;
        index = context.length - 1;

        ds.push(discounts.get(0));
        while (rDepth < depth && index > -1) {
            key = context[index];
            c = r.get(key);

            if (c == null) {
                return r;
            } else {
                int overlap = bs.overlap(c.edgeNode, c.edgeStart, c.edgeLength, context, index);

                assert overlap > 0;

                index -= overlap;

                if (overlap == c.edgeLength) {
                    ds.push(discounts.get(rDepth, rDepth + overlap));
                    rDepth += overlap;
                    r = c;
                } else {
                    discount = discounts.get(rDepth, rDepth + overlap);
                    ds.push(discount);
                    
                    c = c.fragmentForPrediction(r, discounts.get(rDepth, rDepth + c.edgeLength), discount);

                    rDepth += overlap;

                    return c;
                }
            }   
        }

        return r;
    }

    private double fillMostOfPDF(ByteRestaurant r) {
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
                    mostOfPDF[(int) types[i] & 0xFF] += multFactor * (cAndT[tci] - discount * cAndT[tti]) / customers;

                    tci += 2;
                    tti += 2;
                }

                multFactor *= (double) r.tables * discount / (double) r.customers;
            }

            r = r.parent;
        }

        return multFactor;
    }

    private void seatAndUpdateDiscount(byte type, ByteRestaurant r, double p) {
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

    private double predictiveProbability(ByteRestaurant r, byte type) {
        double discount, multFactor, p;
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

        p += multFactor * baseDistribution.probability(type);

        return p;
    }

    public ByteRestaurant getRandomLeafNode(){
        ByteRestaurant r = ecr;
        double totalWeight, cuSum, rand;
        
        while(!r.isEmpty()){
            totalWeight = r.numLeafNodesAtOrBelow;
            rand = RNG.nextDouble();
            cuSum = 0.0;
            for(Object child : r.arrayValues()){
                cuSum += (double) ((ByteRestaurant) child).numLeafNodesAtOrBelow / totalWeight;
                if(cuSum > rand){
                    r = (ByteRestaurant) child;
                    break;
                }
            }
        }

        return r;
    }

    public void deleteRandomRestaurant(){
        getRandomLeafNode().removeFromTreeAndEdgeNode();
    }

    public void check(ByteRestaurant r){
        for(ByteRestaurant child : r.values()){
            check(child);
        }

        if(r.edgeNode != null){
            assert r.edgeNode.contains(r);
        }
    }

    public int check1(ByteRestaurant r){
        int childrenAtOrBelow = 0;
        if(r.isEmpty()){
            childrenAtOrBelow++;
        } else {
            for(ByteRestaurant child : r.values()){
                childrenAtOrBelow += check1(child);
            }
        }

        assert childrenAtOrBelow == r.numLeafNodesAtOrBelow : "Calculated as " + childrenAtOrBelow + " : but recorded as " +  r.numLeafNodesAtOrBelow;
        return childrenAtOrBelow;
    }

    public static void main(String[] args) throws IOException {
        File f;
        BufferedInputStream bis;
        int b, index;
        double logLik;

        ByteSequenceMemoizer sm;
        sm = new ByteSequenceMemoizer(1023, 3);
        //sm.limitMemory(Long.MAX_VALUE, 1000000000);

        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/AliceInWonderland.txt");
        f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/pride_and_prejudice.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/calgary_corpus/geo");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/wikipedia/first8m.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/wikipedia/enwik8");
        //f = new File("/Users/nicholasbartlett/Downloads/test");

        bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(f));

            logLik = 0.0;

            index = 0;
            while ((b = bis.read()) > -1) {
                if(index++ % 100000 == 0){
                    System.out.println("Bytes = " + index + " : Restaurants = " + ByteRestaurant.count);
                }
                logLik -= sm.continueSequence((byte) b);
                //sm.continueSequenceEncode((byte) b );
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
        }

        //sm.check(sm.ecr);
        //sm.check1(sm.ecr);

        //System.out.println(sm.ecr.numLeafNodesAtOrBelow);

        //System.out.println(sm.bs.restaurantCount());

        System.out.println(logLik / Math.log(2.0) / f.length());
        System.out.println(ByteRestaurant.count);

        System.out.println(sm.score());
        sm.sampleSeatingArrangements(5);
        System.out.println(sm.score());

        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/wikipedia/first8m.txt");
        f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/AliceInWonderland.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/pride_and_prejudice.txt");
        byte[] file = new byte[(int) f.length()];

        double ll;
        try {
            bis = new BufferedInputStream(new FileInputStream(f));

            index = 0;
            ll = 0.0;
            while ((b = bis.read()) > -1) {
                ll -= sm.continueSequence((byte) b);
                file[index++] = (byte) b;
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
        }

        //ll = sm.sequenceProbability(null, file);

        System.out.println();
        System.out.println(ByteRestaurant.count);
        System.out.println(-ll / Math.log(2.0) / f.length());
 
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
}
