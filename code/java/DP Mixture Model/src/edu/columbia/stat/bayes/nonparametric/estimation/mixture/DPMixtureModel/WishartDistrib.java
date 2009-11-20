/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel;

import java.util.ArrayList ;
import Jama.* ;

/**
 * @author nicholasbartlett
 */
public  class WishartDistrib
        extends AbstractCondDistrib {
    private int degFreedom ;
    private Matrix scale ;

    public WishartDistrib(int degFreedom, double[][] scale){
        this.degFreedom = degFreedom ;
        this.scale = new Matrix(scale) ;
        distribDesc = DistribDesc.WISHART ;
    }

    public WishartDistrib(int degFreedom, Matrix scale){
        this.degFreedom = degFreedom ;
        this.scale = scale ;
        distribDesc = DistribDesc.WISHART ;
    }

    //Method to return a sample from the distribution
    public Matrix sample(){
        int dim = scale.getColumnDimension() ;

        MultivarNormalDistrib mvnd = new MultivarNormalDistrib(new Matrix(dim,1,0)
                                                              ,scale) ;
        Matrix samp = new Matrix(dim, dim,0) ;
        for(int j = 0; j<degFreedom; ++j){
            Matrix sampJ = mvnd.sample() ;
            samp.plus(sampJ.times(sampJ.transpose())) ;
        }
        
        return samp;
    }

    //Method to return likelihood evaluated at SS
    public double getLikelihood(SufficientStat ss){
        return Math.exp(this.getLogLikelihood(ss)) ;
    }

    //Method to return log-likelihood evaluated at SS
    public double getLogLikelihood(SufficientStat ss){
        int k = scale.getColumnDimension() ;
        
        double ll = 0 ;
        ll -= 1.0*ss.n*degFreedom*k/2*Math.log(2) ;
        ll -= 1.0*ss.n*k*(k-1)*Math.log(Math.PI)/4 ;
        for(int j = 0; j < k; ++j){
            ll -= 1.0*ss.n*GammaDistrib.lnGammaFunction(1.0*(degFreedom + 1 -(j + 1))/2) ;
        }
        ll -= (1.0*ss.n*degFreedom/2)*Math.log(scale.det()) ;
        ll += (1.0*(degFreedom - k -1)/2)*(Double)ss.value.get(0) ;
        ll -= (1.0/2)*scale.inverse().times((Matrix)ss.value.get(1)).trace() ;
        return ll ;
    }

    //Method to return number proportional to likelihood evaluatd at SS
    public double getPLikelihood(SufficientStat ss){
        return Math.exp(getCLogLikelihood(ss)) ;
    }

    //Method to return number equal to log-likelihood plus a constant
    //evaluatd at SS
    public double getCLogLikelihood(SufficientStat ss){
        int k = scale.getColumnDimension() ;

        double ll = 0 ;
        ll -= (1.0*ss.n*degFreedom*k/2)*Math.log(2) ;
        for(int j = 0; j < k; ++j){
            ll -= 1.0*ss.n*GammaDistrib.lnGammaFunction(1.0*(degFreedom + 1 -(j + 1))/2) ;
        }
        ll -= (1.0*ss.n*degFreedom/2)*Math.log(scale.det()) ;
        ll += (1.0*(degFreedom - k -1)/2)*(Double)ss.value.get(0) ;
        ll -= (1.0/2)*scale.inverse().times((Matrix)ss.value.get(1)).trace() ;
        return ll ;
    }

    //Method to return parameter of distribution
    public ArrayList getParameter(){
        ArrayList returnVal = new ArrayList(2) ;
        returnVal.add(degFreedom) ;
        returnVal.add(scale) ;
        
        return returnVal ;
    }

    public int getNumParameters(){
        return 2 ;
    }
}
