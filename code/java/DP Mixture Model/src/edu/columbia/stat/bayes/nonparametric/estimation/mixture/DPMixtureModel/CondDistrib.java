/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel;
import java.util.ArrayList ;

/**
 *
 * @author nicholasbartlett
 */
public interface CondDistrib {

    //Method to return a sample from the distribution
    Object sample() ;

    //Method to return likelihood evaluated at SS
    double getLikelihood(SufficientStat ss) ;

    //Method to return log-likelihood evaluated at SS
    double getLogLikelihood(SufficientStat ss) ;

    //Method to return number proportional to likelihood evaluatd at SS
    double getPLikelihood(SufficientStat ss) ;

    //Method to return number equal to log-likelihood plus a constant
    //evaluatd at SS
    double getCLogLikelihood(SufficientStat ss) ;

    //Method to return parameter of distribution
    ArrayList getParameter() ;

    //Method to return the number of parameters in the distribution ;
    int getNumParameters() ;
}
