/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel;
import Jama.Matrix ;
import java.util.ArrayList;
/**
 *
 * @author nicholasbartlett
 */
public  class NormalInvGammaDistrib
        extends  AbstractCondDistrib {
    private double mean ;
    private int meanScale ;
    private double alpha ;
    private double beta ;

    public NormalInvGammaDistrib(double mean
                                ,int meanScale
                                ,double alpha
                                ,double beta){
        this.mean = mean ;
        this.meanScale = meanScale ;
        this.alpha = alpha ;
        this.beta = beta ;
        distribDesc = DistribDesc.NORMALINVGAMMA ;
    }

    public Matrix sample(){
        Matrix returnVal = new Matrix(2,1,0) ;

        double invSigma2 = new GammaDistrib(alpha, beta).sample() ;
        double sigma2 = 1.0/invSigma2 ;
        double sampledMean = new NormalDistrib(mean,sigma2/meanScale).sample() ;


        returnVal.set(0,0,sampledMean) ;
        returnVal.set(1,0,sigma2) ;
        return returnVal ;
    }

    //Method to return likelihood evaluated at SS
    public double getLikelihood(SufficientStat ss){
        throw new UnsupportedOperationException("Not supported yet.") ;
    }

    //Method to return log-likelihood evaluated at SS
    public double getLogLikelihood(SufficientStat ss){
        throw new UnsupportedOperationException("Not supported yet.") ;
    }

    //Method to return number proportional to likelihood evaluatd at SS
    public double getPLikelihood(SufficientStat ss){
        throw new UnsupportedOperationException("Not supported yet.") ;
    }

    //Method to return number equal to log-likelihood plus a constant
    //evaluatd at SS
    public double getCLogLikelihood(SufficientStat ss){
        throw new UnsupportedOperationException("Not supported yet.") ;
    }

    //Method to return parameter of distribution
    public ArrayList<Double> getParameter(){
        ArrayList<Double> returnVal = new ArrayList<Double>(4);
        returnVal.add(mean) ;
        returnVal.add(1.0*meanScale) ;
        returnVal.add(alpha) ;
        returnVal.add(beta) ;
        return returnVal ;
    }

    //Method to return the number of parameters in the distribution ;
    public int getNumParameters(){
        return 4 ;
    }

}
