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

    public double[] discounts;
    public double[] gradient;

    public Discounts() {
        this(new double[]{.5});
    }

    public Discounts(double[] discounts) {
        this.discounts = discounts;
        this.gradient = new double[this.discounts.length];
    }

    public void set(double[] newDiscounts) {
        this.discounts = newDiscounts;
    }

    public double get(int i) {
        if (i < discounts.length) {
            return discounts[i];
        } else {
            return discounts[discounts.length - 1];
        }
    }

    public double getLog(int i) {
        if (i < discounts.length) {
            return Math.log(discounts[i]);
        } else {
            return Math.log(discounts[discounts.length - 1]);
        }
    }

    public double getLog(int parentDepth, int depth) {
        double logDiscount = 0.0;
        int childDepth = parentDepth;
        int distanceDown = depth - parentDepth;
        while (childDepth < (discounts.length - 1) && distanceDown > 0) {
            childDepth++;
            distanceDown--;
            logDiscount += Math.log(discounts[childDepth]);
        }
        if (distanceDown > 0) {
            logDiscount += distanceDown * Math.log(discounts[discounts.length - 1]);
        }
        return logDiscount;
    }

    public double getGradient(int i) {
        if (i < gradient.length) {
            return gradient[i];
        } else {
            return gradient[gradient.length - 1];
        }
    }

    public void clearGradient() {
        for (int i = 0; i < gradient.length; i++) {
            gradient[i] = 0.0;
        }
    }

    public void addToDiscountGradient(int startLevel, int endLevel, int tw, int t, int c, double parentProb) {
        int minEnd = (endLevel < discounts.length - 1) ? endLevel : discounts.length - 2;
        int minStart = (startLevel < discounts.length - 1) ? startLevel : discounts.length - 2;
        double disc = Math.exp(this.getLog(startLevel - 1, endLevel));
        for (int i = 0; i <= minStart; i++) {
            gradient[i] *= disc * t / ((double) c);
        }
        for (int i = startLevel; i <= minEnd; i++) {
            double dWithout = disc / discounts[i];
            assert (gradient[i] == 0.0);
            gradient[i] = (-tw * dWithout + parentProb * t * dWithout) / ((double) c);
        }
        if (endLevel >= discounts.length - 1) {
            int numLast = endLevel - discounts.length + 1;
            double dWithout = disc / discounts[discounts.length - 1] * (numLast);
            gradient[discounts.length - 1] = (-tw * dWithout + disc * t * gradient[discounts.length - 1] + parentProb * t * dWithout) / ((double) c);
        }
    }

    /**
     * Gradient at the root node.
     *
     * @param tw Number of tables of type w
     * @param c Number of customers
     */
    public void addToGradient(int tw, int t, int c, int alphabetSize) {
        gradient[0] += (-tw + t / ((double) alphabetSize)) / ((double) c);
    }

    /**
     * Take a step in the direction of the gradient. This should be called after
     * addToGradient() has been called for each node in the path.
     */
    public void stepGradient(double eps, double prob) {
        double[] newDiscounts = new double[discounts.length];

        for (int i = 0; i < gradient.length; i++) {
            newDiscounts[i] = discounts[i] + eps * gradient[i] / prob;
            if(newDiscounts[i] <= 0){
                newDiscounts[i] = discounts[i] / 2.0;
            } else if(newDiscounts[i] >= 1){
                newDiscounts[i] = discounts[i] + (1.0 - discounts[i])/2.0;
            }

            if(newDiscounts[i] <= 0.0 || newDiscounts[i] >= 1.0){
                newDiscounts[i] = discounts[i];
            }
        }
        discounts = newDiscounts;
        clearGradient();
    }

    public void printArray(double[] arrayToPrint){
        for(int i = 0; i<arrayToPrint.length; i++){
            if(i == 0){
                System.out.print("[" + arrayToPrint[i]);
            } else {
                System.out.print(", " + arrayToPrint[i]);
            }
        }
        System.out.println("]");
    }
}
