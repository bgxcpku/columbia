/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public class Discounts {
    private double[] discounts, logDiscounts, discountGradient;
    private double alpha, alphaGradient;

    public Discounts(double[] initialDiscounts, double dInfinity){
        if(dInfinity <= 0.0 || dInfinity >= 1.0){
            throw new IllegalArgumentException("dInfinity must be in the interval (0.0,1.0)");
        }

        alpha = Math.log(dInfinity) / (Math.log(dInfinity) + Math.log(initialDiscounts[initialDiscounts.length-1]));
        discounts = initialDiscounts;
        logDiscounts = new double[initialDiscounts.length];
        discountGradient = new double[initialDiscounts.length];

        fillLogDiscounts();
    }

    public double get(int index){
        if(index >= discounts.length){
            throw new IllegalArgumentException("Must only get discounts with an index in [0, length())");
        }
        return discounts[index];
    }

    public void set(int index, double value){
        if(index >= discounts.length){
            throw new IllegalArgumentException("Must only set discounts with an index in [0, length())");
        }
        discounts[index] = value;
    }

    public double getdInfinity(){
        return Math.pow(discounts[discounts.length-1],alpha / (1 - alpha));
    }

    public void setDInfinity(double value){
        alpha = Math.log(value) / (Math.log(value) + Math.log(discounts[discounts.length-1]));
    }

    public double get(int parentDepth, int depth){
        double logDiscount;
        int d;

        if(parentDepth >= depth && (parentDepth != 0 && depth !=0)){
            throw new IllegalArgumentException("parent depth (" + parentDepth + ") " +
                 "must be less than depth of this restaurant (" + depth + ")");
        }

        d = parentDepth + 1;
        logDiscount = 0.0;
        
        if(depth == 0){
            logDiscount = logDiscounts[0];
        } else {
            while(d <= depth && d < discounts.length - 1){
                logDiscount += logDiscounts[d++];
            }

            if(depth >= discounts.length - 1){
                logDiscount += logDiscounts[discounts.length - 1] * Math.pow(alpha, (double) d - (double) discounts.length + 1.0) * (1.0 - Math.pow(alpha, (double) depth - (double) d + 1.0)) / (1.0 - alpha);
            }
        }

        return Math.exp(logDiscount);
    }

    public int length(){
        return discounts.length;
    }

    public void clearGradient(){
        for(int i = 0; i < discountGradient.length; i++){
            discountGradient[i] = 0.0;
        }
        alphaGradient = 0.0;
    }

    //assume going up the path from the node to the root
    public void updateGradient(int parentDepth, int depth, int typeTables, int customers, int tables, double pp, double discount, double multFactor) {
        double derivLogDa, derivLogDd;
        int d;

        if (customers > 0) {
            d = parentDepth + 1;
            derivLogDd = 0.0;
            derivLogDa = 0.0;

            if (depth == 0) {
                derivLogDd = 1.0 / discounts[0];
                discountGradient[0] += (((double) tables * pp - (double) typeTables) * discount * derivLogDd / (double) customers) * multFactor;
            } else {
                while (d <= depth && d < discounts.length - 1) {
                    derivLogDd = 1.0 / discounts[d];
                    discountGradient[d] += (((double) tables * pp - (double) typeTables) * discount * derivLogDd / (double) customers) * multFactor;
                    d++;
                }

                if (depth >= discounts.length - 1) {
                    double a, b;

                    a = (double) d - (double) discounts.length + 1.0;
                    b = (double) depth - (double) d + 1.0;

                    derivLogDd = Math.pow(alpha, a) * (1.0 - Math.pow(alpha, b)) / (1.0 - alpha) / discounts[discounts.length - 1];
                    discountGradient[discounts.length - 1] += (((double) tables * pp - (double) typeTables) * discount * derivLogDd / (double) customers) * multFactor;

                    derivLogDa = logDiscounts[discounts.length - 1] * ((a * Math.pow(alpha, a - 1) - (a + b) * Math.pow(alpha, a + b - 1)) / (1.0 - alpha) + (Math.pow(alpha, a) - Math.pow(alpha, a + b)) / (1.0 - alpha) / (1.0 - alpha));
                    alphaGradient += (((double) tables * pp - (double) typeTables) * discount * derivLogDa / (double) customers) * multFactor;
                }
            }
        }
    }

    public int cnt = 0;
    public void stepDiscounts(double eps, double p) {
        double proposal;

        for(int i = 0; i < discountGradient.length; i++){
            proposal = discounts[i] + eps * discountGradient[i] / p;

            if(proposal > 1.0){
                discounts[i] = 1.0;
            } else if(proposal < 0.0){
                discounts[i] = 0.00000001;
            } else {
                discounts[i] = proposal;
            }
        }

        proposal = alpha + eps * alphaGradient / p;
        if(proposal >= 1.0){
            proposal = alpha + (1.0 - alpha) / 2.0;
        } else if (proposal <= 0.0){
            proposal = alpha / 2.0;
        }

        if(proposal < 1.0 && proposal > 0.0){
            alpha = proposal;
        }

        clearGradient();
        fillLogDiscounts();
    }

    private void fillLogDiscounts(){
        int discount;

        discount = 0;
        for(double disc : discounts){
            logDiscounts[discount++] = Math.log(disc);
        }
    }

    public void print(){
        System.out.print("[" + discounts[0]);
        for(int i = 1; i<discounts.length; i++){
            System.out.print(", " + discounts[i]);
        }
        System.out.println("]");
        System.out.println("The infinite discount is = " + Math.pow(discounts[discounts.length-1],alpha / (1 - alpha)));
    }
}
