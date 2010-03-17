/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

/**
 *
 * @author nicholasbartlett
 */
public class SMTree implements SequenceMemoizerImplementation {

    public int alphabetSize;
    public Restaurant contextFreeRestaurant;
    public Discounts discounts = null;
    public MemoizedSequence seq = new MemoizedSequence();
    public int maxNumberRest = -1;
    public int maxDepth = -1;
    public SeatingStyle smType = SeatingStyle.SIMPLE;
    private double obsLogProb = 0.0;
    public static Random RNG;

    public SMTree(int alphabetSize, int maxDepth, int maxNumberRest, SeatingStyle smType, int seed) {
        RNG = new Random(seed);

        this.alphabetSize = alphabetSize;
        contextFreeRestaurant = new Restaurant(null, null);

        this.smType = smType;
        this.maxDepth = maxDepth;
        this.maxNumberRest = maxNumberRest;
        Restaurant.numberRest = 0;

        double[] discount = {0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95};
        this.discounts = new Discounts(discount);
    }

    public void constrainMemory(int restaurants) {
        this.smType = SeatingStyle.RANDOM_DELETION;
        this.maxNumberRest = restaurants;
    }

    public void newSeq() {
        seq.incrementSeq();
    }

    public double continueSequence(int observation) {
        if (smType != SeatingStyle.SIMPLE) {
            if (Restaurant.numberRest > maxNumberRest - 2) {
                if (smType == SeatingStyle.RANDOM_DELETION) {
                    this.deleteRandomLeafNodes(100);
                } else if (smType == SeatingStyle.BAYES_FACTOR_DELETION) {
                    this.deleteLeastUsefullRestaurantsForLogProbOfData(100);
                }
            }
        }
        seq.add(observation);
        this.seatObs(contextFreeRestaurant, observation, seq.getLastElementIndex() - 1, 0, 1.0 / alphabetSize);
        discounts.stepGradient(0.0001, Math.exp(obsLogProb));
        return obsLogProb;
    }

    public double continueSequence(int[] observations) {
        double logLoss = 0.0;
        for (int i = 0; i < observations.length; i++) {
            logLoss += this.continueSequence(observations[i]);
        }
        return logLoss;
    }

    public int[] generate(int[] context, int numSamples) {
        int[] generatedData = new int[numSamples];
        seq.incrementSeq();
        seq.add(context);

        double cumSum;
        double rawRandom;
        double[] predDist;
        double[] initialPredDist = new double[alphabetSize];
        Arrays.fill(initialPredDist, 1.0 / alphabetSize);

        predDist = this.getPredictiveDist(contextFreeRestaurant, 0, seq.getLastElementIndex(), initialPredDist);
        seq.deleteSeq();
        topFor:
        for (int i = 0; i < numSamples; i++) {
            cumSum = 0.0;
            rawRandom = Math.random();
            for (int j = 0; j < alphabetSize; j++) {
                cumSum += predDist[j];
                if (cumSum > rawRandom) {
                    generatedData[i] = j;
                    continue topFor;
                }
            }
        }
        return generatedData;
    }

    public int[] generateSequence(int[] initialContext, int sequenceLength) {
        int[] generatedData = new int[sequenceLength];
        seq.incrementSeq();
        seq.add(initialContext);

        double cumSum;
        double rawRandom;
        double[] predDist;
        double[] initialPredDist = new double[alphabetSize];
        Arrays.fill(initialPredDist, 1.0 / alphabetSize);

        topFor:
        for (int i = 0; i < sequenceLength; i++) {
            cumSum = 0.0;
            rawRandom = Math.random();
            predDist = this.getPredictiveDist(contextFreeRestaurant, 0, seq.getLastElementIndex(), initialPredDist);
            for (int j = 0; j < alphabetSize; j++) {
                cumSum += predDist[j];
                if (cumSum > rawRandom) {
                    generatedData[i] = j;
                    seq.add(j);
                    continue topFor;
                }
            }
        }
        seq.deleteSeq();
        return generatedData;
    }

