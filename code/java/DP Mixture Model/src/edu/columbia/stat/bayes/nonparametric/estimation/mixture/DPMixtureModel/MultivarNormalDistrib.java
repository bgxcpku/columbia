/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel;
import Jama.* ;
import java.util.ArrayList ;
import java.util.Random ;

/**
 *
 * @author nicholasbartlett
 */
public  class MultivarNormalDistrib
        extends AbstractPostSampleableCondDistrib {
    private Matrix mean ;
    private Matrix sigma2 ;

    public MultivarNormalDistrib(double[] mean, double[][] sigma2){
        Matrix sigma2Matrix = new Matrix(sigma2) ;
        if(new CholeskyDecomposition(sigma2Matrix).isSPD()){
            this.mean = new Matrix(mean, mean.length) ;
            this.sigma2 = sigma2Matrix ;
        } else {
            throw new RuntimeException("Cannot use a covariance matrix which is " +
                "not symmetric and positive definite") ;
        }
        distribDesc = DistribDesc.MVNORMAL ;
    }

    public MultivarNormalDistrib(Matrix mean, Matrix sigma2){
        if(new CholeskyDecomposition(sigma2).isSPD()){
            this.mean = mean ;
            this.sigma2 = sigma2 ;
        } else {
            throw new RuntimeException("Cannot use a covariance matrix which is " +
                "not symmetric and positive definite") ;
        }
        distribDesc = DistribDesc.MVNORMAL ;
    }

    //Method to return a standard normal multivariate distribution with
    //the identity matrix covariance matrix
    public MultivarNormalDistrib(int n){
        mean = new Matrix(n,1,0) ;
        sigma2 = Matrix.identity(n, n) ;
        distribDesc = DistribDesc.MVNORMAL ;
    }

    //Method to return a sample from the distribution
    public Matrix sample(){
        int dim = mean.getColumnDimension() ;
        double[] rawSample = new double[dim] ;
        Random randNumGen = new Random() ;
        for(int j = 0; j<dim; ++j){
            rawSample[j] = randNumGen.nextGaussian() + mean.get(j,0) ;
        }

        Matrix samp = sigma2.chol().getL().times(new Matrix(rawSample, dim)) ;

        return samp ;
    }

    //Method to return likelihood evaluated at SS
    public double getLikelihood(SufficientStat ss){
        return Math.exp(getLogLikelihood(ss)) ;
    }

    //Method to return log-likelihood evaluated at SS
    public double getLogLikelihood(SufficientStat ss){
        int dim = mean.getColumnDimension() ;
        Matrix ss0 = (Matrix)ss.value.get(0);
        Matrix ss1 = (Matrix)ss.value.get(1);
        Matrix yBar = ss0.times(1.0/ss.n) ;
        Matrix s2 = ss1.minus(yBar.times(yBar.transpose()).times(ss.n)) ;

        double ll = 0 ;
        //add piece for normalizing constant
        ll += (-1.0*dim*ss.n/2)*Math.log(2*Math.PI) - (1.0*ss.n/2)*Math.log(sigma2.det()) ;
        Matrix ybarContribution = yBar.minus(mean).times(yBar.minus(mean).transpose()).times(1.0*ss.n) ;
        ll += (-1.0/2)*sigma2.inverse().times(s2.plus(ybarContribution)).trace() ;

        return ll ;
    }

    //Method to return number proportional to likelihood evaluatd at SS
    public double getPLikelihood(SufficientStat ss){
        return Math.exp(getCLogLikelihood(ss)) ;
    }

    //Method to return number equal to log-likelihood plus a constant
    //evaluatd at SS
    public double getCLogLikelihood(SufficientStat ss){
        int dim = mean.getColumnDimension() ;
        Matrix ss0 = (Matrix)ss.value.get(0);
        Matrix ss1 = (Matrix)ss.value.get(1);
        Matrix yBar = ss0.times(1.0/ss.n) ;
        Matrix s2 = ss1.minus(yBar.times(yBar.transpose()).times(ss.n)) ;

        double ll = 0 ;
        //add piece for normalizing constant
        ll -= (1.0*ss.n/2)*Math.log(sigma2.det()) ;

        Matrix ybarContribution = yBar.minus(mean).times(yBar.minus(mean).transpose()).times(1.0*ss.n) ;
        ll += (-1.0/2)*sigma2.inverse().times(s2.plus(ybarContribution)).trace() ;
        
        return ll ;
    }

    //Method to return parameter of distribution
    public ArrayList getParameter(){
        ArrayList returnVal = new ArrayList(2) ;
        returnVal.add(mean) ;
        returnVal.add(sigma2) ;
        return returnVal ;
    }

    public void resampleConjugate(SufficientStat ss, ArrayList priorParams){
        if(priorParams.size() == 2){
            /* assume that the prior is just on the mean with the priorParams
             * being the mean and variance of the prior normal distn, each
             * should be a matrix, the mean being a column matrix, and the prior
             * variance being a kxk symmetric positive definite matrix
             */
            Matrix priorMean = (Matrix)priorParams.get(0) ;
            Matrix priorCov  = (Matrix)priorParams.get(1) ;

            if(!new CholeskyDecomposition(sigma2).isSPD()){
                throw new RuntimeException("Cannot use a prior covariance " +
                        "matrix which is not symetric and positive definite") ;
            }
            
            Matrix ss0 = (Matrix)ss.value.get(0) ;
            Matrix yBar = ss0.times(1.0/ss.n) ;
            Matrix postCov = priorCov.inverse().plus(sigma2.inverse()
                    .times(ss.n.doubleValue())).inverse() ;
            Matrix postMean1 = priorCov.inverse().times(priorMean)
                    .plus(sigma2.inverse().times(yBar).times(ss.n.doubleValue())) ;
            Matrix postMean = postCov.times(postMean1) ;

            mean = new MultivarNormalDistrib(postMean, postCov).sample() ;
        } else if(priorParams.size() == 4){
            /* here the assumption is the Normal Inverse Gamma prior, with the
             * parameters being mu,k,degFreedom,scaleInv, such that the prior specifies
             * that mean|sigma2 ~N(mu,sigma2/k) and that marginally
             * sigma2 ~ InvWishart(degFreedom, scaleInv)
             * mu is a kx1 matrix, k is a double, degFreedom is an int, and
             * scaleInv is a kxk matrix
             */

            Matrix mu0 = (Matrix)priorParams.get(0) ;
            double k0 = (Double)priorParams.get(1) ;
            int v0 = (Integer)priorParams.get(2) ;
            Matrix scale0 = (Matrix)priorParams.get(3) ;
            scale0 = scale0.inverse() ;

            Matrix ss0 = (Matrix)ss.value.get(0) ;
            Matrix ss1 = (Matrix)ss.value.get(1) ;
            Matrix yBar = ss0.times(1.0/ss.n) ;
            Matrix s2 = ss1.minus(yBar.times(yBar.transpose()).times(ss.n)) ;

            Matrix muN = mu0.times(k0/(k0+ss.n)).plus(yBar.times(1.0*ss.n/(k0 + ss.n))) ;
            double kN = k0 + ss.n ;
            int vN = v0 + ss.n ;
            Matrix scaleN = yBar.minus(mu0).times(yBar.minus(mu0).transpose()) ;
            scaleN = scaleN.times(k0*ss.n/(k0 + ss.n)) ;
            scaleN = scaleN.plus(s2).plus(scale0) ;

            sigma2 = new WishartDistrib(vN, scaleN).sample() ;
            sigma2 = sigma2.inverse() ;

            mean = new MultivarNormalDistrib(muN,sigma2.times(1.0/kN)).sample() ;
        } else {
            throw new RuntimeException("The number of prior parameters in the " +
                    "multivariate normal disribution must be eithe 2 or 4") ;
        }
    }

    public int getNumParameters(){
        return 2 ;
    }
}
