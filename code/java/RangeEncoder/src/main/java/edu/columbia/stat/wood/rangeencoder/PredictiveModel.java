/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.rangeencoder;

/**
 *
 * @author nicholasbartlett
 */
public interface PredictiveModel {
    //assumes observation is 0-alphabetSize
    public abstract double continueSequence(int observation);

    public abstract int inverseCDF(double pointOnCDF);

    //returns points on cdf which are low and high or the interval around the
    //token's contriubtion to the CDF
    public abstract double[] cumulativeDistributionInterval(int token);

}
