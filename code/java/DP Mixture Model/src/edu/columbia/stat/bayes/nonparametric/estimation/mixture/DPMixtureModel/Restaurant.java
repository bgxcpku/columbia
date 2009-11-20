/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */
public class Restaurant extends HashMap<DiscreteDistrib, Population> {

    private DirichletDistrib baseMeasure;
    public double alpha = 1;
    public ArrayList<BagOfWordsObservation> dataset;
    public double aa ; //prior parameters on alpha parameter
    public double bb ;

    public Restaurant(ArrayList<BagOfWordsObservation> dataset, DirichletDistrib baseMeasure, double aa, double bb) {
    //people are seated according to a pseudo particle filter which always chooses
    //the seat with the highest probability.

        //temp arraylist to hold table populations before adding.
        ArrayList<Population> pops = new ArrayList((int) Math.ceil(Math.log(dataset.size())));

        for (int obs = 0; obs < dataset.size(); obs++) {
            int popIndex = 0;
            double maxLogWeight = Double.NEGATIVE_INFINITY;
            for (int pp = 0; pp < pops.size(); pp++) {
                Population p = pops.get(pp);
                double popLogWeight = Math.log(p.size());
                int thisObsWeight = 0;
                double baseMeasureWeight = 0;
                for (int k = 0; k < baseMeasure.parameter.length; k++) {
                    popLogWeight +=
                            GammaDistrib.lnGammaFunction(baseMeasure.parameter[k] + p.sufficientStatisic.value[k] + dataset.get(obs).value[k]);
                    popLogWeight -=
                            GammaDistrib.lnGammaFunction(baseMeasure.parameter[k] + p.sufficientStatisic.value[k]);

                    thisObsWeight += dataset.get(obs).value[k];
                    baseMeasureWeight += baseMeasure.parameter[k] + p.sufficientStatisic.value[k];
                }
                popLogWeight -= GammaDistrib.lnGammaFunction(baseMeasureWeight + thisObsWeight);
                popLogWeight += GammaDistrib.lnGammaFunction(baseMeasureWeight);
                if (popLogWeight > maxLogWeight) {
                    maxLogWeight = popLogWeight;
                    popIndex = pp;
                }
            }

            double newGrpLogWeight = Math.log(alpha);
            int thisObsWeight = 0;
            double baseMeasureWeight = 0;
            for (int k = 0; k < baseMeasure.parameter.length; ++k) {
                newGrpLogWeight +=
                        GammaDistrib.lnGammaFunction(baseMeasure.parameter[k] + dataset.get(obs).value[k]);
                newGrpLogWeight -=
                        GammaDistrib.lnGammaFunction(baseMeasure.parameter[k]);

                thisObsWeight += dataset.get(obs).value[k];
                baseMeasureWeight += baseMeasure.parameter[k];
            }
            newGrpLogWeight -= GammaDistrib.lnGammaFunction(baseMeasureWeight + thisObsWeight);
            newGrpLogWeight += GammaDistrib.lnGammaFunction(baseMeasureWeight);

            if (newGrpLogWeight > maxLogWeight) {
                pops.add(new Population(obs, dataset.get(obs)));
            } else {
                pops.get(popIndex).add(obs, dataset.get(obs));
            }
        }

        //now add all the populations ;

        for (Population p : pops) {
            double[] newParam = new double[baseMeasure.parameter.length];
            for (int w = 0; w < baseMeasure.parameter.length; w++) {
                newParam[w] = p.sufficientStatisic.value[w] + baseMeasure.parameter[w];
            }
            put(new DirichletDistrib(newParam).sample(), p);
        }

        this.baseMeasure = baseMeasure;
        this.dataset = dataset;
        this.aa = aa ;
        this.bb = bb ;
    }

