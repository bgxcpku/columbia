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

    public int alphabetSize ;
    public Restaurant contextFreeRestaurant;
    public Discounts discounts =  null;
    public int[] sequence;
    public double obsLogProb = 0.0;
    public double logLoss = 0.0;
    public int maxDepth = -1;

    public StochasticMemoizer(int alphabetSize, Integer maxDepth) {
        this.alphabetSize = alphabetSize;
        contextFreeRestaurant = new Restaurant(null, null, 0);

        if(maxDepth != null){
            this.maxDepth = maxDepth.intValue();
        }

        Restaurant.numberRest = 0;

        double[] discount = {0.5, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95};
        this.discounts = new Discounts(discount);
    }

    public void seatSequnce(int[] seq) {
        if (sequence != null) {
            throw new RuntimeException("sequence must be null");
        }
        sequence = seq;
        int counter = 0;
        int index = 0;
        for (int j = 0; j < sequence.length; j++) {
            if (counter++ >= 100000) {
                System.out.println(index);
                counter = 1;
            }
            index++;
            seatObs(contextFreeRestaurant, j, j - 1, sequence, 1.0 / alphabetSize);
            logLoss -= obsLogProb / Math.log(2);
            discounts.stepGradient(0.0001, Math.exp(obsLogProb));
            //discounts.clearGradient();
        }
    }

    public void seatSequnceWithRandomDeletionOfRestaurants(int[] seq, int maxNumberRest) {
        if (sequence != null) {
            throw new RuntimeException("sequence must be null");
        }
        sequence = seq;
        int counter = 0;
        int index = 0;
        for (int j = 0; j < sequence.length; j++) {
            if (counter++ >= 100000) {
                System.out.println("index = " + index);
                counter = 1;
            }

            if (Restaurant.numberRest > maxNumberRest + 100) {
                this.deleteRandomLeafNodes(Restaurant.numberRest - maxNumberRest + 100);
            }

            index++;
            seatObs(contextFreeRestaurant, j, j - 1, sequence, 1.0 / alphabetSize);
            logLoss -= obsLogProb / Math.log(2);
            discounts.stepGradient(0.0001, Math.exp(obsLogProb));
        }
    }

    public void seatSequnceWithRandomEntireDeletionOfRestaurants(int[] seq, int maxNumberRest) {
        if (sequence != null) {
            throw new RuntimeException("sequence must be null");
        }
        sequence = seq;
        int counter = 0;
        int index = 0;
        for (int j = 0; j < sequence.length; j++) {
            if (counter++ >= 100000) {
                System.out.println("index = " + index);
                counter = 1;
            }

            if (Restaurant.numberRest > maxNumberRest + 100) {
                this.deleteRandomLeafNodesEntirely(Restaurant.numberRest - maxNumberRest + 100);
            }

            index++;
            seatObs(contextFreeRestaurant, j, j - 1, sequence, 1.0 / alphabetSize);
            logLoss -= obsLogProb / Math.log(2);
            discounts.stepGradient(0.0001, Math.exp(obsLogProb));
        }
    }

    public void seatSequenceWithDeletionOfUnusedRestaurants(int[] seq, int maxNumberRest) {
        if (sequence != null) {
            throw new RuntimeException("sequence must be null");
        }
        sequence = seq;
        int counter = 0;
        int index = 0;
        for (int j = 0; j < sequence.length; j++) {
            if (counter++ >= 100000) {
                System.out.println("index = " + index);
                counter = 1;
            }

            if (Restaurant.numberRest > maxNumberRest + 100) {
                this.fillLeastUsedLeafNodeList(maxNumberRest);
                this.deleteLeastUsedLeafRestaurants(Restaurant.numberRest - maxNumberRest + 100);
                leastUsedLeafNodeList.clear();
            }

            index++;
            seatObs(contextFreeRestaurant, j, j - 1, sequence, 1.0 / alphabetSize);
            logLoss -= obsLogProb / Math.log(2);
            discounts.stepGradient(0.0001, Math.exp(obsLogProb));
        }
    }

    public void seatSequenceWithDeletionOfUnhelpfulRestaurants(int[] seq, int maxNumberRest) {
        if (sequence != null) {
            throw new RuntimeException("sequence must be null");
        }
        sequence = seq;
        int counter = 0;
        int index = 0;
        for (int j = 0; j < sequence.length; j++) {
            if (counter++ >= 10000) {
                System.out.println("index = " + index);
                counter = 1;
            }

            if (Restaurant.numberRest > maxNumberRest + 100) {
                this.deleteLeastUsefullRestaurantsForLogProbOfData(Restaurant.numberRest - maxNumberRest + 100);
            }

            index++;
            seatObs(contextFreeRestaurant, j, j - 1, sequence, 1.0 / alphabetSize);
            logLoss -= obsLogProb / Math.log(2);
            discounts.stepGradient(0.0001, Math.exp(obsLogProb));
        }
    }

    private boolean seatObs(Restaurant rest, int obsIndex, int contextIndex, int[] seq, double upperRestProbOfObs) {
        //initiate some variables to be used later for ease and readability
        int depth = obsIndex - contextIndex - 1;
        boolean leafNode = (contextIndex == -1);
        if(maxDepth > -1){
            leafNode = (leafNode || (depth == maxDepth));
        }

        //get the logDiscount for the restaurant rest
        double logDiscount;
        if(rest.parent != null){
            logDiscount = discounts.getLog(depth - (rest.parentPath[1] - rest.parentPath[0]),depth);
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
        if(restCounts[3] >0){
            if(rest.parent == null){
                discounts.addToGradient(restCounts[1], restCounts[3], restCounts[2], alphabetSize);
            } else {
                discounts.addToDiscountGradient(depth - rest.parentPath[1] + rest.parentPath[0] + 1, depth, restCounts[1], restCounts[3], restCounts[2], upperRestProbOfObs);
            }
        }

        //handle leaf nodes first
        if (leafNode) {
            //update recently used to reflect when the restaurant was created
            rest.updateMostRecentTimeUsed(obsIndex);

            int[] rootRestCounts = contextFreeRestaurant.getRestCounts(seq[obsIndex]);
            double probRoot;
            if (rootRestCounts[3] > 0) {
                probRoot = 1.0 * (rootRestCounts[0] - Math.exp(Math.log(rootRestCounts[1]) + discounts.getLog(0))) / rootRestCounts[2] + Math.exp(Math.log(rootRestCounts[3]) + discounts.getLog(0) + Math.log(1.0 / alphabetSize) - Math.log(rootRestCounts[2]));
            } else {
                probRoot = 1.0 / alphabetSize;
            }

            obsLogProb = Math.log((99.0 * prob + probRoot) / 100);
            return rest.sitAtRest(seq[obsIndex], upperRestProbOfObs, depth, discounts);
        }

        Restaurant childRest = rest.get(new Integer(seq[contextIndex]));

        while (true) {
            //CASE 1 : there are no children in the direction of this observation
            if (childRest == null) {
                int[] childParentPath = new int[2];
                childParentPath[1] = contextIndex + 1;
                if(maxDepth > -1 && (contextIndex + 1 - (maxDepth - depth)) > 0){
                    childParentPath[0] = contextIndex + 1 - (maxDepth - depth);
                } else {
                    childParentPath[0] = 0;
                }

                childRest = new Restaurant(rest, childParentPath, obsIndex);
                rest.putChild(childRest, seq);

                rest.updateMostRecentTimeUsed(obsIndex);
                contextIndex = -1;
                break;
            }

            int diffIndex = this.compareContexts(childRest.parentPath, contextIndex, seq);

            //CASE 2 : there is a child exactly in the direction of this observation
            if (diffIndex == (childRest.parentPath[1] - childRest.parentPath[0])) {
                contextIndex -= diffIndex;
                rest.updateMostRecentTimeUsed(obsIndex);
                break;
            }

            //CASE 3 : there is a child in a direction which shares some context
            //with this obs
            int[] rest2ParentPath = {0, contextIndex + 1};
            contextIndex -= diffIndex;

            rest2ParentPath[0] = contextIndex + 1;

            rest.updateMostRecentTimeUsed(obsIndex);
            childRest.updateMostRecentTimeUsed(obsIndex);

            childRest = childRest.reconfigureRestaurantReturnIntermediateRestaurant(rest2ParentPath, depth + rest2ParentPath[1] - rest2ParentPath[0], seq, discounts);
            break;
        }

        if (this.seatObs(childRest, obsIndex, contextIndex, seq, prob)) {
            return rest.sitAtRest(seq[obsIndex], upperRestProbOfObs, depth,discounts);
        } else {
            return false;
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

    /*
    public void sampleRestAndChildren(Restaurant rest) {
        if (rest.keySet().size() == 0) {
            rest.reSeat(alphabetSize);
        } else {
            for (Restaurant child : rest.values()) {
                this.sampleRestAndChildren(child);
            }
            rest.reSeat(alphabetSize);
        }
    }

    public void sampleTree() {
        this.sampleRestAndChildren(contextFreeRestaurant);
    }*/

    
/*
    public double getLogLikelihoodRestandChildren(Restaurant rest, double[] logParentDistrib, Double logDiscount, int depth) {
        double logLikelihood = 0.0;
        if (logDiscount == null) {
            logDiscount = new Double(rest.Restaurant.this.logDiscount);
        }

        logLikelihood += rest.getLogLikelihood(logDiscount, logParentDistrib);

        int tablesInRest = 0;
        int custInRest = 0;
        for (int typeIndex = 0; typeIndex < rest.state.length; typeIndex++) {
            tablesInRest += rest.state[typeIndex].length - 1;
            for (int table = 1; table < rest.state[typeIndex].length; table++) {
                custInRest += rest.state[typeIndex][table];
            }
        }

        //update the logParentDistribution for children nodes first by multiplying
        //by the discount parameter
        for (int i = 0; i < logParentDistrib.length; i++) {
            logParentDistrib[i] += logDiscount.doubleValue() + Math.log(tablesInRest) - Math.log(custInRest);
        }

        //now update by going through the state and adding the counts
        int custOfType;
        int tablesOfType;
        for (int typeIndex = 0; typeIndex < rest.state.length; typeIndex++) {
            custOfType = 0;
            tablesOfType = rest.state[typeIndex].length - 1;
            for (int table = 1; table < rest.state[typeIndex].length; table++) {
                custOfType += rest.state[typeIndex][table];
            }
            logParentDistrib[rest.state[typeIndex][0]] -= logDiscount.doubleValue() + Math.log(tablesInRest) - Math.log(custInRest);
            logParentDistrib[rest.state[typeIndex][0]] = Math.log((custOfType - Math.exp(Math.log(tablesOfType) + logDiscount.doubleValue())) / custInRest + Math.exp(Math.log(tablesInRest) + logDiscount + logParentDistrib[rest.state[typeIndex][0]]) / custInRest);
        }

        //stupid check
        double prob = 0.0;
        for(int type = 0; type<logParentDistrib.length; type++){
            prob += Math.exp(logParentDistrib[type]);
        }
        if(prob != 1.0){
            System.out.println("sum of prob = " + prob);
        }

        int distanceDown;
        int childDepth;
        for (Restaurant child : rest.values()) {
            distanceDown = child.parentPath[1] - child.parentPath[0];
            childDepth = depth;
            double childLogDiscount = 0.0;
            while (childDepth < 10 && distanceDown > 0) {
                childDepth++;
                distanceDown--;
                childLogDiscount += Math.log(discount[childDepth]);
            }
            if (distanceDown > 0) {
                childLogDiscount += distanceDown * Math.log(discountInfty);
                childDepth += distanceDown;
            }
            logLikelihood += this.getLogLikelihoodRestandChildren(child, logParentDistrib, childLogDiscount, childDepth);
        }

        return logLikelihood;
    }

    public double getLogLikelihoodTree() {
        double[] logParentDistrib = new double[alphabetSize];
        for (int i = 0; i < alphabetSize; i++) {
            logParentDistrib[i] = Math.log(1.0 / alphabetSize);
        }
        return this.getLogLikelihoodRestandChildren(contextFreeRestaurant, logParentDistrib, Math.log(discount[0]), 0);
    } */

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

    public int getNumberLeafNodes() {
        return this.getNumberLeafNodes(contextFreeRestaurant);
    }

    public ArrayList<Pair<Restaurant, Integer>> leastUsedLeafNodeList = null;
    public void fillLeastUsedLeafNodeList(Restaurant rest) {
        if (rest.size() == 0) {
            leastUsedLeafNodeList.add(new Pair(rest, new Integer(rest.mostRecentTimeUsed)));
        } else {
            for (Restaurant child : rest.values()) {
                this.fillLeastUsedLeafNodeList(child);
            }
        }
    }

    public void fillLeastUsedLeafNodeList(int initialCapacity) {
        leastUsedLeafNodeList = new ArrayList<Pair<Restaurant, Integer>>(initialCapacity);
        this.fillLeastUsedLeafNodeList(contextFreeRestaurant);
    }

    public void deleteLeastUsedLeafRestaurants(int numberToDelete) {
        //this method assumes the leafNodeList is filled
        if (leastUsedLeafNodeList.size() == 0) {
            throw new RuntimeException("this method assumes the leafNodeList has" +
                    " already been filled");
        }

        Pair[] arrayLeafNodes = leastUsedLeafNodeList.toArray(new Pair[0]);
        Arrays.sort(arrayLeafNodes, new PairComparator());

        int index = 0;
        Restaurant restToDelete;
        while (index < numberToDelete) {
            restToDelete = (Restaurant) arrayLeafNodes[index++].first();
            restToDelete.delete(sequence);
        }
    }

    public ArrayList<Restaurant> toDeleteList = null;
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

    public void deleteRandomLeafNodes(int numberToDelete) {
        toDeleteList = new ArrayList<Restaurant>(numberToDelete);
        int numberLeafNodes = this.getNumberLeafNodes();

        double initialRawRandomSample = Math.random() / numberToDelete;
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

    public void deleteRandomLeafNodesEntirely(int numberToDelete) {
        toDeleteList = new ArrayList<Restaurant>(numberToDelete);
        int numberLeafNodes = this.getNumberLeafNodes();

        double initialRawRandomSample = Math.random() / numberToDelete;
        Stack<Double> rawRandomSample = new Stack<Double>();
        //initialize rawRandomSample stack
        for (int j = numberToDelete; j >= 0; j--) {
            rawRandomSample.add(new Double(initialRawRandomSample + j * (1.0 / numberToDelete)));
        }

        //start recursive search of leaf nodes at contextFreeRestaurant
        this.deleteRandomLeafNodes(contextFreeRestaurant, 0.0, rawRandomSample, numberLeafNodes);

        //delete rest to be deleted
        for (Restaurant rest : toDeleteList) {
            rest.deleteEntirely(sequence);
        }
        //clear deletion list
        toDeleteList = null;
    }

    public ArrayList<Pair<Restaurant,Double>> logProbLeafNodeList = null;
    public void fillLogProbLeafNodeList(Restaurant rest, double[] logProbDistInParent, int depth){
        //check if leaf node first
        if(rest.size() == 0){
            Pair<Restaurant, Double> pairToAdd = new Pair(rest, new Double(rest.getLogProbDiffIfDelete(logProbDistInParent,depth,discounts)));
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
        if(rest.parent != null){
            logDiscount = discounts.getLog(depth - rest.parentPath[1] + rest.parentPath[0],depth);
        } else {
            logDiscount = discounts.getLog(0);
        }

        double[] logPredDist = new double[logProbDistInParent.length];
        for(int type = 0; type < logPredDist.length; type++){
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
        for(Restaurant child : rest.values()){
            childDepth = depth + child.parentPath[1] - child.parentPath[0];
            this.fillLogProbLeafNodeList(child, logPredDist,childDepth);
        }
    }
    
    public void fillLogProbLeafNodeList(){
        logProbLeafNodeList = new ArrayList<Pair<Restaurant,Double>>(100000);
        double[] logProbDistInParent = new double[alphabetSize];
        for(int type = 0; type<alphabetSize; type++){
            logProbDistInParent[type] = Math.log(1.0/alphabetSize);
        }
        this.fillLogProbLeafNodeList(contextFreeRestaurant, logProbDistInParent,0);
    }

    public void deleteLeastUsefullRestaurantsForLogProbOfData(int numberToDelete){
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
