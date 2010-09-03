/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer.v1;

import edu.columbia.stat.wood.sequencememoizer.BytePredictiveModel;
import edu.columbia.stat.wood.sequencememoizer.v1.ByteSeq.BackwardsIterator;
import edu.columbia.stat.wood.sequencememoizer.v1.ByteSeq.ByteSeqNode;
import edu.columbia.stat.wood.util.ByteArrayFiniteDiscreteDistribution;
import edu.columbia.stat.wood.util.ByteDiscreteDistribution;
import edu.columbia.stat.wood.util.DoubleStack;
import edu.columbia.stat.wood.util.LogBracketFunction;
import edu.columbia.stat.wood.util.LogGeneralizedSterlingNumbers;
import edu.columbia.stat.wood.util.MersenneTwisterFast;
import edu.columbia.stat.wood.util.MutableInt;
import edu.columbia.stat.wood.util.SeatingArranger;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;

/**
 *
 * @author nicholasbartlett
 */
public class ByteSequenceMemoizer extends BytePredictiveModel implements ByteSequenceMemoizerInterface, Serializable{

    static final long serialVersionUID = 1 ;

    public static MersenneTwisterFast RNG;

    public static final double minP = 5.01 / (double) Integer.MAX_VALUE;
    public static final double adj = 1.0 + 257.0 * minP;

    private int depth, rDepth, trueDepth;
    private ByteRestaurant ecr;
    private ByteSeq bs;
    private Discounts discounts;
    private DoubleStack ds;
    private double[] mostOfPDF;
    private SeatReturn sr;
    private ByteDiscreteDistribution baseDistribution;
    private MutableInt newKey = new MutableInt(-1);
    private long maxNumberRestaurants, maxSequenceLength, seed;

    public ByteSequenceMemoizer(ByteSequenceMemoizerParameters parameters) {
        RNG = new MersenneTwisterFast(parameters.seed);
        trueDepth = parameters.depth;
        depth = 0;
        rDepth = 0;
        ecr = new ByteRestaurant(null, 0, 0, null, 1);
        bs = new ByteSeq(1024);
        discounts = new Discounts(parameters.discounts, parameters.infiniteDiscount);
        ds = new DoubleStack();
        sr = new SeatReturn();
        baseDistribution = parameters.baseDistribution;
        maxNumberRestaurants = parameters.maxNumberRestaurants;
        maxSequenceLength = parameters.maxSequenceLength;
        seed = parameters.seed;

        SeatingArranger.rng = RNG;
    }

    public ByteSequenceMemoizer() {
        this(new ByteSequenceMemoizerParameters());
    }

    public void setDepth(int depth){
        trueDepth = depth;
    }

    public void setMaxNumberRestaurants(long maxNumberRestaurants){
        this.maxNumberRestaurants = maxNumberRestaurants;
    }

    public void setMaxSequenceLength(long maxSequenceLength){
        this.maxSequenceLength = maxSequenceLength;
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        out.writeObject(RNG);
        out.writeInt(depth);
        out.writeInt(trueDepth);
        out.writeObject(bs);
        out.writeObject(discounts);
        out.writeObject(baseDistribution);
        out.writeLong(maxNumberRestaurants);
        out.writeLong(maxSequenceLength);
        out.writeLong(seed);
        out.writeInt(ByteRestaurant.count);
        out.writeObject(ecr);

        writeEdgeNodeObjects(out);
    }

    private void writeEdgeNodeObjects(ObjectOutputStream out) throws IOException{
        if(!ecr.isEmpty()){
            for(Object c : ecr.values()){
                writeEdgeNodeObjects((ByteRestaurant) c, out);
            }
        }
    }