    public double[] predictiveProbability(int[] context, int[] tokens) {
        double[] predDist = this.predictiveProbability(context);
        double[] predDistTokens = new double[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            predDistTokens[i] = predDist[tokens[i]];
        }
        return predDistTokens;
    }

    public double[] predictiveProbability(int[] context) {
        seq.incrementSeq();
        seq.add(context);

        double[] initialPredDist = new double[alphabetSize];
        Arrays.fill(initialPredDist, 1.0 / alphabetSize);
        double[] predDist = this.getPredictiveDist(contextFreeRestaurant, 0, seq.getLastElementIndex(), initialPredDist);

        seq.deleteSeq();
        return predDist;
    }

    public double sequenceProbability(int[] context, int[] sequence) {
        seq.incrementSeq();
        seq.add(context);

        double[] initialPredDist = new double[alphabetSize];
        Arrays.fill(initialPredDist, 1.0 / alphabetSize);

        double[] predDist;
        double logProb = 0.0;

        for (int i = 0; i < sequence.length; i++) {
            predDist = this.getPredictiveDist(contextFreeRestaurant, 0, seq.getLastElementIndex(), initialPredDist);
            seq.add(sequence[i]);
            logProb += Math.log(predDist[sequence[i]]);
        }

        seq.deleteSeq();
        return Math.exp(logProb);
    }

    public double sample(int numSweeps) {
        double score = 0.0;
        for (int i = 0; i < numSweeps; i++) {
            this.sampleSeating();
            score = this.sampleDiscounts()[2];
        }
        if (numSweeps == 0) {
            score = this.score();
        }
        return score;
    }

    public double score() {
        return this.getLogLik();
    }

    public String getParameterNames() {
        throw new RuntimeException("not supported yet");
    }

    public String getParameterValues() {
        throw new RuntimeException("not supported yet");
    }

