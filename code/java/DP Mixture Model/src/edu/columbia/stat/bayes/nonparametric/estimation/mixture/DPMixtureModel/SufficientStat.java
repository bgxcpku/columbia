/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel;
import java.util.ArrayList ;
import Jama.Matrix ;
import Jama.CholeskyDecomposition ;

/**
 *
 * @author nicholasbartlett
 * 
 ****Discrete*******
 * For the Discrete Distribution the length of the value ArrayList should
 * equal the number of unique values in the Discrete distribution.  Each
 * entry is the number of times that unique value is represented in the data
 * assuming the data is a random sample of size this.n from the same
 * Discrete Distribution

 *****Gamma*******
 * For the Gamma Distribution the length of the value ArrayList should be
 * two.  The first being the sum of the logs of the observed values, the
 * second being the sum of the observations.

 ****Dirichlet*******
 * For the Dirichlet Distribution the length of the value ArrayList should
 * be equal to the number of parameters in the corresponding distriubtion.
 * Each entry in the SS should be the component wise sum of the log values

 ****Normal*******
 * For the Normal Distribution the length of the value ArrayList should be
 * two. The first should be the sum of the obs, the second should be the sum
 * of the square of the obs

 ****Multivariate Normal*******
 * For the multivariate normal distribution the first element of the value
 * ArrayList is a column matrix which holds the coordinate wise sum of the
 * data and the second element is a matrix which holds the sum of each
 * observation, represented as a column matrix, multiplied by it's own
 * traspose

 ****Wishart*******
 * For the wishart distribution the value ArrayList should be of length two.
 * The first value is the sum of the logs of the determinants of the
 * observed matrices.  The second is a matrix, which is the sum of the
 * observed values.
 *
 */

public class SufficientStat {
    public ArrayList value ;
    public Integer n ;
    public DistribDesc distribDesc ;

    public SufficientStat(DistribDesc distribDesc, ArrayList value, int n){
        this.value = value ;
        this.n     = n     ;
        this.distribDesc = distribDesc ;
    }

    public void addObs(ArrayList obs){
        if(distribDesc == DistribDesc.DISCRETE){
            //An obs should have the same number of elemnts as the value ArrayList
            if(value.size() == obs.size()){
                for(int j=0; j<obs.size(); ++j){
                    int newVal = (Integer)obs.get(j) + (Integer)value.get(j) ;
                    value.set(j, newVal) ;
                }
            } else {
                throw new RuntimeException("Observation length must equal " +
                        "length of distribution") ;
            }
        } else if(distribDesc == DistribDesc.DIRICHLET){
            //An obs should have the same number of elemnts as the value ArrayList
            if(value.size() == obs.size()){
                for(int j=0; j<obs.size(); ++j){
                    double newVal = Math.log((Double)obs.get(j)) + (Double)value.get(j) ;
                    value.set(j,newVal) ;
                }
            } else {
                throw new RuntimeException("Observation length must equal " +
                        "length of distribution") ;
            }
        } else if(distribDesc == DistribDesc.GAMMA){
            //An obs should be non-negative
            if((Double)obs.get(0) >= 0){
                double newVal = (Double)value.get(0) + Math.log((Double)obs.get(0)) ;
                value.set(0, newVal) ;
                newVal = (Double)value.get(1) + (Double)obs.get(0) ;
                value.set(1, newVal) ;
            } else {
                throw new RuntimeException("Observation must be non-negative") ;
            }
        } else if(distribDesc == DistribDesc.NORMAL){
            double newVal = (Double)value.get(0) + (Double)obs.get(0) ;
            value.set(0,newVal) ;
            newVal = (Double)value.get(1) + Math.pow((Double)obs.get(0),2) ;
            value.set(1,newVal) ;
        } else if(distribDesc == DistribDesc.MVNORMAL){
            Matrix newVal = (Matrix)value.get(0) ;
            if(newVal.getColumnDimension() == obs.size()){
                Matrix obsMatrix = new Matrix(obs.size(),1,0) ;
                for(int j = 0; j<newVal.getColumnDimension(); ++j){
                    double newEntry =newVal.get(j,0) + (Double)obs.get(j) ;
                    newVal.set(j, 0, newEntry) ;
                    obsMatrix.set(j,0, (Double)obs.get(j)) ;
                }
                value.set(0,newVal) ;
                newVal = (Matrix)value.get(1) ;
                newVal = newVal.plus(obsMatrix.times(obsMatrix.transpose())) ;
                value.set(1,newVal) ;
            } else{
                throw new RuntimeException("Observation length must is " +
                            "not the same length as the existing SS") ;
            }
        } else if(distribDesc == DistribDesc.WISHART){
            //An obs should be a matrix the same size as the second value ArrayList
            //element, and obs matrix should be symmetric and positive definite ;
            Matrix ss1 = (Matrix)value.get(1) ;
            Matrix obsMat = (Matrix)obs.get(0) ;
            if(ss1.getColumnDimension() == obsMat.getColumnDimension() &&
                    ss1.getRowDimension() == obsMat.getRowDimension() &&
                    new CholeskyDecomposition(obsMat).isSPD()){
                double newVal = (Double)value.get(0) + Math.log(obsMat.det()) ;
                value.set(0, newVal) ;
                Matrix newVal1 = (Matrix)value.get(1) ;
                newVal1 = newVal1.plus(obsMat) ;
                value.set(1,newVal1) ;
            } else{
                throw new RuntimeException("Obs must be symetric and postive " +
                        "definite and the dimension must agree with the current " +
                        "dimension of the value(1)") ;
            }
        } 
        ++n ;
    }

