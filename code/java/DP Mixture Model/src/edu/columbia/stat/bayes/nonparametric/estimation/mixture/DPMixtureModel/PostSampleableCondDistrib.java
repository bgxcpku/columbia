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
public  abstract class PostSampleableCondDistrib
        implements CondDistrib {
    public DistribDesc distribDesc ;
    abstract void resampleConjugate(SufficientStat ss, ArrayList priorParams) ;
}