    private Restaurant seatObs(Restaurant rest, int observation, int contextIndex, int depth, double upperRestProbOfObs) {
        //initiate some variables to be used later for ease and readability
        //int depth = obsIndex - contextIndex - 1;
        boolean leafNode = (contextIndex == -1);
        if (maxDepth > -1) {
            leafNode = (leafNode || (depth == maxDepth));
        }

        //get the logDiscount for the restaurant rest
        double logDiscount;
        if (rest.parent != null) {
            logDiscount = discounts.getLog(depth - (rest.parentPath[1] - rest.parentPath[0]), depth);
        } else {
            logDiscount = discounts.getLog(0);
        }

        //calculate the predictive prob of this type in restaurant rest
        double prob;
        int[] restCounts = rest.getRestCounts(observation);
        if (restCounts[3] > 0) {
            prob = 1.0 * (restCounts[0] - Math.exp(Math.log(restCounts[1]) + logDiscount)) / restCounts[2] + Math.exp(Math.log(restCounts[3]) + logDiscount + Math.log(upperRestProbOfObs) - Math.log(restCounts[2]));
        } else {
            prob = upperRestProbOfObs;
        }

        //update the discount gradient information
        if (restCounts[3] > 0) {
            if (rest.parent == null) {
                discounts.addToGradient(restCounts[1], restCounts[3], restCounts[2], alphabetSize);
            } else {
                discounts.addToDiscountGradient(depth - rest.parentPath[1] + rest.parentPath[0] + 1, depth, restCounts[1], restCounts[3], restCounts[2], upperRestProbOfObs);
            }
        }

        //handle leaf nodes first
        if (leafNode) {
            int[] rootRestCounts = contextFreeRestaurant.getRestCounts(observation);
            double probRoot;
            if (rootRestCounts[3] > 0) {
                probRoot = 1.0 * (rootRestCounts[0] - Math.exp(Math.log(rootRestCounts[1]) + discounts.getLog(0))) / rootRestCounts[2] + Math.exp(Math.log(rootRestCounts[3]) + discounts.getLog(0) + Math.log(1.0 / alphabetSize) - Math.log(rootRestCounts[2]));
            } else {
                probRoot = 1.0 / alphabetSize;
            }

            obsLogProb = Math.log((99.0 * prob + probRoot) / 100);
            return rest.sitAtRest(observation, upperRestProbOfObs, logDiscount);
        }

        Restaurant childRest = rest.get(new Integer(seq.get(contextIndex)));

        while (true) {
            //CASE 1 : there are no children in the direction of this observation
            if (childRest == null) {
                int[] childParentPath = new int[3];
                childParentPath[1] = contextIndex + 1;
                childParentPath[2] = seq.getCurrentSeq();
                if (maxDepth > -1 && (contextIndex + 1 - (maxDepth - depth)) > 0) {
                    childParentPath[0] = contextIndex + 1 - (maxDepth - depth);
                } else {
                    childParentPath[0] = 0;
                }

                childRest = new Restaurant(rest, childParentPath);
                rest.putChild(childRest, seq);

                contextIndex = -1;
                break;
            }

            int diffIndex = seq.compareContexts(childRest.parentPath, contextIndex);

            //CASE 2 : there is a child exactly in the direction of this observation
            if (diffIndex == (childRest.parentPath[1] - childRest.parentPath[0])) {
                contextIndex -= diffIndex;
                break;
            }

            //CASE 3 : there is a child in a direction which shares some context
            //with this obs
            int[] rest2ParentPath = {0, contextIndex + 1, seq.getCurrentSeq()};
            contextIndex -= diffIndex;

            rest2ParentPath[0] = contextIndex + 1;

            childRest = childRest.fragmentAndAdd(rest2ParentPath, depth + rest2ParentPath[1] - rest2ParentPath[0], seq, discounts);
            break;
        }

        int childDepth = depth + childRest.parentPath[1] - childRest.parentPath[0];

        if (this.seatObs(childRest, observation, contextIndex, childDepth, prob) != null) {
            return rest.sitAtRest(observation, upperRestProbOfObs, logDiscount);
        } else {
            return null;
        }
    }
    /*
    public int[] generateData(int length){
    int[] generatedData = new int[length];
    seq.incrementSeq();

    double cumSum;
    double rawRandom;
    double[] predDist;
    double[] initialPredDist = new double[alphabetSize];
    Arrays.fill(initialPredDist, 1.0/alphabetSize);

    topFor:
    for(int i = 0; i<length; i++){
    cumSum = 0.0;
    rawRandom = Math.random();
    predDist = this.getPredictiveDist(contextFreeRestaurant, 0, seq.getLastElementIndex(), initialPredDist);
    for(int j = 0; j<alphabetSize; j++){
    cumSum += predDist[j];
    if(cumSum > rawRandom){
    generatedData[i] = j;
    this.seatObs(j, false, false);
    continue topFor;
    }
    }
    }
    return generatedData;
    }*/

    public double[] getPredictiveDist(Restaurant rest, int d, int contextIndex, double[] predDist) {
        //fill predictive counts on the way down
        double logDiscount;
        if (rest.parent == null) {
            logDiscount = discounts.getLog(0);
        } else {
            logDiscount = discounts.getLog(d - rest.parentPath[1] + rest.parentPath[0], d);
        }
        rest.fillPredictiveCounts(alphabetSize, Math.exp(logDiscount), 0);

        if (rest.predictiveCounts.cust == 0) {
            return predDist;
        }

        for (int i = 0; i < rest.predictiveCounts.typeNum.length; i++) {
            predDist[i] = predDist[i] * (rest.predictiveCounts.tables * rest.predictiveCounts.discount + rest.predictiveCounts.concentration) / (rest.predictiveCounts.cust + rest.predictiveCounts.concentration);
            predDist[i] += rest.predictiveCounts.typeNum[i] / (rest.predictiveCounts.cust + rest.predictiveCounts.concentration);
        }

        rest.predictiveCounts = null;

        if (contextIndex == -1) {
            return predDist;
        }

        Restaurant childRest;
        childRest = rest.get(new Integer(seq.get(contextIndex)));

        if (childRest == null) {
            return predDist;
        }

        int diffIndex = seq.compareContexts(childRest.parentPath, contextIndex);

        if (diffIndex == (childRest.parentPath[1] - childRest.parentPath[0])) {

            contextIndex -= diffIndex;

        } else {

            int[] rest2ParentPath = {0, contextIndex + 1, seq.getCurrentSeq()};
            contextIndex -= diffIndex;

            rest2ParentPath[0] = contextIndex + 1;

            childRest = childRest.fragment(rest2ParentPath, d + rest2ParentPath[1] - rest2ParentPath[0], seq, discounts);
        }
        return this.getPredictiveDist(childRest, d + childRest.parentPath[1] - childRest.parentPath[0], contextIndex, predDist);
    }