    public void removeObs(ArrayList obs){
        if(distribDesc == DistribDesc.DISCRETE){
            //An obs should have the same number of elemnts as the value ArrayList
            if(value.size() == obs.size()){
                for(int j=0; j<obs.size(); ++j){
                    int newVal = (Integer)value.get(j) - (Integer)obs.get(j) ;
                    value.set(j, newVal) ;
                }
            } else {
                throw new RuntimeException("Observation length must equal " +
                        "length of distribution") ;
            }
        } else if(distribDesc == DistribDesc.DIRICHLET){
            //An obs should have the same number of elemnts as the value ArrayList
            if(value.size() == obs.size()){
                for(int j=0; j<obs.size(); ++j){
                    double newVal = (Double)value.get(j) - Math.log((Double)obs.get(j)) ;
                    value.set(j,newVal) ;
                }
            } else {
                throw new RuntimeException("Observation length must equal " +
                        "length of distribution") ;
            }
        } else if(distribDesc == DistribDesc.GAMMA){
            //An obs should be non-negative
            if((Double)obs.get(0) >= 0){
                double newVal = (Double)value.get(0) - Math.log((Double)obs.get(0)) ;
                value.set(0, newVal) ;
                newVal = (Double)value.get(1) - (Double)obs.get(0) ;
                value.set(1, newVal) ;
            } else {
                throw new RuntimeException("Observation must be non-negative") ;
            }
        } else if(distribDesc == DistribDesc.NORMAL){
            double newVal = (Double)value.get(0) - (Double)obs.get(0) ;
            value.set(0,newVal) ;
            newVal = (Double)value.get(1) - Math.pow((Double)obs.get(0),2) ;
            value.set(1,newVal) ;
        } else if(distribDesc == DistribDesc.MVNORMAL){
            Matrix newVal = (Matrix)value.get(0) ;
            if(newVal.getColumnDimension() == obs.size()){
                Matrix obsMatrix = new Matrix(obs.size(),1,0) ;
                for(int j = 0; j<newVal.getColumnDimension(); ++j){
                    double newEntry =newVal.get(j,0) - (Double)obs.get(j) ;
                    newVal.set(j, 0, newEntry) ;
                    obsMatrix.set(j,0, (Double)obs.get(j)) ;
                }
                value.set(0,newVal) ;
                newVal = (Matrix)value.get(1) ;
                newVal = newVal.minus(obsMatrix.times(obsMatrix.transpose())) ;
                value.set(1,newVal) ;
            } else{
                throw new RuntimeException("Observation length must is " +
                            "not the same length as the existing SS") ;
            }
        } else if(distribDesc == DistribDesc.WISHART){
            //An obs should be a matrix the same size as the second value ArrayList
            //element, and obs matrix should be symmetric and positive definite ;
            Matrix ss1 = (Matrix)value.get(1) ;
            Matrix obsMat = (Matrix)obs.get(0) ;
            if(ss1.getColumnDimension() == obsMat.getColumnDimension() &&
                    ss1.getRowDimension() == obsMat.getRowDimension() &&
                    new CholeskyDecomposition(obsMat).isSPD()){
                double newVal = (Double)value.get(0) - Math.log(obsMat.det()) ;
                value.set(0, newVal) ;
                Matrix newVal1 = (Matrix)value.get(1) ;
                newVal1 = newVal1.minus(obsMat) ;
                value.set(1,newVal1) ;
            } else{
                throw new RuntimeException("Obs must be symetric and postive " +
                        "definite and the dimension must agree with the current " +
                        "dimension of the value(1)") ;
            }
        }
        --n ;
    }
}
