/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;
import java.util.HashMap ;
import java.util.HashSet ;

/**
 *
 * @author nicholasbartlett
 */
public class TopLevelRestaurant {
    public HashMap<DiscreteDistrib,SetableDouble> betaMap;
    public HashMap<DiscreteDistrib,HashSet<TableLabel>> tableMap ;
    public HashMap<DiscreteDistrib,HashSet<Token>> phiMap ;

    public SetableDouble gamma;
    //constructor

    public double sample() {

         int thisObsWeight = 0;
            double baseMeasureWeight = 0;
            for (int k = 0; k < baseMeasure.parameter.length; ++k) {
                logWeights[keySet().size()] +=
                        GammaDistrib.lnGammaFunction(baseMeasure.parameter[k] + dataset.get(j).value[k]);
                logWeights[keySet().size()] -=
                        GammaDistrib.lnGammaFunction(baseMeasure.parameter[k]);

                thisObsWeight += dataset.get(j).value[k];
                baseMeasureWeight += baseMeasure.parameter[k];
            }
            logWeights[keySet().size()] -= GammaDistrib.lnGammaFunction(baseMeasureWeight + thisObsWeight);
            logWeights[keySet().size()] += GammaDistrib.lnGammaFunction(baseMeasureWeight);


        return 0.0;
    }

    //method to return prob of sitting at new table and potential phi at which
    //you sat
    public Pair<Double,DiscreteDistrib> getMeasure(int word){return null;}

    //method to return map of active discrete distributions to current beta value
    public HashMap<DiscreteDistrib,Double> getBetaMap(){
        return betaMap ;
    }

    //method to return map of active discrete distributions to tables of tabel
    //labels
    public HashMap<DiscreteDistrib,HashSet<TableLabel>> getTableMap() {
        return tableMap ;
    }
    
}
