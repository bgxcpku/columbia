/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.finitedepthhpyp;

import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author nicholasbartlett
 */
public class HPYTree {

    private int alphabetSize;
    private Restaurant contextFreeRestaurant;
    private Discounts discounts;
    private Concentrations concentrations;
    private int[] context = new int[0];
    private double logLoss = 0.0;
    private int depth;
    public static Random RNG;

    public HPYTree(int alphabetSize, int depth, int seed) {
        this.alphabetSize = alphabetSize;
        this.depth = depth;
        Restaurant.numberRest = 0;
        discounts = new Discounts(new double[]{0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95});
        concentrations = new Concentrations();
        contextFreeRestaurant = new Restaurant(null);
        RNG = new Random(seed);
    }

    public double continueSequence(int observation){
        //seat token first given the current context
        this.seatToken(contextFreeRestaurant, observation, context.length-1, 1.0/alphabetSize, 0);

        //update context
        int[] newContext;
        if(context.length < depth){
            newContext = new int[context.length + 1];
            System.arraycopy(context, 0, newContext, 0,context.length);
            newContext[context.length] = observation;
            context = newContext;
        } else {
            for(int i = 0; i<(context.length-1); i++){
                context[i] = context[i + 1];
            }
            context[context.length-1] = observation;
        }

        //step gradient
        discounts.stepGradient(0.0001, Math.exp(logLoss));

        //return the logLoss for the seated observation;
        return logLoss;
    }

    private Restaurant seatToken(Restaurant rest, int token, int contextIndex, double probUpper, int depth) {
        //get predictive counts in restaurant as well as restaurant discount and concentration
        int[] restCounts = rest.getRestCounts(token);
        double restDiscount = discounts.get(depth);
        double restConcentration = concentrations.get(depth);

        //get probability of observing the token in this restaurant
        double prob;
        double totalWeight = restCounts[2] + restConcentration;
        if (restCounts[3] > 0) {
            prob = 1.0 * (restCounts[0] - restCounts[1] * restDiscount) / totalWeight +
                    (restDiscount * restCounts[3] + restConcentration) * probUpper / totalWeight;
        } else {
            prob = probUpper;
        }

        //update discount gradient
        if (restCounts[3] > 0) {
            if (rest.parent == null) {
                discounts.addToGradient(restCounts[1], restCounts[3], restCounts[2], alphabetSize);
            } else {
                discounts.addToDiscountGradient(depth, depth, restCounts[1], restCounts[3], restCounts[2], probUpper);
            }
        }

        //if leaf node then calculated log loss and seat token in the restaurant
        if (contextIndex == -1) {
            //TODO FIX
            logLoss = Math.log(prob*99.0/100 + (1.0/100)*(1.0/alphabetSize));
            //logLoss = Math.log(prob) ;
            return rest.sitAtRest(token, probUpper, restDiscount, restConcentration);
        }

        //if not leaf node then find childRest
        Integer childKey = new Integer(context[contextIndex]);
        Restaurant childRest = rest.get(childKey);

        //if no children in the direction of this obs will need to create one
        if (childRest == null) {
            childRest = new Restaurant(rest);
            rest.put(childKey, childRest);
        }

        if (this.seatToken(childRest, token, --contextIndex, prob, ++depth) != null) {
            return rest.sitAtRest(token, probUpper, restDiscount, restConcentration);
        } else {
            return null;
        }
    }

    public int[] predDist(double lowPointOnCDF, double highPointOnCDF){
        int lowSample = 0;
        int highSample = alphabetSize-1;
        double[] predDist = this.getPredDist();

        double cumSum = 0.0;
        for(int i = 0; i<predDist.length; i++){
            cumSum += predDist[i];
            if(cumSum>lowPointOnCDF){
                lowSample = i;
                break;
            }
        }

        cumSum = 0.0;
        for(int i = 0; i<predDist.length; i++){
            cumSum += predDist[i];
            if(cumSum>highPointOnCDF){
                highSample = i;
                break;
            }
        }

        return new int[]{lowSample, highSample};
    }

    public double[] cumulativeDistributionInterval(int token){
        double[] predDist = this.getPredDist();
        double low = 0.0;
        for(int i = 0; i<token; i++){
            low += predDist[i];
        }
        double high = low + predDist[token];
        return new double[]{low, high};
    }