    //Method to reseat restaurant
    public void reSeatRestaurant() {
        //first, invert the hashtable so as to know which table people are
        //sitting at
        DiscreteDistrib[] tableList = new DiscreteDistrib[dataset.size()];
        for (DiscreteDistrib t : keySet()) {
            for (Integer obs : (Integer[]) get(t).toArray(new Integer[0])) {
                tableList[obs] = t;
            }
        }

        //now reseat each person iteratively in order
        for (int j = 0; j < dataset.size(); j++) {
            //remove element from table
            get(tableList[j]).remove(j, dataset.get(j));

            //delete table if need be
            if (get(tableList[j]).size() == 0) {
                remove(tableList[j]);
            }

            //initialize arraylist and array for log weights and the order in
            //which the iterator gives back the hashmap key values
            ArrayList<DiscreteDistrib> tableOrder = new ArrayList(keySet().size());
            double[] logWeights = new double[keySet().size() + 1];

            //set weight values for all active tables
            int i = 0;
            for (DiscreteDistrib t : keySet()) {
                logWeights[i++] = Math.log(get(t).size()) +
                        t.getLogLikelihood(dataset.get(j));
                tableOrder.add(t);
            }

            //find weight of last element which is integrated distn against
            //the base measure
            logWeights[keySet().size()] = Math.log(alpha);
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

            //re-scale weights to make numerically possible
            double weightScale = logWeights[0];
            for (int w = 1; w < logWeights.length; w++) {
                if (logWeights[w] > weightScale) {
                    weightScale = logWeights[w];
                }
            }

            double totWeight = 0;
            for (int w = 0; w < logWeights.length; ++w) {
                if (logWeights[w] != Double.NEGATIVE_INFINITY) {
                    logWeights[w] -= weightScale;
                    totWeight += Math.exp(logWeights[w]);
                }
            }

            //Sample new cluster indicator variable
            int z = 0;
            double cumSum = 0;
            double rawSample = Math.random();
            for (int w = 0; w < logWeights.length; ++w) {
                cumSum += Math.exp(logWeights[w]) / totWeight;
                if (rawSample < cumSum) {
                    z = w;
                    break;
                }
            }

            //add obs to correct population
            if (z < size()) {
                get(tableOrder.get(z)).add(j, dataset.get(j));
            } else {
                double[] newParam = new double[baseMeasure.parameter.length];
                for (int w = 0; w < baseMeasure.parameter.length; w++) {
                    newParam[w] = dataset.get(j).value[w] + baseMeasure.parameter[w];
                }
                put(new DirichletDistrib(newParam).sample(), new Population(j, dataset.get(j)));
                System.out.println("MADE NEW TABLE");
            }
        }
    }

    public void reSampleTables() {
        DiscreteDistrib[] oldKeySet = new DiscreteDistrib[size()];
        DiscreteDistrib[] newKeySet = new DiscreteDistrib[size()];

        //go through key set and generate new distributions for each table
        int i = 0;
        for (DiscreteDistrib t : keySet()) {
            double[] newParam = new double[baseMeasure.parameter.length];
            for (int j = 0; j < baseMeasure.parameter.length; j++) {
                newParam[j] = get(t).sufficientStatisic.value[j] + baseMeasure.parameter[j];
            }
            newKeySet[i] = new DirichletDistrib(newParam).sample();
            oldKeySet[i] = t;
            i++;
        }

        //actually add new table parameters to tables by making new tables with
        //old populations and getting rid of old tables
        for (int j = 0; j < oldKeySet.length; j++) {
            put(newKeySet[j], get(oldKeySet[j]));
            remove(oldKeySet[j]);
        }
    }

    public void reSampleAlpha() { //prior parameters on gamma distrib for alpha
        //first sample aux parameter eta
        double[] betaParams = {alpha + 1, dataset.size()};
        double eta = new DirichletDistrib(betaParams).sample().parameter[0];

        //calculate mixture levels
        double mix = (aa + size() - 1) /
                (aa + size() - 1 + dataset.size() * (bb - Math.log(eta)));

        //then choose mixture component
        if (Math.random() < mix) {
            alpha = new GammaDistrib(aa + size(), bb - Math.log(eta)).sample();
        } else {
            alpha = new GammaDistrib(aa + size() - 1, bb - Math.log(eta)).sample();
        }
    }

    //method to return log likelihood of current state of restaurant
    public double getLogLikelihood(){
        double ll = 0 ;
        for(DiscreteDistrib table:keySet()){
            ll += table.getLogLikelihood(get(table).sufficientStatisic) ;
            ll += baseMeasure.getLogLikelihood(table) ;
            for(int j = 0; j < get(table).size() ; j++){
                if(j == 0) {
                    ll += Math.log(alpha) ;
                } else {
                    ll +=Math.log(j) ;
                }
            }
        }
        ll += new GammaDistrib(aa, bb).getLogLikelihood(alpha) ;
        return ll ;
    }
}