    public double getLogLik() {
        double logLik = 0.0;
        double logDisc = discounts.getLog(0);

        for (int i = 0; i < discounts.discounts.length; i++) {
            if (discounts.discounts[i] <= 0.0 || discounts.discounts[i] >= 1.0) {
                return Double.NEGATIVE_INFINITY;
            }
        }

        int custInRest = 0;
        for (int typeIndex = 0; typeIndex < contextFreeRestaurant.state.length; typeIndex++) {
            for (int table = 1; table < contextFreeRestaurant.state[typeIndex].length; table++) {
                if (custInRest > 0) {
                    logLik += Math.log(table) + logDisc - Math.log(custInRest);
                }
                logLik += Math.log(1.0 / alphabetSize);
                custInRest++;
                for (int cust = 1; cust < contextFreeRestaurant.state[typeIndex][table]; cust++) {
                    logLik += Math.log(cust - Math.exp(logDisc)) - Math.log(custInRest);
                    custInRest++;
                }
            }
        }

        int childDepth;
        for (Restaurant child : contextFreeRestaurant.values()) {
            childDepth = child.parentPath[1] - child.parentPath[0];
            logLik += this.getLogLik(child, childDepth);
        }

        return logLik;
    }

    private double getLogLik(Restaurant rest, int depth) {
        double logLik = 0.0;
        double logDisc = discounts.getLog(depth - (rest.parentPath[1] - rest.parentPath[0]), depth);

        int custInRest = 0;
        for (int typeIndex = 0; typeIndex < rest.state.length; typeIndex++) {
            for (int table = 1; table < rest.state[typeIndex].length; table++) {
                if (custInRest > 0) {
                    logLik += Math.log(table) + logDisc - Math.log(custInRest);
                }
                custInRest++;
                for (int cust = 1; cust < rest.state[typeIndex][table]; cust++) {
                    logLik += Math.log(cust - Math.exp(logDisc)) - Math.log(custInRest);
                    custInRest++;
                }
            }
        }

        int childDepth;
        for (Restaurant child : rest.values()) {
            childDepth = depth + child.parentPath[1] - child.parentPath[0];
            logLik += this.getLogLik(child, childDepth);
        }

        return logLik;
    }

    public int getNumberLeafNodes() {
        return this.getNumberLeafNodes(contextFreeRestaurant);
    }

    public int getNumberLeafNodes(Restaurant rest) {
        int leafNodes = 0;
        if (rest.size() == 0) {
            leafNodes++;
        } else {
            for (Restaurant child : rest.values()) {
                leafNodes += this.getNumberLeafNodes(child);
            }
        }
        return leafNodes;
    }
    public ArrayList<Restaurant> toDeleteList = null;

    public void deleteRandomLeafNodes(int numberToDelete) {
        toDeleteList = new ArrayList<Restaurant>(numberToDelete);
        int numberLeafNodes = this.getNumberLeafNodes();

        double initialRawRandomSample = SMTree.RNG.nextDouble() / numberToDelete;
        Stack<Double> rawRandomSample = new Stack<Double>();
        //initialize rawRandomSample stack
        for (int j = numberToDelete; j >= 0; j--) {
            rawRandomSample.add(new Double(initialRawRandomSample + j * (1.0 / numberToDelete)));
        }

        //start recursive search of leaf nodes at contextFreeRestaurant
        this.deleteRandomLeafNodes(contextFreeRestaurant, 0.0, rawRandomSample, numberLeafNodes);

        //delete rest to be deleted
        for (Restaurant rest : toDeleteList) {
            rest.delete(seq);
        }
        //clear deletion list
        toDeleteList = null;
    }