    private void writeEdgeNodeObjects(ByteRestaurant r, ObjectOutputStream out) throws IOException{
        if(!r.isEmpty()){
            for(Object c : r.values()){
                writeEdgeNodeObjects((ByteRestaurant) c, out);
            }
        }
        
        out.writeInt(r.edgeNode.getIndex());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        RNG = (MersenneTwisterFast) in.readObject();
        depth = in.readInt();
        trueDepth = in.readInt();
        bs = (ByteSeq) in.readObject();
        discounts = (Discounts) in.readObject();
        baseDistribution = (ByteDiscreteDistribution) in.readObject();
        maxNumberRestaurants = in.readLong();
        maxSequenceLength = in.readLong();
        seed = in.readLong();
        ByteRestaurant.count = in.readInt();

        ecr = (ByteRestaurant) in.readObject();

        readEdgeNodeObjects(in);
        SeatingArranger.rng = RNG;

        rDepth = 0;
        ds = new DoubleStack();
        sr = new SeatReturn();
        newKey = new MutableInt(-1);
    }

    private void readEdgeNodeObjects(ObjectInputStream in) throws IOException{
        if(!ecr.isEmpty()){
            for(Object c : ecr.values()){
                readEdgeNodeObjects((ByteRestaurant) c, in);
            }
        }
    }

    private void readEdgeNodeObjects(ByteRestaurant r, ObjectInputStream in) throws IOException{
        if(!r.isEmpty()){
            for(Object c : r.values()){
                readEdgeNodeObjects((ByteRestaurant) c, in);
            }
        }

        r.edgeNode = bs.get(in.readInt());
    }

