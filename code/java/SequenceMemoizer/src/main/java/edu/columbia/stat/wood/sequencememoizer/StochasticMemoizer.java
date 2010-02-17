/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 *
 * @author nicholasbartlett
 */
public class StochasticMemoizer {

    public int alphabetSize;
    public Restaurant contextFreeRestaurant;
    public Discounts discounts = null;
    public int[] sequence;
    public double obsLogProb = 0.0;
    public double logLoss = 0.0;
    public int maxDepth = -1;
    public int maxCacheSize = 0;

    public StochasticMemoizer(int alphabetSize, Integer maxDepth) {
        this.alphabetSize = alphabetSize;
        contextFreeRestaurant = new Restaurant(null, null, 0);

        if (maxDepth != null) {
            this.maxDepth = maxDepth.intValue();
        }

        Restaurant.numberRest = 0;

        double[] discount = {0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95};
        this.discounts = new Discounts(discount);
    }

    public Restaurant seatObs(Restaurant rest, int obsIndex, int contextIndex, int[] seq, double upperRestProbOfObs) {
        //initiate some variables to be used later for ease and readability
        int depth = obsIndex - contextIndex - 1;
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
        int[] restCounts = rest.getRestCounts(seq[obsIndex]);
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
            int[] rootRestCounts = contextFreeRestaurant.getRestCounts(seq[obsIndex]);
            double probRoot;
            if (rootRestCounts[3] > 0) {
                probRoot = 1.0 * (rootRestCounts[0] - Math.exp(Math.log(rootRestCounts[1]) + discounts.getLog(0))) / rootRestCounts[2] + Math.exp(Math.log(rootRestCounts[3]) + discounts.getLog(0) + Math.log(1.0 / alphabetSize) - Math.log(rootRestCounts[2]));
            } else {
                probRoot = 1.0 / alphabetSize;
            }

            obsLogProb = Math.log((99.0 * prob + probRoot) / 100);
            int parentDepth;
            if (rest.parent != null) {
                parentDepth = depth - rest.parentPath[1] + rest.parentPath[0];
            } else {
                parentDepth = 0;
            }
            return rest.sitAtRest(seq[obsIndex], upperRestProbOfObs, discounts.getLog(parentDepth, depth));
        }

        Restaurant childRest = rest.get(new Integer(seq[contextIndex]));

        while (true) {
            //CASE 1 : there are no children in the direction of this observation
            if (childRest == null) {
                int[] childParentPath = new int[2];
                childParentPath[1] = contextIndex + 1;
                if (maxDepth > -1 && (contextIndex + 1 - (maxDepth - depth)) > 0) {
                    childParentPath[0] = contextIndex + 1 - (maxDepth - depth);
                } else {
                    childParentPath[0] = 0;
                }

                childRest = new Restaurant(rest, childParentPath, obsIndex);
                rest.putChild(childRest, seq);

                contextIndex = -1;
                break;
            }

            int diffIndex = this.compareContexts(childRest.parentPath, contextIndex, seq);
            maxCacheSize = (diffIndex > maxCacheSize) ? diffIndex : maxCacheSize;

            //CASE 2 : there is a child exactly in the direction of this observation
            if (diffIndex == (childRest.parentPath[1] - childRest.parentPath[0])) {
                contextIndex -= diffIndex;
                break;
            }

            //CASE 3 : there is a child in a direction which shares some context
            //with this obs
            int[] rest2ParentPath = {0, contextIndex + 1};
            contextIndex -= diffIndex;

            rest2ParentPath[0] = contextIndex + 1;

            childRest = childRest.reconfigureRestaurantReturnIntermediateRestaurant(rest2ParentPath, depth + rest2ParentPath[1] - rest2ParentPath[0], seq, discounts);
            break;
        }

        int parentDepth;
        if (rest.parent != null) {
            parentDepth = depth - rest.parentPath[1] + rest.parentPath[0];
        } else {
            parentDepth = 0;
        }

        if (this.seatObs(childRest, obsIndex, contextIndex, seq, prob) != null) {
            return rest.sitAtRest(seq[obsIndex], upperRestProbOfObs, discounts.getLog(parentDepth, depth));
        } else {
            return null;
        }
    }

    private int compareContexts(int[] parentPath, int l, int[] seq) {
        //walk through the parent path backwards, comparing it to the sequence
        //from l, going backwards.
        for (int j = 0; j < (parentPath[1] - parentPath[0]); j++) {
            if (seq[l - j] != seq[parentPath[1] - j - 1]) {
                return j;
            }
        }
        return (parentPath[1] - parentPath[0]);
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

    public double getLogLik(Restaurant rest, int depth) {
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

        double initialRawRandomSample = ByteSeater.utils.RNG.nextDouble() / numberToDelete;
        Stack<Double> rawRandomSample = new Stack<Double>();
        //initialize rawRandomSample stack
        for (int j = numberToDelete; j >= 0; j--) {
            rawRandomSample.add(new Double(initialRawRandomSample + j * (1.0 / numberToDelete)));
        }

        //start recursive search of leaf nodes at contextFreeRestaurant
        this.deleteRandomLeafNodes(contextFreeRestaurant, 0.0, rawRandomSample, numberLeafNodes);

        //delete rest to be deleted
        for (Restaurant rest : toDeleteList) {
            rest.delete(sequence);
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
            restToDelete.deleteWithDeletionStateUpdate(sequence);
        }

        logProbLeafNodeList = null;
    }

    /********************************SAMPLING**********************************/
    public double[] sampleDiscounts() {
        //sample each discount seperately
        double[] returnVal = {0, 0, 0};
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
        rest.fillPredictiveCounts(alphabetSize, discounts.get(d), 0);

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
                    this.sitR(rest, type, this.getPredictiveDist(rest, type));
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
        probUpper = probUpper * (rest.predictiveCounts.cust + rest.predictiveCounts.concentration) / (rest.predictiveCounts.discount * rest.predictiveCounts.tables + rest.predictiveCounts.concentration);

        Restaurant sitInParent = rest.sitAtRest(type, probUpper, rest.predictiveCounts.discount);
        this.sitR(sitInParent, type, probUpper);
    }

    /********************************PRINTING TREE*****************************/
    public void printRestAndChildren(Restaurant rest, int indexDownPath, int stringLengthOfWord) {
        int stringLength;

        int[] parentPath;
        if (rest.parentPath != null) {
            parentPath = new int[rest.parentPath[1] - rest.parentPath[0]];
            System.arraycopy(sequence, rest.parentPath[0], parentPath, 0, parentPath.length);

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