    public double deleteRandomLeafNodes(Restaurant rest, double cumSum, Stack<Double> rawRandomSample, int numberLeafNodes) {
        if (rest.size() == 0) {
            cumSum += 1.0 / numberLeafNodes;
            if (cumSum > rawRandomSample.peek().doubleValue()) {
                toDeleteList.add(rest);
                rawRandomSample.pop();
            }
        } else {
            for (Restaurant child : rest.values()) {
                cumSum = this.deleteRandomLeafNodes(child, cumSum, rawRandomSample, numberLeafNodes);
            }
        }
        return cumSum;
    }
    public ArrayList<Pair<Restaurant, Double>> logProbLeafNodeList = null;

    public void fillLogProbLeafNodeList(Restaurant rest, double[] logProbDistInParent, int depth) {
        //check if leaf node first
        if (rest.size() == 0) {
            Pair<Restaurant, Double> pairToAdd = new Pair(rest, new Double(rest.getLogProbDiffIfDelete(logProbDistInParent, depth, discounts)));
            logProbLeafNodeList.add(pairToAdd);
            return;
        }

        int tablesInRest = 0;
        int custInRest = 0;
        for (int typeIndex = 0; typeIndex < rest.state.length; typeIndex++) {
            tablesInRest += rest.state[typeIndex].length - 1;
            for (int table = 1; table < rest.state[typeIndex].length; table++) {
                custInRest += rest.state[typeIndex][table];
            }
        }

        double logDiscount;
        if (rest.parent != null) {
            logDiscount = discounts.getLog(depth - rest.parentPath[1] + rest.parentPath[0], depth);
        } else {
            logDiscount = discounts.getLog(0);
        }

        double[] logPredDist = new double[logProbDistInParent.length];
        for (int type = 0; type < logPredDist.length; type++) {
            logPredDist[type] = logProbDistInParent[type] + logDiscount + Math.log(tablesInRest) - Math.log(custInRest);
        }

        int custOfType;
        int tablesOfType;
        for (int typeIndex = 0; typeIndex < rest.state.length; typeIndex++) {
            custOfType = 0;
            tablesOfType = rest.state[typeIndex].length - 1;
            for (int table = 1; table < rest.state[typeIndex].length; table++) {
                custOfType += rest.state[typeIndex][table];
            }
            logPredDist[rest.state[typeIndex][0]] = Math.log((custOfType - Math.exp(Math.log(tablesOfType) + logDiscount)) / custInRest + Math.exp(Math.log(tablesInRest) + logDiscount + logProbDistInParent[rest.state[typeIndex][0]]) / custInRest);
        }

        int childDepth;
        for (Restaurant child : rest.values()) {
            childDepth = depth + child.parentPath[1] - child.parentPath[0];
            this.fillLogProbLeafNodeList(child, logPredDist, childDepth);
        }
    }

    public void fillLogProbLeafNodeList() {
        logProbLeafNodeList = new ArrayList<Pair<Restaurant, Double>>(100000);
        double[] logProbDistInParent = new double[alphabetSize];
        for (int type = 0; type < alphabetSize; type++) {
            logProbDistInParent[type] = Math.log(1.0 / alphabetSize);
        }
        this.fillLogProbLeafNodeList(contextFreeRestaurant, logProbDistInParent, 0);
    }

    public void deleteLeastUsefullRestaurantsForLogProbOfData(int numberToDelete) {
        this.fillLogProbLeafNodeList();

        Pair[] arrayLeafNodes = logProbLeafNodeList.toArray(new Pair[0]);
        Arrays.sort(arrayLeafNodes, new PairComparatorDouble());

        int index = 0;
        Restaurant restToDelete;
        while (index < numberToDelete) {
            restToDelete = (Restaurant) arrayLeafNodes[index++].first();
            restToDelete.deleteWithDeletionStateUpdate(seq);
        }

        logProbLeafNodeList = null;
    }