    public double[] getPredDist(){
        double[] predDist = new double[alphabetSize];
        Arrays.fill(predDist, 1.0/alphabetSize);

        Restaurant currentRest = contextFreeRestaurant;
        Restaurant childRest = null;
        int d = 0;
        for(int i = 0; i<context.length; i++){
            currentRest.fillPredictiveCounts(alphabetSize, discounts.get(d), concentrations.get(d));

            for(int j = 0; j<alphabetSize; j++){
                predDist[j] *= (currentRest.predictiveCounts.tables*currentRest.predictiveCounts.discount + currentRest.predictiveCounts.concentration)/(currentRest.predictiveCounts.concentration + currentRest.predictiveCounts.cust);
                predDist[j] += currentRest.predictiveCounts.typeNum[j]/(currentRest.predictiveCounts.concentration + currentRest.predictiveCounts.cust);
            }

            currentRest.predictiveCounts = null;

            childRest = currentRest.get(new Integer(context[context.length-1-i]));
            if(childRest == null){
                break;
            } else {
                currentRest = childRest;
                childRest = null;
            }
        }


        //TODO fix this
        for(int i = 0; i<alphabetSize;i++){
            predDist[i] *= 99.0/100;
            predDist[i] += (1.0/100)*(1.0/alphabetSize);
        }

        return predDist;
    }

/*
    public double getLogLik() {
        double logLik = 0.0;
        double restDiscount = discounts.get(0);
        double restConcentration = concentrations.get(0);
        int custInRest = 0;

        
        if (!discounts.isDiscountInRange() || !concentrations.isConcentrationInRange()){
            return Double.NEGATIVE_INFINITY;
        }
        

        for (int i = 0; i < concentrations.concentrations.length; i++) {
            if (concentrations.concentrations[i] < 0.0) {
                return Double.NEGATIVE_INFINITY;
            }
        }

        for (int typeIndex = 0; typeIndex < contextFreeRestaurant.state.length; typeIndex++) {
            for (int table = 1; table < contextFreeRestaurant.state[typeIndex].length; table++) {
                if (custInRest > 0) {
                    logLik += Math.log(restDiscount * table + restConcentration) - Math.log(custInRest + restConcentration);
                }
                logLik += Math.log(1.0 / alphabetSize);
                custInRest++;
                for (int cust = 1; cust < contextFreeRestaurant.state[typeIndex][table]; cust++) {
                    logLik += Math.log(cust - restDiscount) - Math.log(custInRest + restConcentration);
                    custInRest++;
                }
            }
        }

        //go through children to find their contribution
        for (Restaurant child : contextFreeRestaurant.values()) {
            logLik += this.getLogLik(child, 1);
        }

        return logLik;
    }

    public double getLogLik(Restaurant rest, int depth) {
        double logLik = 0.0;
        double restDiscount = discounts.get(depth);
        double restConcentration = concentrations.get(depth);
        int custInRest = 0;

        //calc log likelihood contribution of this restaurant
        for (int typeIndex = 0; typeIndex < rest.state.length; typeIndex++) {
            for (int table = 1; table < rest.state[typeIndex].length; table++) {
                if (custInRest > 0) {
                    logLik += Math.log(restDiscount * table + restConcentration) - Math.log(custInRest + restConcentration);
                }
                custInRest++;
                for (int cust = 1; cust < rest.state[typeIndex][table]; cust++) {
                    logLik += Math.log(cust - restDiscount) - Math.log(custInRest + restConcentration);
                    custInRest++;
                }
            }
        }

        //go through children to find their contribution
        for (Restaurant child : rest.values()) {
            logLik += this.getLogLik(child, depth + 1);
        }

        return logLik;
    }

    public double[] sampleDiscounts() {
        //sample each discount seperately
        double[] returnVal = {0, 0,0};
        int numToSample = (depth>(discounts.discounts.length -1))?discounts.discounts.length:(depth+1);

        double currentLogLik = this.getLogLik();
        double propLogLik;
        double acceptProb;
        double jump;
        double spanJumpDist = .1;
        for (int i = 0; i < numToSample; i++) {
            jump = 2 * spanJumpDist * RNG.nextDouble() - spanJumpDist;
            discounts.set(discounts.get(i) + jump, i);
            propLogLik = this.getLogLik();
            acceptProb = Math.exp(propLogLik - currentLogLik);
            if (RNG.nextDouble() < acceptProb) {
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

    public double[] sampleConcentrations() {
        double[] returnVal = {0, 0,0};
        int numToSample = (depth>(discounts.discounts.length -1))?discounts.discounts.length:(depth+1);

        //sample each concentration seperately
        double currentLogLik = this.getLogLik();
        double propLogLik;
        double acceptProb;
        double jump;
        double spanJumpDist = 1;
        for (int i = 0; i < numToSample; i++) {
            jump = jump = 2 * spanJumpDist * RNG.nextDouble() - spanJumpDist;
            concentrations.set(concentrations.get(i) + jump, i);
            propLogLik = this.getLogLik();
            acceptProb = Math.exp(propLogLik - currentLogLik +
                    this.getLogPriorConcentration(concentrations.get(i)) -
                    this.getLogPriorConcentration(concentrations.get(i) - jump));
            if (RNG.nextDouble() < acceptProb) {
                returnVal[0]++;
                currentLogLik = propLogLik;
            } else {
                concentrations.set(concentrations.get(i) - jump, i);
            }
            returnVal[1]++;
        }
        returnVal[2] = currentLogLik;
        return returnVal;
    }

    private double getLogPriorConcentration(double c) {
        double k = 1;
        double theta = 5;
        if (c < 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return (k - 1) * Math.log(c) - (c / theta) - k * Math.log(theta);
    }
*/
    //sampling will be depth first, only preserving the predictive distriubtion
    //in each node for those restaurants directly above it.  Sampling will
    //happen first at the bottom nodes.

