/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;
import java.util.Random;

/**
 *
 * @author nicholasbartlett
 *
 * we use the parameterization of the density as
 * beta^alpha/gamma(alpha) * x^(alpha-1) exp(-x*beta) ;
 */
public  class GammaDistrib {
    private double alpha ;
    private double beta ;

    public GammaDistrib(double alpha, double beta){
        if(alpha >0 & beta>0){
            this.alpha = alpha ;
            this.beta = beta ;
        } else {
            throw new RuntimeException("Parameters of the Gamma distribution " +
                    "must be positive") ;
        }
        if(!RAND_INIT){
            RAND.setSeed(0) ;
            RAND_INIT = true ;
        }
    }

    /**
     * Returns a double sampled according to this distribution.  Uniformly
     * fast for all k > 0.  (Reference: Non-Uniform Random Variate Generation,
     * Devroye http://cgm.cs.mcgill.ca/~luc/rnbookindex.html)  Uses Cheng's
     * rejection algorithm (GB) for k>=1, rejection from Weibull distribution
     * for 0 < k < 1.
     */

    public static Random RAND  = new Random() ;
    public static boolean RAND_INIT = false ;

    //Method to return a sample from the distribution
    public Double sample(){
        double k = alpha;
        double lambda = beta ;

        boolean accept = false;
	if (k >= 1) {
	    //Cheng's algorithm
	    double b = (k - Math.log(4));
	    double c = (k + Math.sqrt(2*k - 1));
	    double lam = Math.sqrt(2*k - 1);
	    double cheng = (1 + Math.log(4.5));
	    double u, v, x, y, z, r;
	    do {
		u = RAND.nextDouble();
		v = RAND.nextDouble();
		y = ((1 / lam) * Math.log(v / (1 - v)));
		x = (k * Math.exp(y));
		z = (u * v * v);
		r = (b + (c * y) - x);
		if ((r >= ((4.5 * z) - cheng)) || (r >= Math.log(z))) {
		    accept = true;
		}
	    } while (!accept );

            return new Double(x / lambda) ;
	}
	else {
	    //Weibull algorithm
	    double c = (1 / k);
	    double d = ((1 - k) * Math.pow(k, (k / (1 - k))));
	    double u, v, z, e, x;
	    do {
		u = RAND.nextDouble();
		v = RAND.nextDouble();
		z = -Math.log(u); //generating random exponential variates
		e = -Math.log(v);
		x = Math.pow(z, c);
		if ((z + e) >= (d + x)) {
		    accept = true;
		}
	    } while (!accept);

            return new Double(x / lambda) ;
	}
    }

    //Method to return likelihood evaluated at SS
    public double getLikelihood(double x){
        return Math.exp(getLogLikelihood(x)) ;
    }

    //Method to return log-likelihood evaluated at SS
    public double getLogLikelihood(double x){
        double ll = 0 ;
        ll += alpha*Math.log(beta) ;
        ll -= GammaDistrib.lnGammaFunction(alpha) ;
        ll += (alpha - 1)*Math.log(x) ;
        ll -= beta*x ;
        return ll ;
    }

     /*
     * Returns an approximation of the log of the Gamma function
     * of x.  Laczos Approximation
     * Reference: Numerical Recipes in C
     * http://www.library.cornell.edu/nr/cbookcpdf.html
     */
    public static double lnGammaFunction(double x){
     	//Ripped off from the blog implementation
	double[] cof = {76.18009172947146, -86.50532032941677,
			24.01409824083091,-1.231739572450155,
			0.1208650973866179e-2,-0.5395239384953e-5};
	double y, ser, tmp;
	y = x;
	tmp = x + 5.5;
	tmp -= ((x + 0.5) * Math.log(tmp));
	ser = 1.000000000190015;
	for (int j = 0; j < 6; j += 1) {
	    y += 1;
	    ser += (cof[j] / y);
	}
	return (-tmp + Math.log(2.5066282746310005*ser / x));
    }

    public static double gammaFunction(double val){
        return Math.exp(lnGammaFunction(val)) ;
    } ;
}