    public void newSequence(){
        depth = 0;
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

    public double sample(int numSweeps){
        for(int i = 0; i < numSweeps - 1; i++){
            sampleSeatingArrangements(1);
            sampleDiscounts(1);
        }

        sampleSeatingArrangements(1);
        return sampleDiscounts(1);
    }

    public void sampleSeatingArrangements(int numSweeps) {
        for (int i = 0; i < numSweeps; i++) {
            sampleSeatingArrangements(ecr, null, 0);
        }
    }

    public double sampleDiscounts(int numSweeps) {
        if (numSweeps > 0) {
            double score = 0.0;
            for (int i = 0; i < numSweeps; i++) {
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

        if (!r.isEmpty()) {
            for (Object child : r.values()) {
                sampleSeatingArrangements((ByteRestaurant) child, bsn, d + ((ByteRestaurant) child).edgeLength);
            }
        }

        bsn.sample();
        bsn.fillRestaurant(r);
    }

    private double sampleDiscounts(double stdProposal) {
        double logLik, pLogLik, currentValue, proposal;
        boolean accept;

        logLik = score();

        for (int dIndex = 0; dIndex < discounts.length(); dIndex++) {
            currentValue = discounts.get(dIndex);
            proposal = currentValue + stdProposal * RNG.nextGaussian();

            if (proposal > 0.0 && proposal < 1.0) {
                discounts.set(dIndex, proposal);
                pLogLik = score();

                accept = RNG.nextDouble() < Math.exp(pLogLik - logLik);
                if (accept) {
                    logLik = pLogLik;
                } else {
                    discounts.set(dIndex, currentValue);
                }
            }
        }

        currentValue = discounts.getdInfinity();
        proposal = currentValue + stdProposal * RNG.nextGaussian();

        if (proposal > 0.0 && proposal < 1.0) {
            discounts.setDInfinity(proposal);
            pLogLik = score();

            accept = RNG.nextDouble() < Math.exp(pLogLik - logLik);
            if (accept) {
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
        if (!r.isEmpty()) {
            for (Object child : r.values()) {
                logLik += score((ByteRestaurant) child, restaurantDepth + ((ByteRestaurant) child).edgeLength);
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

    public ByteSequenceMemoizerParameters getParameters() {
        double[] d = new double[discounts.length()];
        for (int i = 0; i < discounts.length(); i++) {
            d[i] = discounts.get(i);
        }

        return new ByteSequenceMemoizerParameters(baseDistribution, d, discounts.getdInfinity(), depth, seed, this.maxNumberRestaurants, this.maxSequenceLength);
    }

    public double continueSequence(byte b) {
        ByteRestaurant r;
        int index;
        double p;

        while (bs.length() > maxSequenceLength - 1) {
            bs.shorten();
        }

        while (ByteRestaurant.count > maxNumberRestaurants - 2) {
            deleteRandomRestaurant();
        }

        r = getWithInsertion();

        index = ds.index();
        p = predictiveProbability(r, b);

        ds.setIndex(index);
        seatAndUpdateDiscount(b, r, p);

        bs.append(b);

        if(depth < trueDepth){
            depth++;
        }

        return Math.log(p);
    }

    public void continueSequenceEncode(byte b) {
        ByteRestaurant r;
        double multFactor, l, h;
        int type, index;

        while (bs.length() > maxSequenceLength - 1) {
            bs.shorten();
        }

        while (ByteRestaurant.count > maxNumberRestaurants - 2) {
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

        assert h < 1.0;

        ds.setIndex(index);
        seatAndUpdateDiscount(b, r, (h - l) * adj - minP);

        bs.append(b);

        if(depth < trueDepth){
            depth++;
        }
    }

    public void continueSequenceDecode(double pointOnCDF) {
        ByteRestaurant r;
        int index, type;
        double multFactor, l, h;

        while (bs.length() > maxSequenceLength - 1) {
            bs.shorten();
        }

        while (ByteRestaurant.count > maxNumberRestaurants - 2) {
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

            if(depth < trueDepth){
                depth++;
            }
        }
    }

    private byte[] mostRecentContext = null;
    public void continueSequenceEncodeWithoutInsertion(byte b){
        //fill in mostRecentContext byte array
        if(mostRecentContext == null){
            mostRecentContext = new byte[trueDepth];
            int index = 0;
            BackwardsIterator bi = bs.backwardsIterator();
            while(bi.hasNext() && index < depth){
                mostRecentContext[trueDepth - 1 - index] = bi.next();
            }
        }

        //get restaurant
        ByteRestaurant r;
        if(depth < trueDepth){
            byte[] context = new byte[depth];
            System.arraycopy(mostRecentContext,trueDepth - depth, context,0,depth);
            r = getWithoutInsertion(context);
        } else {
            r = getWithoutInsertion(mostRecentContext);
        }

        System.arraycopy(mostRecentContext,1,mostRecentContext, 0, trueDepth -1);
        mostRecentContext[trueDepth - 1] = b;

        double multFactor = fillMostOfPDF(r);

        double h = 0.0;
        int type = (int) b & 0xFF;
        for (int i = 0; i <= type; i++) {
            h += (multFactor * baseDistribution.probability((byte) i) + mostOfPDF[i] + minP) / adj;
        }
        double l = h - (multFactor * baseDistribution.probability(b) + mostOfPDF[type] + minP) / adj;

        low = l;
        high = h;

        if(depth < trueDepth){
            depth++;
        }
    }

    public void continueSequenceDecodeWithoutInsertion(double pointOnCDF){
        //fill in mostRecentContext byte array
        if(mostRecentContext == null){
            mostRecentContext = new byte[trueDepth];
            int index = 0;
            BackwardsIterator bi = bs.backwardsIterator();
            while(bi.hasNext() && index < depth){
                mostRecentContext[trueDepth - 1 - index] = bi.next();
            }
        }

        //get restaurant
        ByteRestaurant r;
        if(depth < trueDepth){
            byte[] context = new byte[depth];
            System.arraycopy(mostRecentContext,trueDepth - depth, context,0,depth);
            r = getWithoutInsertion(context);
        } else {
            r = getWithoutInsertion(mostRecentContext);
        }

        double multFactor = fillMostOfPDF(r);
        double h = 0.0;
        int type = -1;
        while (h <= pointOnCDF && type < 255) {
            type++;
            h += (multFactor * baseDistribution.probability((byte) type) + mostOfPDF[type] + minP) / adj;
        }

        if (h <= pointOnCDF) {
            decode = 256;
        } else {
            double l = h - (multFactor * baseDistribution.probability((byte) type) + mostOfPDF[type] + minP) / adj;

            low = l;
            high = h;
            decode = type;

            if(depth < trueDepth){
                depth++;
            }

            System.arraycopy(mostRecentContext,1,mostRecentContext, 0, trueDepth -1);
            mostRecentContext[trueDepth - 1] = (byte) type;
        }
    }

    public void endOfStream() {
        ByteRestaurant r;
        double multFactor, l;

        while (bs.length() > maxSequenceLength - 1) {
            bs.shorten();
        }

        while (ByteRestaurant.count > maxNumberRestaurants - 2) {
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
                c = new ByteRestaurant(r, bi.ind, el, bi.node, 1);

                if (!r.isEmpty()) {
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
                    if (newKey.intValue() > -1) {
                        nc.put((byte) newKey.intValue(), c);
                        if (c.edgeStart >= bs.blockSize()) {
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

    public ByteRestaurant getWithoutInsertion() {
        double discount;
        ByteRestaurant r, c;
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
                return r;
            } else {
                int overlap = bi.overlap(c.edgeNode, c.edgeStart, c.edgeLength, newKey);

                assert overlap > 0;

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

    private ByteRestaurant getRandomLeafNode() {
        ByteRestaurant r = ecr;
        double totalWeight, cuSum, rand;

        while (!r.isEmpty()) {
            totalWeight = r.numLeafNodesAtOrBelow;
            rand = RNG.nextDouble();
            cuSum = 0.0;
            for (Object child : r.values()) {
                cuSum += (double) ((ByteRestaurant) child).numLeafNodesAtOrBelow / totalWeight;
                if (cuSum > rand) {
                    r = (ByteRestaurant) child;
                    break;
                }
            }
        }

        return r;
    }

    private void deleteRandomRestaurant() {
        getRandomLeafNode().removeFromTreeAndEdgeNode();
    }

    public void check(ByteRestaurant r){
        if(!r.isEmpty()){
            for(Object c : r.values()){
                check((ByteRestaurant) c);
            }
        }

        r.check();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File f;
        BufferedInputStream bis = null;
        int b;

        ByteSequenceMemoizer sm = null;
        sm = new ByteSequenceMemoizer(new ByteSequenceMemoizerParameters(1023,Long.MAX_VALUE,Long.MAX_VALUE));

        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/AliceInWonderland.txt");
        f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/pride_and_prejudice.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/AliceInWonderland.10k.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/calgary_corpus/geo");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/wikipedia/first8m.txt");
        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/wikipedia/enwik8");
        //f = new File("/Users/nicholasbartlett/Downloads/test");

        try {
            bis = new BufferedInputStream(new FileInputStream(f));

            while ((b = bis.read()) > -1) {
                sm.continueSequence((byte) b);
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
        }

        /*
        System.out.println(sm.score());
        for(int i = 0; i < 15; i++){
            sm.sampleSeatingArrangements(1);
            System.out.println(sm.sampleDiscounts(0.07));
        }

        
        sm.sampleSeatingArrangements(10);
        System.out.println(sm.score());*/

        
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream("/Users/nicholasbartlett/Documents/np_bayes/data/test/sm");
            oos = new ObjectOutputStream(fos);
            oos.writeObject(sm);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

        /*ByteSequenceMemoizer smm = null;
        ObjectInputStream ois = null;
        try{
            ois = new ObjectInputStream(new FileInputStream("/Users/nicholasbartlett/Documents/np_bayes/data/test/sm"));
            smm = (ByteSequenceMemoizer) ois.readObject();
        } finally {
            ois.close();
        }

        System.out.println(ByteRestaurant.count);
*/
        /*f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/AliceInWonderland.txt");

        //f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/pride_and_prejudice.txt");

        sm.newSequence();

        try {
            bis = new BufferedInputStream(new FileInputStream(f));
            double logLik = 0.0;
            while((b = bis.read()) > -1){
                //System.out.println(sm.continueSequence((byte) b));
                logLik -= sm.continueSequence((byte) b);
            }
            System.out.println(logLik / Math.log(2) / f.length());
        } finally {
            bis.close();
        }

        sm.check(sm.ecr);*/
    }

    public class SeatReturn implements Serializable{

        static final long serialVersionUID = 1;

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
