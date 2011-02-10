/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stats.hdplda;

/**
 *
 * @author nicholasbartlett
 *
 * This class implements the CondDistrib interface for a discrete distribution
 * over the first k natural numbers unioned with zeros as specified by the
 * constructor call.
 *
 * Changes
 * 10/31/2009 NSB Initially created code
 */
public class DiscreteDistrib {

    public double[] parameter;

    //Constructor to initialize parameter given the weights w
    public DiscreteDistrib(double[] w) {
        int l = w.length;
        parameter = new double[l];
        double sumW = 0;
        //calculate total sum of weights, throw exception if weight is negative
        for (int j = 0; j < l; ++j) {
            if (w[j] >= 0) {
                sumW += w[j];
            } else {
                throw new RuntimeException("Cannot use a weight " +
                        "which is negative in a discrete distribution");
            }
        }
        //initialize parameters
        for (int j = 0; j < l; ++j) {
            parameter[j] = w[j] / sumW;
        }
    }

    //Constructor to initialize parameter to a uniform distribution over
    //0-(k-1)
    public DiscreteDistrib(int k) {
        parameter = new double[k];
        for (int j = 0; j < k; ++j) {
            parameter[j] = 1.0 / k;
        }
    }
/*
    //Method to return a sample from the distribution
    public Document sample() {
        double rawSample = Math.random();
        Document samp = new Document(parameter.length);

        double cumSum = 0;
        for (int j = 0; j < parameter.length; ++j) {
            cumSum += parameter[j];
            if (rawSample < cumSum) {
                samp.value[j] = 1;
                break;
            }
        }
        return samp;
    }

    //Method to return a multimnomail sample from the distribution with m draws
    public Document sample(int m) {
        Document samp = new Document(parameter.length);
        for (int j = 0; j < m; ++j) {
            samp.plus(sample());
        }
        return samp;
    }
*/
    //Method to return likelihood evaluated at SS
    public double getLikelihood(Token obs) {
        return Math.exp(getLogLikelihood(obs));
    }

    //Method to return log-likelihood evaluated at SS
    public double getLogLikelihood(Token obs) {
    

        return Math.log(parameter[obs.getType()]);
    }
}
