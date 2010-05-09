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
        contextFreeRestaurant = new SamplingRestaurant(null);
        RNG = new Random(seed);
    }

    public int getDepth(){
        return depth;
    }

    public double continueSequence(int observation) {
        this.seatToken(contextFreeRestaurant, observation, context, context.length - 1, 1.0 / alphabetSize, 0);

        this.updateContext(observation);

        discounts.stepGradient(0.0001, Math.exp(logLoss));
        //discounts.clearGradient();

        return logLoss;
    }

    public void addToken(int observation, int[] cntxt){
        this.seatToken(contextFreeRestaurant, observation, cntxt, cntxt.length-1, 1.0 / alphabetSize, 0);
    }

    private int[] restCounts = new int[4];
    private Restaurant seatToken(Restaurant rest, int token, int[] cntxt, int contextIndex, double probUpper, int depth) {
        //get predictive counts in restaurant as well as restaurant discount and concentration
        rest.getRestCounts(token, restCounts);
        double restDiscount = discounts.get(depth);
        double restConcentration = concentrations.get(depth);

        //get probability of observing the token in this restaurant
        double prob;
        double totalWeight = restCounts[2] + restConcentration;
        if (restCounts[3] > 0) {
            prob = 1.0* (restCounts[0] - restCounts[1] * restDiscount) / totalWeight +
                    (restDiscount * restCounts[3] + restConcentration) * probUpper / totalWeight;
        } else {
            prob = probUpper;
        }

        //update discount gradient
        if (restCounts[3] > 0) {
            if (rest.getParent() == null) {
                discounts.addToGradient(restCounts[1], restCounts[3], restCounts[2], alphabetSize);
            } else {
                discounts.addToDiscountGradient(depth, depth, restCounts[1], restCounts[3], restCounts[2], probUpper);
            }
        }

        //if leaf node then calculated log loss and seat token in the restaurant
        if (contextIndex == -1) {
            logLoss = Math.log(prob);
            return rest.sitAtRest(token, probUpper, restDiscount, restConcentration);
        }

        //if not leaf node then find childRest
        Integer childKey = new Integer(cntxt[contextIndex]);
        Restaurant childRest = rest.get(childKey);

        //if no children in the direction of this obs will need to create one
        if (childRest == null) {
            childRest = new SamplingRestaurant(rest);
            //childRest = new OnlineRestaurant(rest);
            rest.put(childKey, childRest);
        }

        if (this.seatToken(childRest, token,cntxt, --contextIndex, prob, ++depth) != null) {
            return rest.sitAtRest(token, probUpper, restDiscount, restConcentration);
        } else {
            return null;
        }
    }

    public void getPredDist(double[] predDist, int[] cntxt) {
        assert(predDist.length == alphabetSize);

        Arrays.fill(predDist, 1.0 / alphabetSize);

        if (contextFreeRestaurant.hasNoCustomers()) {
            return;
        }

        PredictiveCounts pc = new PredictiveCounts(alphabetSize);
        contextFreeRestaurant.fillPredictiveCounts(discounts.get(0), concentrations.get(0), pc);
        for (int j = 0; j < alphabetSize; j++) {
            predDist[j] *= 1.0 * (pc.tables * pc.discount + pc.concentration) / (pc.concentration + pc.cust);
            predDist[j] += 1.0 * pc.typeNum[j] / (pc.concentration + pc.cust);
        }

        if(cntxt == null){
            return;
        }

        Restaurant currentRest = contextFreeRestaurant;
        int d = 1;
        for (int i = 0; i < cntxt.length; i++) {
            currentRest = currentRest.get(new Integer(cntxt[cntxt.length - 1 - i]));

            if (currentRest == null) {
                break;
            }

            currentRest.fillPredictiveCounts(discounts.get(d), concentrations.get(d), pc);

            for (int j = 0; j < alphabetSize; j++) {
                predDist[j] *= 1.0 * (pc.tables * pc.discount + pc.concentration) / (pc.concentration + pc.cust);
                predDist[j] += pc.typeNum[j] / (pc.concentration + pc.cust);
            }
            d++;
        }
    }

    private void updateContext(int obs){
        if(context == null){
            context = new int[]{obs};
        } else if(context.length<depth){
            int[] newContext = new int[context.length + 1];
            System.arraycopy(context, 0, newContext, 0, context.length);
            newContext[context.length] = obs;
            context = newContext;
        } else {
            for(int i = 0; i< depth -1; i++){
                context[i] = context[i + 1];
            }
            context[depth-1] = obs;
        }
    }
    
