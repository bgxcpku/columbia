/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author nicholasbartlett
 */
public class ParticleFilter extends ArrayList<Particle> {

    public ArrayList<BagOfWordsObservation> dataset;
    private DirichletDistrib baseMeasure;
    public double alpha = 1;
    public double[] weights;

    public ParticleFilter(int numberOfParticles, BagOfWordsObservation firstObs, DirichletDistrib baseMeasure, double alpha) {
        super(numberOfParticles);
        add(new Particle(0));

        dataset = new ArrayList<BagOfWordsObservation>();
        dataset.add(firstObs);

        this.baseMeasure = baseMeasure;
        this.alpha = alpha;
        this.weights = new double[numberOfParticles];
        weights[0] = 1.0;
    }

    public void updateParticleFilter(BagOfWordsObservation obs) {
        //add new obs to the dataset
        dataset.add(obs);

        //initialize double array for weights of proposals and get keys in the
        //current keySet 
        double[][] proposalWeights = new double[size()][];
        int index = 0;
        for (Particle particle : this) {
            proposalWeights[index++] = new double[particle.size() + 1];
        }

        //loop through current particles
        double maxWeight = Double.NEGATIVE_INFINITY;
        index = 0;
        for (Particle particle : this) {
            //loop through each table represented in the particle
            for (int table = 0; table < particle.size(); table++) {
                //for each table, will need a sufficient statistic, which is not
                //available from the table so that I'm not putting the data into
                //memory a bunch of different times
                BagOfWordsObservation tableObs = new BagOfWordsObservation(obs.value.length);
                for (Integer cust : particle.get(table)) {
                    tableObs.plus(dataset.get(cust));
                }

                //now calcluate log weight for seating the new obs at this table
                //in this particular particle
                double logWeight = Math.log(particle.get(table).size());
                int thisObsWeight = 0;
                double baseMeasureWeight = 0;

                for (int k = 0; k < baseMeasure.parameter.length; ++k) {
                    logWeight +=
                            GammaDistrib.lnGammaFunction(baseMeasure.parameter[k] + tableObs.value[k] + obs.value[k]);
                    logWeight -=
                            GammaDistrib.lnGammaFunction(baseMeasure.parameter[k] + tableObs.value[k]);

                    thisObsWeight += obs.value[k];
                    baseMeasureWeight += baseMeasure.parameter[k] + tableObs.value[k];
                }
                logWeight -= GammaDistrib.lnGammaFunction(baseMeasureWeight + thisObsWeight);
                logWeight += GammaDistrib.lnGammaFunction(baseMeasureWeight);

                //multiply weight by existing weight on particle
                proposalWeights[index][table] = logWeight + Math.log(weights[index]);

                if (proposalWeights[index][table] > maxWeight) {
                    maxWeight = proposalWeights[index][table];
                }
            }

            //now add weight for new table in this particle
            double logWeight = Math.log(alpha);
            int thisObsWeight = 0;
            double baseMeasureWeight = 0;

            for (int k = 0; k < baseMeasure.parameter.length; ++k) {
                logWeight +=
                        GammaDistrib.lnGammaFunction(baseMeasure.parameter[k] + obs.value[k]);
                logWeight -=
                        GammaDistrib.lnGammaFunction(baseMeasure.parameter[k]);

                thisObsWeight += obs.value[k];
                baseMeasureWeight += baseMeasure.parameter[k];
            }
            logWeight -= GammaDistrib.lnGammaFunction(baseMeasureWeight + thisObsWeight);
            logWeight += GammaDistrib.lnGammaFunction(baseMeasureWeight);

            //multiply weight by existing weight on particle
            proposalWeights[index][particle.size()] = logWeight + Math.log(weights[index]);

            if (proposalWeights[index][particle.size()] > maxWeight) {
                maxWeight = proposalWeights[index][particle.size()];
            }
            index++;
        }

        //scale weight and sum
        double totWeight = 0;
        int doubleArraySize = 0;
        index = 0;
        for (Particle particle : this) {
            for (int table = 0; table < particle.size() + 1; table++) {
                proposalWeights[index][table] -= maxWeight;
                totWeight += Math.exp(proposalWeights[index][table]);
                doubleArraySize++;
            }
            index++;
        }

        //normalize weights
        double[] proposalWeightsArray = new double[doubleArraySize];
        index = 0;
        doubleArraySize = 0;
        for (Particle particle : this) {
            for (int table = 0; table < particle.size() + 1; table++) {
                proposalWeights[index][table] = Math.exp(proposalWeights[index][table]) / totWeight;
                proposalWeightsArray[doubleArraySize] = proposalWeights[index][table];
                doubleArraySize++;
            }
            index++;
        }

        

        //if the number of particles allowed is not yet hit then can add all
        //proposed particles
        if (doubleArraySize <= weights.length) {
            index = 0;
            int totalIndex = 0;
            int currentSize = size();
            for (int pIndex = 0; pIndex<currentSize; pIndex++) {
                for (int table = 0; table < get(pIndex).size() + 1; table++) {
                    add(get(pIndex).copy());
                    if (table < get(pIndex).size()) {
                        get(totalIndex + currentSize).get(table).add(dataset.size() - 1);
                    } else {
                        HashSet<Integer> tableToAdd = new HashSet<Integer>(2);
                        tableToAdd.add(dataset.size() - 1);
                        get(totalIndex + currentSize).add(tableToAdd);
                    }
                    weights[totalIndex++] = proposalWeights[index][table];
                }
                index++;
            }
            this.removeRange(0, currentSize);
            return;
        }

        //sort array
        Arrays.sort(proposalWeightsArray);

        //find the optimal cut off level such that stratified sampling on the rest
        //of the weights will give only one value at most of each particle
        //start with the large weights
        /*double leftOverWeight = sumWeight;
        int l = proposalWeightsArray.length;
        double cutoff = 0;
        double k = 0 ;

        for (int j = 0; j < l; j++) {
            if (leftOverWeight / (weights.length - j) >= proposalWeightsArray[l - j - 1]) {
                cutoff = proposalWeightsArray[l - j - 1];

                k = leftOverWeight / (weights.length - j);
                break;
            }
            leftOverWeight -= proposalWeightsArray[l - j - 1];
        }*/

        //find the optimal cut off level such that stratified sampling on the rest
        //of the weights will give only one value at most of each particle
        //start with the large weights

        double leftOverWeight = 0.0 ;
        int  l = proposalWeightsArray.length;
        double cutoff = 0 ;
        double k = 0 ;
        for(int j=0; j<l-weights.length;j++){
            leftOverWeight += proposalWeightsArray[j] ;
        }
        
        for(int j = 0; j <weights.length ;j++){
            leftOverWeight += proposalWeightsArray[j+l-weights.length] ;
            if(leftOverWeight/(j+1) < proposalWeightsArray[j+l-weights.length]){
                leftOverWeight -= proposalWeightsArray[j+l-weights.length] ;
                cutoff = proposalWeightsArray[j+l-weights.length-1] ;
                k = leftOverWeight/j ;
                break ;
            }
        }

        //go through entire list of weights and sample accoringly
        int totalIndex = 0;
        int currentSize = size();
        double u = Math.random() * k;
        for (int pIndex = 0; pIndex<currentSize; pIndex++) {
            for (int table = 0; table < get(pIndex).size() + 1; table++) {
                if (proposalWeights[pIndex][table] > cutoff) {
                    add(get(pIndex).copy());
                    if (table < get(pIndex).size()) {
                        get(totalIndex + currentSize).get(table).add(dataset.size() - 1);
                    } else {
                        HashSet<Integer> tableToAdd = new HashSet<Integer>(2);
                        tableToAdd.add(dataset.size() - 1);
                        get(totalIndex + currentSize).add(tableToAdd);
                    }
                    weights[totalIndex++] = proposalWeights[pIndex][table];
                } else {
                    u -= proposalWeights[pIndex][table];
                    if (u < 0) {
                        add(get(pIndex).copy());
                        if (table < get(pIndex).size()) {
                            get(totalIndex + currentSize).get(table).add(dataset.size() - 1);
                        } else {
                            HashSet<Integer> tableToAdd = new HashSet<Integer>(2);
                            tableToAdd.add(dataset.size() - 1);
                            get(totalIndex + currentSize).add(tableToAdd);
                        }
                        weights[totalIndex++] = proposalWeights[pIndex][table];
                        u += k;
                    }
                }
            }
        }
        this.removeRange(0, currentSize);
    }

    public void printParticles(int m) {
        int nmbrParticles = size() ;
        System.out.println("THERE ARE CURRENTLY " + nmbrParticles + " PARTICLES") ;
        m = Math.min(size(), m) ;
        //copy and sort weights
        double[] sortedWeights = new double[size()] ;
        for(int j = 0; j<size(); j++){
            sortedWeights[j] = weights[j] ;
        }

        Arrays.sort(sortedWeights) ;

        for(int j = 0 ; j < m ; j++){
            for(int i=0; i<size(); i++){
                if(weights[i] == sortedWeights[size() - j - 1]){
                    System.out.print("(Weight = " + weights[i] + ")") ;
                    for (int table = 0; table < get(i).size(); table++) {
                        System.out.print("{") ;
                        for (Integer cust : get(i).get(table)) {
                            System.out.print(cust + ",");
                        }
                        System.out.print("}") ;
                    }
                }
            }
            System.out.println() ;
        }
    }
        
    public void printParticles() {
        printParticles(size());
    }
}