    /********************************SAMPLING**********************************/
    public double[] sampleDiscounts() {
        //sample each discount seperately
        double[] returnVal = {0.0, 0.0, 0.0};
        int numToSample = (maxDepth > -1) ? (maxDepth + 1) : discounts.discounts.length;

        double currentLogLik = this.getLogLik();
        double propLogLik;
        double acceptProb;
        double jump;
        double spanJumpDist = .05;
        for (int i = 0; i < numToSample; i++) {
            jump = 2 * spanJumpDist * Math.random() - spanJumpDist;
            discounts.set(discounts.get(i) + jump, i);
            propLogLik = this.getLogLik();
            acceptProb = Math.exp(propLogLik - currentLogLik);
            if (Math.random() < acceptProb) {
                returnVal[0]++;
                currentLogLik = propLogLik;
            } else {
                discounts.set(discounts.get(i) - jump, i);
            }
            returnVal[1]++;
        }
        returnVal[2] = currentLogLik;
        return returnVal;
    }

    //sampling will be depth first, only preserving the predictive distriubtion
    //in each node for those restaurants directly above it.  Sampling will
    //happen first at the bottom nodes.
    public void sampleSeating() {
        this.sampleSeating(contextFreeRestaurant, 0);
    }

    public void sampleSeating(Restaurant rest, int d) {
        //fill predictive counts on the way down
        double logDiscount;
        if (rest.parent == null) {
            logDiscount = discounts.getLog(0);
        } else {
            logDiscount = discounts.getLog(d - rest.parentPath[1] + rest.parentPath[0], d);
        }
        rest.fillPredictiveCounts(alphabetSize, Math.exp(logDiscount), 0);

        //sample bottom up, so recurse down first
        int childDepth;
        for (Restaurant child : rest.values()) {
            childDepth = d + child.parentPath[1] - child.parentPath[0];
            this.sampleSeating(child, childDepth);
        }

        //make a copy of the restaurant state
        int[][] startState = new int[rest.state.length][];
        System.arraycopy(rest.state, 0, startState, 0, rest.state.length);

        //go through each customer unseating them and then re-seating them
        int type;
        for (int typeIndex = 0; typeIndex < startState.length; typeIndex++) {
            type = startState[typeIndex][0];
            if (startState[typeIndex].length == 2 && startState[typeIndex][1] == 1) {
                continue;
            }
            for (int table = 1; table < startState[typeIndex].length; table++) {
                for (int cust = 0; cust < startState[typeIndex][table]; cust++) {
                    //unseat cust
                    Restaurant unseatInParent = (--rest.state[typeIndex][table] == 0) ? rest.parent : null;
                    rest.predictiveCounts.decrement(type, (unseatInParent != null));
                    this.unseatR(unseatInParent, type);

                    //sit cust
                    double predictiveProb = this.getPredictiveDist(rest, type);
                    double probUpper = predictiveProb - rest.predictiveCounts.typeNum[type] / (rest.predictiveCounts.cust + rest.predictiveCounts.concentration);
                    probUpper *= (rest.predictiveCounts.cust + rest.predictiveCounts.concentration) / (rest.predictiveCounts.discount * rest.predictiveCounts.tables + rest.predictiveCounts.concentration);

                    Restaurant sitInParent = rest.sitAtRestS(type, probUpper, logDiscount);
                    this.sitR(sitInParent, type, probUpper);
                }
            }
            //clean up if any zero rows
            int[] newRowLong = new int[rest.state[typeIndex].length];
            newRowLong[0] = type;
            int index = 1;
            for (int table = 1; table < rest.state[typeIndex].length; table++) {
                if (rest.state[typeIndex][table] != 0) {
                    newRowLong[index++] = rest.state[typeIndex][table];
                }
            }
            if (index == newRowLong.length) {
                rest.state[typeIndex] = newRowLong;
            } else {
                rest.state[typeIndex] = new int[index];
                System.arraycopy(newRowLong, 0, rest.state[typeIndex], 0, index);
            }
        }
        rest.predictiveCounts = null;
    }

