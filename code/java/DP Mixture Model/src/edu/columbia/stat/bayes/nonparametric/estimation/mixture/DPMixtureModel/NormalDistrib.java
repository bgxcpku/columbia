/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel;
import java.util.ArrayList ;
import java.util.Random ;

/**
 *
 * @author nicholasbartlett
 */
public  class NormalDistrib
        extends AbstractPostSampleableCondDistrib {
    private double mean ;
    private double sigma2 ;

    public NormalDistrib(double mean, double sigma2){
        if(sigma2 > 0){
            this.mean = mean ;
            this.sigma2 = sigma2 ;
        } else new RuntimeException("Sigma2 must be greater than 0 to be nondegenerate") ;
        distribDesc = DistribDesc.NORMAL ;
    }

    public NormalDistrib(double[] params){
        if(params[1] > 0){
            mean = params[0] ;
            sigma2 = params[1] ;
        } else new RuntimeException("Sigma2 must be greater than 0 to be nondegenerate") ;
        distribDesc = DistribDesc.NORMAL ;
    }

    public NormalDistrib(){
        this(1.0, 1.0) ;
        distribDesc = DistribDesc.NORMAL ;
    }

    //Method to return a sample from the distribution
    public Double sample() {
        Double returnVal = mean + (new Random().nextGaussian())*Math.sqrt(sigma2) ;
        return returnVal ;
    };

    //Method to return likelihood evaluated at SS
    public double getLikelihood(SufficientStat ss){
        return Math.exp(getLogLikelihood(ss)) ;
    }

    //Method to return log-likelihood evaluated at SS
    public double getLogLikelihood(SufficientStat ss){
        double ll = 0 ;
        
        double yBar = (Double)ss.value.get(0)/ss.n ;
        double s2 = (Double)ss.value.get(1) - ss.n*Math.pow(yBar,2) ;

        //first consider the normalizing constant
        ll -= (1.0*ss.n/2)*Math.log(2*Math.PI*sigma2) ;
        //create a var mult to contain 1/2*sigma2
        double mult = 1.0/(2*sigma2) ;
        //add in part due to MSE of data
        ll -= mult*s2 ;
        //update mult by multiplying by ss.n to use for piece regarding the
        //difference between the mean and xbar.
        mult *= ss.n ;
        ll -= mult*Math.pow(yBar - mean, 2);
        return ll ;
    }

    //Method to return number proportional to likelihood evaluatd at SS
    public double getPLikelihood(SufficientStat ss){
        return Math.exp(getCLogLikelihood(ss)) ;
    }

    //Method to return number equal to log-likelihood plus a constant
    //evaluatd at SS
    public double getCLogLikelihood(SufficientStat ss){
        double ll = 0 ;

        double yBar = (Double)ss.value.get(0)/ss.n ;
        double s2 = (Double)ss.value.get(1) - ss.n*Math.pow(yBar,2) ;

        //first consider the normalizing constant
        ll -= (1.0*ss.n/2)*Math.log(sigma2) ;
        //create a var mult to contain 1/2*sigma2
        double mult = 1.0/(2*sigma2) ;
        //add in part due to MSE of data
        ll -= mult*s2 ;
        //update mult by multiplying by ss.n to use for piece regarding the
        //difference between the mean and xbar.
        mult *= ss.n ;
        ll -= mult*Math.pow(yBar - mean, 2);
        return ll ;
    }

    //Method to return parameter of distribution
    public ArrayList<Double> getParameter(){
        ArrayList<Double> returnVal = new ArrayList<Double>(2) ;
        returnVal.add(this.mean) ;
        returnVal.add(this.sigma2) ;
       
        return returnVal ;
    }

    //Method to reSample the parameter values given a conjugate prior, in this
    //case, the Normal prior, or the Normal Inverse Gamma prior ;
    public void resampleConjugate(SufficientStat ss, ArrayList priorParams){
        if(priorParams.size() == 2){
            /* assume that the prior is just on the mean with the priorParams
             * being the mean and variance of the prior normal distn
             */
            double priorMean = (Double)priorParams.get(0) ;
            double priorSigma2 = (Double)priorParams.get(1) ;

            double postMean = (priorMean/priorSigma2 + (Double)ss.value.get(0)/sigma2) ;
            postMean *= 1.0/(1.0/priorSigma2 + ss.n/sigma2) ;
            double postVar = 1.0/(1.0/priorSigma2 + ss.n/sigma2) ;

            mean = new NormalDistrib(postMean, postVar).sample() ;
            return ;
        } else if(priorParams.size() == 4){
            /* here the assumption is the Normal Inverse Gamma prior, with the
             * parameters being mu,k,alpha,beta, such that the prior specifies
             * that mean|sigma2 ~N(mu,sigma2/k) and that marginally
             * sigma2 ~ InvGamma(alpha, beta)
             */
           double yBar = (Double)ss.value.get(0)/ss.n ;
           double mu0 = (Double)priorParams.get(0) ;
           double k0 = (Double)priorParams.get(1) ;
           double v0 = (Double)priorParams.get(2) ;
           double sigma0 = (Double)priorParams.get(3) ;

           double muN =(k0/(k0 + ss.n))*mu0 + (ss.n/(k0 + ss.n))*yBar ;
           double kN = k0 + ss.n ;
           double vN = v0 + ss.n ;
           double vNsigmaN = v0*sigma0 + (Double)ss.value.get(1) - ss.n*Math.pow(yBar,2) +
                   (k0*ss.n/(k0 + ss.n))*Math.pow(yBar - mu0,2) ;
           double sigma2inv = new GammaDistrib(vN/2,2.0/vNsigmaN).sample() ;
           sigma2 = 1.0/sigma2inv ;
           mean = new NormalDistrib(muN, sigma2/kN).sample() ;
        } else {
            throw new RuntimeException("Can only resample with conjugate " +
                    "prior which has 2 or 4 parameters") ;
        }
    }

    public int getNumParameters() {
        return 2;
    }
}
