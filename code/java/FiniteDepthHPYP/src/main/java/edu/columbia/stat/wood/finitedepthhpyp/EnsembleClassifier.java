/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.finitedepthhpyp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author nicholasbartlett
 */
public class EnsembleClassifier {
    private int[][] representations;
    private ArrayList<HPYTree> binaryClassifiers;
    
    private static Random RNG;
    
    public EnsembleClassifier(int alphabetSize, int ensembleSize, int seed){
        RNG = new Random(seed);

        representations = new int[alphabetSize][ensembleSize];
        binaryClassifiers = new ArrayList<HPYTree>(ensembleSize);
        double rawRandom;
        for(int i = 0; i<ensembleSize;i++){
            binaryClassifiers.add(new HPYTree(2,3,seed));
        }
        for(int i = 0; i<alphabetSize; i++){
            for(int j = 0; j<ensembleSize; j++){
                rawRandom = RNG.nextDouble();
                representations[i][j] = (rawRandom<.5)?0:1;
            }
        }
    }

    public double continueSequence(int obs){
        double[] pRep = new double[binaryClassifiers.size()];

        int actualObs;
        int ind = 0;
        for(HPYTree classifier : binaryClassifiers){
            actualObs = representations[obs][ind];
            if(actualObs == 1){
                pRep[ind] = Math.exp(classifier.continueSequence(actualObs,obs));
            } else {
                pRep[ind] = 1-Math.exp(classifier.continueSequence(actualObs,obs));
            }
            ind++;
        }
        
        return this.getLogPredictiveProb(pRep, obs);
    }

    private double getLogPredictiveProb(double[] pRep, int symbol){
        double[] predictiveDistribution = new double[representations.length];
        double cuSum = 0.0;
        for(int i = 0; i<predictiveDistribution.length; i++){
            predictiveDistribution[i] = this.getLogLik(pRep, i);
            cuSum += Math.exp(predictiveDistribution[i]);
        }

        double cuSum1 = 0.0;
        for(int i = 0; i<predictiveDistribution.length; i++){
            predictiveDistribution[i] = Math.exp(predictiveDistribution[i]);
            predictiveDistribution[i] /= cuSum;
            cuSum1 += predictiveDistribution[i];
        }

        return Math.log(predictiveDistribution[symbol]);
    }

    private double getLogLik(double[] pRep, int symbol){
        double logLik = 0.0;

        for(int i = 0; i<pRep.length; i++){
            if(representations[symbol][i] == 1){
                logLik += Math.log(pRep[i]);
            } else {
                logLik += Math.log(1-pRep[i]);
            }
        }

        return logLik;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException{
        String path = "/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/";
        String file = "AliceInWonderland.txt";
        BufferedInputStream bis = null;
        EnsembleClassifier ec = new EnsembleClassifier(256,10,0);
        try{
            bis = new BufferedInputStream(new FileInputStream(path + file));
            int fileLength = bis.available();
            int b;
            double logLik = 0.0;
            int ind = 0;
            while((b = bis.read())>-1 && ind++ < 1000){
                logLik += ec.continueSequence(b);
            }
            System.out.println(-logLik/Math.log(2)/1000);
        } finally {
            if(bis != null){
                bis.close();
            }
        }
    }
}
