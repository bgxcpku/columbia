/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;


/**
 * @author nicholasbartlett
 */
public class DirichletDistrib {
    public double[] parameter ;

    //constructor to initialize dirichlet distribution
    public DirichletDistrib(double[] params){
        parameter = params ;
        checkParameter() ;
    }

    //constructor to initialize uniform dirichlet distribution with weight 1
    public DirichletDistrib(int k){
        parameter = new double[k] ;
        for(int j = 0; j<k; ++j){
            parameter[j] = 1.0/k ;
        }
    }

    //constructor to initialize uniform dirichlet distribution with weight totalWeight
    public DirichletDistrib(double totalWeight, int k){
        parameter = new double[k] ;
        for(int j = 0; j<k; ++j){
            parameter[j] = totalWeight/k ;
        }
    }

    //method to check that all parameters are positive
    private void checkParameter(){
        for(int j=0; j<parameter.length; ++j){
            if(parameter[j]<=0){
                throw new RuntimeException("Dirichlet Distribution parameters " +
                        "must be postive.") ;
            }
        }
    }

    //Method to return a sample from the distribution
    public DiscreteDistrib sample(){
        double[] rawSample = new double[parameter.length] ;
        double sumSamp = 0 ;
        
        for(int j = 0; j < parameter.length; j++){
            double sampToAdd = new GammaDistrib(parameter[j],1).sample() ;
            rawSample[j] = sampToAdd ;
            sumSamp += sampToAdd ;
        }
        for(int j = 0; j < parameter.length; j++){
            rawSample[j] = rawSample[j]/sumSamp ;
        }

        return new DiscreteDistrib(rawSample) ;
    }

    
    //Method to return likelihood evaluated at SS
    public double getLikelihood(DiscreteDistrib x){
        return Math.exp(getLikelihood(x)) ;
    }

    //Method to return log-likelihood evaluated at SS
    public double getLogLikelihood(DiscreteDistrib x) {
        double ll = 0 ;
        double totalWeight = 0 ;
        for(int j = 0; j<parameter.length; ++j){
            ll += (parameter[j] - 1)*x.parameter[j] ;
            ll -= GammaDistrib.lnGammaFunction(parameter[j]) ;
            totalWeight += parameter[j] ;
        }
        ll += GammaDistrib.lnGammaFunction(totalWeight) ;
        return ll ;
    }
}