/*

    public double getLogLik() {
        double logLik = 0.0;
        double restDiscount = discounts.get(0);
        double restConcentration = concentrations.get(0);
        int custInRest = 0;
        int tablesInRest = 0;

        if (!discounts.discountInRange() || !concentrations.concentrationInRange()) {
            return Double.NEGATIVE_INFINITY;
        }

        //treat first type seperately
        
        //fist person at first table
        logLik += Math.log(1.0 / alphabetSize);
        custInRest++;
        
        //rest of first table
        for(int cust = 1; cust<contextFreeRestaurant.state[0][1]; cust++){
            logLik += Math.log(cust - restDiscount) - Math.log(custInRest + restConcentration);
            custInRest++;
        }
        tablesInRest++;

        //rest of first type
        for (int table = 2; table < contextFreeRestaurant.state[0].length; table++) {
            logLik += Math.log(restDiscount * (tablesInRest) + restConcentration) - Math.log(custInRest + restConcentration) + Math.log(1.0 / alphabetSize);
            custInRest++;
            for (int cust = 1; cust < contextFreeRestaurant.state[0][table]; cust++) {
                logLik += Math.log(cust - restDiscount) - Math.log(custInRest + restConcentration);
                custInRest++;
            }
            tablesInRest++;
        }

        //now handle rest of types
        for (int typeIndex = 1; typeIndex < contextFreeRestaurant.state.length; typeIndex++) {
            for (int table = 1; table < contextFreeRestaurant.state[typeIndex].length; table++) {
                logLik += Math.log(restDiscount * tablesInRest + restConcentration) - Math.log(custInRest + restConcentration) + Math.log(1.0 / alphabetSize);
                custInRest++;
                for (int cust = 1; cust < contextFreeRestaurant.state[typeIndex][table]; cust++) {
                    logLik += Math.log(cust - restDiscount) - Math.log(custInRest + restConcentration);
                    custInRest++;
                }
                tablesInRest++;
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
        int tablesInRest = 0;

        //treat first type seperately

        //do first table
        custInRest++;
        for(int cust = 1; cust<rest.state[0][1]; cust++){
            logLik += Math.log(cust - restDiscount) - Math.log(custInRest + restConcentration);
            custInRest++;
        }
        tablesInRest++;

        //do rest of first type
        for(int table = 2; table< rest.state[0].length; table++){
            logLik += Math.log(restDiscount * tablesInRest + restConcentration) - Math.log(custInRest + restConcentration);
            for(int cust = 1; cust<rest.state[0][table]; cust++){
                logLik += Math.log(cust - restDiscount) - Math.log(custInRest + restConcentration);
                custInRest++;
            }
            tablesInRest++;
        }

        //do all other types
        for (int typeIndex = 1; typeIndex < rest.state.length; typeIndex++) {
            for (int table = 1; table < rest.state[typeIndex].length; table++) {
                logLik += Math.log(restDiscount * tablesInRest + restConcentration) - Math.log(custInRest + restConcentration);
                custInRest++;
                for (int cust = 1; cust < rest.state[typeIndex][table]; cust++) {
                    logLik += Math.log(cust - restDiscount) - Math.log(custInRest + restConcentration);
                    custInRest++;
                }
                tablesInRest++;
            }
        }

        //go through children to find their contribution
        for (Restaurant child : rest.values()) {
            logLik += this.getLogLik(child, depth + 1);
        }

        return logLik;
    }

    
    /*
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