    /*
    public void sampleSeating(){
        this.sampleSeating(contextFreeRestaurant,0);
    }


    public void sampleSeating(Restaurant rest, int d){
        //fill predictive counts on the way down
        rest.fillPredictiveCounts(alphabetSize, discounts.get(d), concentrations.get(d));

        //sample bottom up, so recurse down first
        for(Restaurant child:rest.values()){
            this.sampleSeating(child, d+1);
        }

        //make a copy of the restaurant state
        int[][] startState = new int[rest.state.length][];
        System.arraycopy(rest.state, 0, startState, 0, rest.state.length);

        //go through each customer unseating them and then re-seating them
        int type;
        for(int typeIndex = 0; typeIndex<startState.length; typeIndex++){
            type = startState[typeIndex][0];
            if(startState[typeIndex].length == 2 && startState[typeIndex][1] == 1){
                continue;
            }
            for(int table = 1; table < startState[typeIndex].length; table++){
                for(int cust = 0; cust < startState[typeIndex][table]; cust++){
                    //unseat cust
                    Restaurant unseatInParent = (--rest.state[typeIndex][table] == 0)?rest.parent:null;
                    rest.predictiveCounts.decrement(type, (unseatInParent!=null));
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

    public void unseatR(Restaurant rest, int type){
        if(rest == null){
            return;
        }

        Restaurant unseatInParent = rest.unseat(type);
        rest.predictiveCounts.decrement(type, (unseatInParent!=null));
        this.unseatR(unseatInParent, type);
    }

    public double getPredictiveDist(Restaurant rest, int type){
        if (rest == null){
            return 1.0/alphabetSize;
        }

        double predictiveDistrib  = rest.predictiveCounts.typeNum[type]/(rest.predictiveCounts.cust + rest.predictiveCounts.concentration);
        predictiveDistrib += this.getPredictiveDist(rest.parent, type)*(rest.predictiveCounts.discount*rest.predictiveCounts.tables + rest.predictiveCounts.concentration)/(rest.predictiveCounts.cust + rest.predictiveCounts.concentration);
        return predictiveDistrib;
    }

    public void sitR(Restaurant rest, int type, double predictiveProb){
        if(rest == null){
            return;
        }

        double probUpper = predictiveProb - rest.predictiveCounts.typeNum[type]/(rest.predictiveCounts.cust + rest.predictiveCounts.concentration);
        probUpper = probUpper*(rest.predictiveCounts.cust + rest.predictiveCounts.concentration)/(rest.predictiveCounts.discount*rest.predictiveCounts.tables + rest.predictiveCounts.concentration)   ;

        Restaurant sitInParent = rest.sitAtRest(type, probUpper, rest.predictiveCounts.discount, rest.predictiveCounts.concentration);
        this.sitR(sitInParent, type, probUpper);
    }
     */
}

