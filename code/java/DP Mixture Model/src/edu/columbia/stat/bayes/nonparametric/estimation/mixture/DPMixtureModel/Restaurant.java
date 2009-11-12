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

    //Constructor to initialize each obs into own population
    /*public Restaurant(ArrayList<BagOfWordsObservation> dataset, DirichletDistrib baseMeasure) {
    for (int j = 0; j < dataset.size(); ++j) {
    put(baseMeasure.sample(), new Population(j, dataset.get(j)));
    }
    this.baseMeasure = baseMeasure;
    this.dataset = dataset;
    }*/
    
    public Restaurant(ArrayList<BagOfWordsObservation> dataset, DirichletDistrib baseMeasure) {
        for (int obs = 0; obs < dataset.size(); obs++) {
            DiscreteDistrib tableToAddTo = null;
            double maxLogWeight = Double.NEGATIVE_INFINITY;
            for (DiscreteDistrib t : keySet()) {
                if (t.getLogLikelihood(dataset.get(obs)) + Math.log(get(t).size()) > maxLogWeight) {
                    maxLogWeight = Math.log(get(t).size()) + t.getLogLikelihood(dataset.get(obs));
                    tableToAddTo = t;
                }
            }

            double newGrpLogWeight = Math.log(alpha);
            int thisObsWeight = 0;
            int baseMeasureWeight = 0;
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

            if(newGrpLogWeight > maxLogWeight){
                double[] newParam = baseMeasure.parameter;
                for (int w = 0; w < baseMeasure.parameter.length; ++w) {
                    newParam[w] += dataset.get(obs).value[w];
                }
                put(new DirichletDistrib(newParam).sample(), new Population(obs, dataset.get(obs)));
            } else {
                get(tableToAddTo).add(obs, dataset.get(obs)) ;
            }
        }
        this.baseMeasure = baseMeasure;
        this.dataset = dataset;
    }

    public Restaurant(ArrayList<BagOfWordsObservation> dataset, double baseMeasureWeight, int numberTables) {
        baseMeasure = new DirichletDistrib(baseMeasureWeight, dataset.get(0).value.length);
        this.dataset = dataset;
        Population[] pops = new Population[numberTables];
        for (int k = 0; k < numberTables; ++k) {
            pops[k] = new Population(k, dataset.get(k));
        }
        for (int j = numberTables; j < dataset.size(); ++j) {
            int i = j % numberTables;
            pops[i].add(j, dataset.get(j));
        }
        for (int k = 0; k < numberTables; ++k) {
            put(baseMeasure.sample(), pops[k]);
        }
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
        for (int j = 0; j < dataset.size(); ++j) {
            //remove element from table
            get(tableList[j]).remove(j, dataset.get(j));

            //initialize arraylist and array for log weights and the order in
            //which the iterator gives back the hashmap key values
            ArrayList<DiscreteDistrib> tableOrder = new ArrayList(keySet().size());
            double[] logWeights = new double[keySet().size() + 1];

            //set weight values for all active tables
            int i = 0;
            for (DiscreteDistrib t : keySet()) {
                if (get(t).size() != 0) {
                    logWeights[i] = Math.log(get(t).size()) +
                            t.getLogLikelihood(dataset.get(j));
                } else {
                    logWeights[i] = Double.NEGATIVE_INFINITY;
                }
                tableOrder.add(t);
                ++i;
            }

            //find weight of last element which is integrated distn against
            //the base measure
            logWeights[keySet().size()] = Math.log(alpha);
            int thisObsWeight = 0;
            int baseMeasureWeight = 0;
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
            for (int w = 1; w < logWeights.length; ++w) {
                weightScale = Math.max(weightScale, logWeights[w]);
            }

            double totWeight = 0;
            for (int w = 0; w < logWeights.length; ++w) {
                if (logWeights[w] != Double.NEGATIVE_INFINITY) {
                    logWeights[w] -= weightScale;
                }
                totWeight += Math.exp(logWeights[w]);
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
                double[] newParam = baseMeasure.parameter;
                for (int w = 0; w < baseMeasure.parameter.length; ++w) {
                    newParam[w] += dataset.get(j).value[w];
                }
                put(new DirichletDistrib(newParam).sample(), new Population(j, dataset.get(j)));
            }

            //delete table if need be
            if (get(tableList[j]).size() == 0) {
                remove(tableList[j]);
            }
        }
    }

    public void reSampleTables() {
        DiscreteDistrib[] oldKeySet = new DiscreteDistrib[size()];
        DiscreteDistrib[] newKeySet = new DiscreteDistrib[size()];

        //go through key set and generate new distributions for each table
        int i = 0;
        for (DiscreteDistrib t : keySet()) {
            double[] newParam = baseMeasure.parameter;
            for (int j = 0; j < baseMeasure.parameter.length; j++) {
                newParam[j] += get(t).sufficientStatisic.value[j];
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

    public void reSampleAlpha(double a, double b) { //prior parameters on gamma distrib for alpha
        //first sample aux parameter eta
        double[] betaParams = {alpha + 1, dataset.size()};
        double eta = new DirichletDistrib(betaParams).sample().parameter[0];

        //calculate mixture levels
        double mix = (a + size() - 1) /
                (a + size() - 1 + dataset.size() * (b - Math.log(eta)));

        //then choose mixture component
        if (Math.random() < mix) {
            alpha = new GammaDistrib(a + size(), b - Math.log(eta)).sample();
        } else {
            alpha = new GammaDistrib(a + size() - 1, b - Math.log(eta)).sample();
        }
    }
}
