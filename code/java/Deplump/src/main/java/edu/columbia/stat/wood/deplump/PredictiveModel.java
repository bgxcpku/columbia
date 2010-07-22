/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

/**
 * Predictive model used during compression and decompression.
 *
 * @author nicholasbartlett
 */
public interface PredictiveModel {

    /**
     * Populates interval with CDF(token - 1) and CDF(token) and incorporates
     * the token into the predictive model.
     *
     * @param token token to encode / incorporate
     * @param interval container for CDF values
     */
    public abstract void cumulativeDistributionInterval(int token, double[] interval);

    /**
     * Finds the token such that CDF(token - 1) is less than pointOnCDF and
     * CDF(token) is greater than pointOnCDF.  Populates interval with CDF(token - 1)
     * and CDF(token) and incorporates the token into the predictive model.
     *
     * @param pointOnCDF point on CDF
     * @param interval container for CDF values
     * @return token such that CDF(token -1) < pointOnCDF and CDF(token) > pointOnCDF
     */
    public abstract int inverseCDF(double pointOnCDF, double[] interval);
}
