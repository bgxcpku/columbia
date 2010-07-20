/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

/**
 *
 * @author nicholasbartlett
 */
public interface PredictiveModel {
    public abstract int inverseCDF(double pointOnCDF, double[] interval);

    public abstract void cumulativeDistributionInterval(int token, double[] interval);
}