    public void unseatR(Restaurant rest, int type) {
        if (rest == null) {
            return;
        }

        Restaurant unseatInParent = rest.unseat(type);
        rest.predictiveCounts.decrement(type, (unseatInParent != null));
        this.unseatR(unseatInParent, type);
    }

    public double getPredictiveDist(Restaurant rest, int type) {
        if (rest == null) {
            return 1.0 / alphabetSize;
        }

        double predictiveDistrib = rest.predictiveCounts.typeNum[type] / (rest.predictiveCounts.cust + rest.predictiveCounts.concentration);
        predictiveDistrib += this.getPredictiveDist(rest.parent, type) * (rest.predictiveCounts.discount * rest.predictiveCounts.tables + rest.predictiveCounts.concentration) / (rest.predictiveCounts.cust + rest.predictiveCounts.concentration);
        return predictiveDistrib;
    }

    public void sitR(Restaurant rest, int type, double predictiveProb) {
        if (rest == null) {
            return;
        }

        double probUpper = predictiveProb - rest.predictiveCounts.typeNum[type] / (rest.predictiveCounts.cust + rest.predictiveCounts.concentration);
        probUpper *= (rest.predictiveCounts.cust + rest.predictiveCounts.concentration) / (rest.predictiveCounts.discount * rest.predictiveCounts.tables + rest.predictiveCounts.concentration);

        Restaurant sitInParent = rest.sitAtRest(type, probUpper, rest.predictiveCounts.discount);
        rest.predictiveCounts.increment(type, sitInParent != null);

        this.sitR(sitInParent, type, probUpper);
    }

    /********************************PRINTING TREE*****************************/
    public void printRestAndChildren(Restaurant rest, int indexDownPath, int stringLengthOfWord) {
        int stringLength;

        int[] parentPath;
        if (rest.parentPath != null) {
            parentPath = new int[rest.parentPath[1] - rest.parentPath[0]];
            for (int i = 0; i < rest.parentPath[1] - rest.parentPath[0]; i++) {
                parentPath[i] = seq.get(rest.parentPath[2], rest.parentPath[0] + i);
            }

            //System.arraycopy(sequence, rest.parentPath[0], parentPath, 0, parentPath.length);

            if (indexDownPath == 0) {
                stringLength = new Integer(parentPath[parentPath.length - 1]).toString().length();
                for (int j = 0; j < stringLengthOfWord - stringLength; j++) {
                    System.out.print(" ");
                }
                System.out.print(parentPath[parentPath.length - 1]);
                indexDownPath++;

            } else {
                System.out.print(" - ");
                stringLength = new Integer(parentPath[parentPath.length - 1]).toString().length();
                for (int j = 0; j < stringLengthOfWord - stringLength; j++) {
                    System.out.print(" ");
                }
                System.out.print(parentPath[parentPath.length - 1]);
                indexDownPath++;
            }
            for (int i = 1; i < parentPath.length; i++) {
                System.out.print(" - ");
                stringLength = new Integer(parentPath[parentPath.length - 1 - i]).toString().length();
                for (int j = 0; j < stringLengthOfWord - stringLength; j++) {
                    System.out.print(" ");
                }
                System.out.print(parentPath[parentPath.length - 1 - i]);
                indexDownPath++;
            }
        }

        int index = 0;
        for (Restaurant child : rest.values()) {
            if (index++ == 0) {
                printRestAndChildren(child, indexDownPath, stringLengthOfWord);
            } else {
                System.out.println();
                //add in spaces for index down path
                for (int j = 0; j < indexDownPath; j++) {
                    for (int i = 0; i < stringLengthOfWord; i++) {
                        System.out.print(" ");
                    }
                }
                //add in spaces for missed ( - )
                for (int j = 0; j < indexDownPath - 1; j++) {
                    System.out.print("   ");
                }
                printRestAndChildren(child, indexDownPath, stringLengthOfWord);
            }

        }
        if (rest == this.contextFreeRestaurant) {
            System.out.println();
        }
    }

    public void printTree() {
        printRestAndChildren(this.contextFreeRestaurant, 0, new Integer(this.alphabetSize).toString().length());
    }
}
