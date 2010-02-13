/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stats.ldadhpyplm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

/**
 *
 * @author davidpfau
 */
public class LDAGibbsEM implements Serializable {

    private Dictionary dict;
    private ArrayList<Document> docs;
    private ArrayList<Document> testDocs;
    private int numTopics;
    private double alpha = 1.0;
    private double beta = 1.0;
    private int[][] numWordInTopic;
    private int[] numAllWordsInTopic;
    private int[][] numTestWordInTopic;
    private int[] numAllTestWordsInTopic;
    private static Random rn = new Random(0);
    
    private double[] alphas;
    private double[] betas;

    public LDAGibbsEM(Dictionary dict, ArrayList<Document> docs, ArrayList<Document> testDocs, int numTopics) {
        this.dict = dict;
        this.docs = new ArrayList<Document>(docs.size());
        this.testDocs = new ArrayList<Document>(testDocs.size());
        for (Document d : docs) {
            this.docs.add(new Document(d.getTokens(),d.getDomains(),d.getContextLength(),numTopics));
        }
        for (Document d : testDocs) {
            this.testDocs.add(new Document(d.getTokens(),d.getContextLength(),numTopics));
        }
        this.numTopics = numTopics;
        numWordInTopic = new int[numTopics][dict.size()];
        numAllWordsInTopic = new int[numTopics];
        numTestWordInTopic = new int[numTopics][dict.size()];
        numAllTestWordsInTopic = new int[numTopics];

        for (Document d : this.docs) {
            for (int i = 0; i < d.size(); i++) {
                numWordInTopic[d.getDomain(i) - 1][d.getToken(i)]++;
                numAllWordsInTopic[d.getDomain(i) - 1]++;
            }
        }
        
        for (Document d : this.testDocs) {
            for (int i = 0; i < d.size(); i++) {
                numTestWordInTopic[d.getDomain(i) - 1][d.getToken(i)]++;
                numAllTestWordsInTopic[d.getDomain(i) -  1]++;
            }
        }
        
        alphas = new double[numTopics];
        betas  = new double[dict.size()];
        for (int i = 0; i < numTopics; i++) {
            alphas[i] = alpha/numTopics;
        }
        for (int i = 0; i < dict.size(); i++) {
            betas[i]  = beta/dict.size();
        }
        
        /*for (int i = 0; i < 10; i++) {
            sampleAlpha();
            sampleBeta();
        }*/
    }

    public LDAGibbsEM(int numDocs, int numWords, int numTopics, int wordsInDoc, double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
        this.numTopics = numTopics;
        this.docs = makeData(numDocs, numWords, numTopics, wordsInDoc, alpha, beta);
        this.dict = new Dictionary();
        for (int i = 0; i < numWords; i++) {
            dict.lookupOrAddIfNotFound(Integer.toString(i));
        }
        numWordInTopic = new int[numTopics][numWords];
        numAllWordsInTopic = new int[numTopics];

        for (Document d : docs) {
            for (int i = 0; i < wordsInDoc; i++) {
                numWordInTopic[d.getDomain(i) - 1][d.getToken(i)]++;
                numAllWordsInTopic[d.getDomain(i) - 1]++;
            }
        }
    }

    public LDAGibbsEM(int numDocs, int numTopics, int wordsInDoc) {
        this.alpha = 21.0;
        this.beta = 0.00001;
        this.numTopics = numTopics;
        this.docs = makeDegenerateData(numDocs, numTopics, wordsInDoc);
        this.dict = new Dictionary();
        for (int i = 0; i < numTopics; i++) {
            dict.lookupOrAddIfNotFound(Integer.toString(i));
        }
        numWordInTopic = new int[numTopics][numTopics];
        numAllWordsInTopic = new int[numTopics];

        for (Document d : docs) {
            for (int i = 0; i < wordsInDoc; i++) {
                numWordInTopic[d.getDomain(i) - 1][d.getToken(i)]++;
                numAllWordsInTopic[d.getDomain(i) - 1]++;
            }
        }
    }
    
    public LDAGibbsEM(int numDocs, int numTopics, int numWords, int wordsInDoc, double beta) {
        this.alpha = 100.0; //strongly bias towards uniform distribution of topics in document
        this.beta = beta;
        this.numTopics = numTopics;
        this.docs = makeDegenerateData2(numDocs, numTopics, numWords, wordsInDoc, beta);
        this.dict = new Dictionary();
        for (int i = 0; i < numWords; i++) {
            dict.lookupOrAddIfNotFound(Integer.toString(i));
        }
        numWordInTopic = new int[numTopics][numWords];
        numAllWordsInTopic = new int[numTopics];

        for (Document d : docs) {
            for (int i = 0; i < wordsInDoc; i++) {
                numWordInTopic[d.getDomain(i) - 1][d.getToken(i)]++;
                numAllWordsInTopic[d.getDomain(i) - 1]++;
            }
        }
    }
    
    public void sample(int n) {
        int[][][] numWordInTopicOverSamples = new int[n][dict.size()][numTopics];
        int[][][] numTopicInDocOverSamples  = new int[n][numTopics][docs.size()];
        int[][]   numInTopicOverSamples     = new int[n][numTopics];
        for (int i = 0; i < n; i++) {
            System.out.println("\nSample " + Integer.toString(i+1) + " out of " + n);
            System.out.println("Gibbs EM LDA log likelihood: " + sample());
            for (int k = 0; k < numTopics; k++) {
                for (int d = 0; d < docs.size(); d++) {
                    numTopicInDocOverSamples[i][k][d] = docs.get(d).getNumInDomain(k+1);
                }
                
                for (int j = 0; j < dict.size(); j++) {
                    numWordInTopicOverSamples[i][j][k] = numWordInTopic[k][j];
                    numInTopicOverSamples[i][k]        = numAllWordsInTopic[k];
                }
            }
        }
        
        GibbsEMAlpha(numTopicInDocOverSamples);
        GibbsEMBeta(numWordInTopicOverSamples, numInTopicOverSamples);
        System.out.println("Sampling alpha and beta complete.\n");
    }

    public ArrayList<BagOfWords> sampleAndSave(int i) {
        ArrayList<BagOfWords> allObservations = new ArrayList<BagOfWords>(numTopics);

        for (int t = 0; t < numTopics; t++) {
            allObservations.add(new BagOfWords(dict.size()));
        }

        for (int j = 0; j < i; j++) {
            System.out.println("Sweep " + Integer.toString(j + 1) + " of " + Integer.toString(i) + ":");
            sample();
            for (int t = 0; t < numTopics; t++) {
                allObservations.get(t).plus(new BagOfWords(numWordInTopic[t]));
            }
        }

        return allObservations;
    }

    public double sample() {
        for (Document d : docs) {
            //System.out.print("{");
            for (int i = 0; i < d.size(); i++) {
                sampleWord(d, i);
                if (i != d.size() - 1) {
                //System.out.print(", ");
                }
            }
        //System.out.println("}");
        //System.out.println("Reassigned topics to " + d.size() + " words.");
        }
        double ll = logLikelihood();
        //System.out.println("Log Likelihood: " + Double.toString(ll));
        //for (int i = 0; i < 5; i++) {
        //    System.out.print("Topic assignments for Document " + Integer.toString(i) + ": ");
        //    for (int j = 0; j < docs.get(i).size(); j++) {
        //       System.out.print(Integer.toString(docs.get(i).getDomain(j)) + " ");
        //    }
        //    System.out.print("\n");
        //}
        return ll;
    }

    public void sampleWord(Document d, int i) {
        int oldTopic = d.getDomain(i);
        int token = d.getToken(i);
        numWordInTopic[oldTopic - 1][token]--;
        numAllWordsInTopic[oldTopic - 1]--;
        d.setDomain(i, -1); // remove observation from count

        int[] numInTopicPerDoc = d.getNumInDomain();
        double[] score = new double[numTopics];
        double[] cumSum = new double[numTopics];
        for (int j = 0; j < numTopics; j++) {
            score[j] = (numInTopicPerDoc[j] + alphas[j]) * (numWordInTopic[j][token] + betas[token]) / (numAllWordsInTopic[j] + beta);
            if (j == 0) {
                cumSum[j] = score[j];
            } else {
                cumSum[j] = score[j] + cumSum[j - 1];
            }
        }

        int newTopic = 1;
        double sample = rn.nextDouble() * cumSum[numTopics - 1];
        for (int j = 0; j < numTopics; j++) {
            if (sample < cumSum[j]) {
                newTopic = j + 1;
                break;
            }
        }

        /*if(oldTopic == 1 && newTopic != 1) {
        System.out.println(oldTopic + "->" + newTopic + " " + sample/cumSum[numTopics - 1]);
        for (int j = 0; j < numTopics; j++) {
        System.out.print(" " + cumSum[j]/cumSum[numTopics-1]);
        }
        System.out.print("\n");
        }*/

        d.setDomain(i, newTopic);
        numWordInTopic[newTopic - 1][token]++;
        numAllWordsInTopic[newTopic - 1]++;
    //System.out.print(Integer.toString(newTopic));
    }
    
    public void sampleTestWord(Document d, int i) {
        sampleTestWord(d,i,d.getNumInDomain());
    }
    
    public void sampleTestWord(Document d, int i, int[] numInTopicPerDoc) {
        int oldTopic = d.getDomain(i);
        int token = d.getToken(i);
        numTestWordInTopic[oldTopic - 1][token]--;
        numAllTestWordsInTopic[oldTopic - 1]--;
        d.setDomain(i, -1); // remove observation from count

        double[] score = new double[numTopics];
        double[] cumSum = new double[numTopics];
        for (int j = 0; j < numTopics; j++) {
            score[j] = (numInTopicPerDoc[j] + alphas[j]) * (numWordInTopic[j][token] + numTestWordInTopic[j][token] + betas[token]) / (numAllWordsInTopic[j] + numAllTestWordsInTopic[j] + beta);
            if (j == 0) {
                cumSum[j] = score[j];
            } else {
                cumSum[j] = score[j] + cumSum[j - 1];
            }
        }

        int newTopic = 1;
        double sample = rn.nextDouble() * cumSum[numTopics - 1];
        for (int j = 0; j < numTopics; j++) {
            if (sample < cumSum[j]) {
                newTopic = j + 1;
                break;
            }
        }

        /*if(oldTopic == 1 && newTopic != 1) {
        System.out.println(oldTopic + "->" + newTopic + " " + sample/cumSum[numTopics - 1]);
        for (int j = 0; j < numTopics; j++) {
        System.out.print(" " + cumSum[j]/cumSum[numTopics-1]);
        }
        System.out.print("\n");
        }*/

        d.setDomain(i, newTopic);
        numTestWordInTopic[newTopic - 1][token]++;
        numAllTestWordsInTopic[newTopic - 1]++;
    //System.out.print(Integer.toString(newTopic));
    }
    
    public void sampleTestCorpus() {
        for (Document d : testDocs) {
            for (int i = 0; i < d.size(); i++) {
                sampleTestWord(d,i);
            }
        }
    }
    
    public void sampleTestCorpus(int n) {
        for (int i = 0; i < n; i++) {
            sampleTestCorpus();
        }
    }

    public DirichletDistrib getTopicDistrib(int i) {
        //Returns the posterior distribution of the ith topic given the current assignments    
        int w = dict.size();
        double[] params = new double[w];

        for (int j = 0; j < w; j++) {
            params[j] = numWordInTopic[i - 1][j] + beta;
        }

        return new DirichletDistrib(params);
    }

    public DirichletDistrib getDocumentDistrib(Document d) {
        double[] params = new double[numTopics];

        int[] numInTopic = d.getNumInDomain();
        for (int j = 0; j < numTopics; j++) {
            params[j] = numInTopic[j] + alpha;
        }

        return new DirichletDistrib(params);
    }

    public double getAlpha() {
        return alpha;
    }

    public double getBeta() {
        return beta;
    }

    
    // implements Hanna Wallach's Gibbs EM method for estimating the hyperparameters of 
    public void GibbsEMAlpha(int[][][] numTopicInDoc) {
        int numSample = numTopicInDoc.length;
        double eps = 0.01; // the threshold for ending iteration
        boolean going = true;
        int ctr = 0;
        while (going && ctr < 25) {
            going = false;
            for (int k = 0; k < numTopics; k++) {
                double numer = 0.0;
                double denom = 0.0;
                for (int s = 0; s < numSample; s++) {
                    for (int d = 0; d < docs.size(); d++) {
                        numer += digamma(numTopicInDoc[s][k][d] + alphas[k]) - digamma(alphas[k]);
                        denom += digamma(docs.get(d).size() + alpha) - digamma(alpha);
                    }
                }
                alphas[k] *= numer/denom;
                if (!going && Math.abs(1 - numer/denom) > eps) {
                    going = true;
                }
            }
            
            //the new alpha is the sum of all alphas
            alpha = 0.0;
            for (int k = 0; k < numTopics; k++) {
                alpha += alphas[k];
            }
            ctr++;
        }
    }
    
    public void GibbsEMBeta(int[][][] numWordInTopic, int[][] numInTopic) {
        int numSample = numWordInTopic.length;
        double eps = 0.01;
        boolean going = true;
        int ctr = 0;
        while (going && ctr < 25) {
            going = false;
            for (int j = 0; j < dict.size(); j++) {
                double numer = 0.0;
                double denom = 0.0;
                for (int s = 0; s < numSample; s++) {
                    for (int k = 0; k < numTopics; k++) {
                        numer += digamma(numWordInTopic[s][j][k] + betas[j]) - digamma(betas[j]);
                        denom += digamma(numInTopic[s][k] + beta) - digamma(beta);
                    }
                }
                betas[j] *= numer/denom;
                if (!going && Math.abs(1 - numer/denom) > eps) {
                    going = true;
                }
            }
            
            //the new alpha is the sum of all alphas
            beta = 0.0;
            for (int j = 0; j < dict.size(); j++) {
                beta += betas[j];
            }
            ctr++;
        }
    }
    
    // rough approximation to the digamma function
    public double digamma(double x) {
        return Math.log(x) - 1/(2*x) - 1/(12 * x*x) + 1/(120 * x*x*x*x) - 1/(252* x*x*x*x*x*x);
    }

    public double logLikelihood() {
        double logLike = 0.0;
        for (int i = 0; i < numTopics; i++) {
            for (int j = 0; j < dict.size(); j++) {
                logLike += Gamma.logGamma(numWordInTopic[i][j] + betas[j]);
                logLike -= Gamma.logGamma(betas[j]);
            }
            logLike -= Gamma.logGamma(numAllWordsInTopic[i] + beta);
        }
        logLike += numTopics * Gamma.logGamma(beta);

        return logLike;
    }
    
    // Estimates the marginal evidence of the data from posterior samples by taking the harmonic mean of the likelihood
    public double evidence(int n) {
        double harmonicMean = 0.0;
        for (int i = 0; i < n; i++) {
            sample();
            harmonicMean += 1/logLikelihood();
        }
        return n/harmonicMean;
    }
    
    //Log Likelihood of the test data given the labels and training data
    public double logLikelihoodTest() {
        double logLike = 0.0;
        for (int i = 0; i < numTopics; i++) {
            for (int j = 0; j < dict.size(); j++) {
                logLike += Gamma.logGamma(numTestWordInTopic[i][j] + numWordInTopic[i][j] + betas[j]);
                logLike -= Gamma.logGamma(numWordInTopic[i][j] + betas[j]); 
            }
            logLike += Gamma.logGamma(numAllWordsInTopic[i] + beta);
            logLike -= Gamma.logGamma(numAllWordsInTopic[i] + numAllTestWordsInTopic[i] + beta);
        }
        return logLike;
    }
    
    // Multiplies per-word probabilities rather than calculating everything with log gamma f'ns
    public double logLikelihoodTest2() {
        double logLike = 0.0;
        for (Document d : testDocs) {
            for (int i = 0; i < d.size(); i++) {
                logLike += Math.log(numWordInTopic[d.getDomain(i)-1][d.getToken(i)] + betas[d.getToken(i)]);
                logLike -= Math.log(numAllWordsInTopic[d.getDomain(i)-1] + beta);
                numWordInTopic[d.getDomain(i)-1][d.getToken(i)]++;
                numAllWordsInTopic[d.getDomain(i)-1]++;
            }
        }
        
        for (Document d : testDocs) {
            for (int i = 0; i < d.size(); i++) {
                numWordInTopic[d.getDomain(i)-1][d.getToken(i)]--;
                numAllWordsInTopic[d.getDomain(i)-1]--;
            }
        }
        return logLike;
    }
    
    public double bitsPerWord(int n, int m) {
        double[][] logLikelihoods = new double[n][m];
        for (int i = 0; i < n; i++) {
            sample(10);
            for (int j = 0; j < m; j++) {
                sampleTestCorpus(10);
                logLikelihoods[i][j] = logLikelihoodTest();
            }
        }
        return -avgmin(logLikelihoods)/(Math.log(2) * testCorpusSize());
    }
    
    // takes the minimum of each sub-array and returns the average of exp of those minima
    public double avgmin(double[][] input) {
        int max = 0; // index of the subarray with the greatest minimum
        int min[] = new int[input[0].length]; // array of the index of the minimum for each sub-array
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                if (input[i][j] < input[i][min[i]]) {
                    min[i] = j;
                }
            }
            if (input[i][min[i]] > input[max][min[max]]) {
                max = i;
            }
        }
        double expSum = 0.0;
        for (int i = 0; i < input.length; i++) {
            expSum += Math.exp(input[i][min[i]] - input[max][min[max]]);
        }
        return input[max][min[max]] + Math.log(expSum/input.length);
    }
    
        //implements left-to-right algorithm of Wallach '09 with r particles
    public double leftToRightBitsPerWord(int R) {
        double[] probOfWord = new double[testCorpusSize()];
        for (int r = 0; r < R; r++) {
            numTestWordInTopic = new int[numTopics][dict.size()];
            numAllTestWordsInTopic = new int[numTopics]; // initiate particle filter by removing all counts
            int ctr = 0;
            for (Document d : testDocs) {
                int[] numInTopicPerDoc = new int[numTopics]; // number of times that topic appears in this document
                for (int i = 0; i < d.size(); i++) {
                    for (int j = 0; j < i; j++) {
                        numInTopicPerDoc[d.getDomain(j)-1]--;
                        sampleTestWord(d,j,numInTopicPerDoc);
                        numInTopicPerDoc[d.getDomain(j)-1]++;
                    }
                    for (int t = 0; t < numTopics; t++) {
                        probOfWord[ctr] += ((numTestWordInTopic[t][d.getToken(i)] + numWordInTopic[t][d.getToken(i)] + betas[d.getToken(i)])
                                           /(numAllTestWordsInTopic[t] + numAllWordsInTopic[t] + beta))
                                          *((numInTopicPerDoc[t] + alphas[t])
                                           /(i + alpha));
                    }
                    numTestWordInTopic[d.getDomain(i)-1][d.getToken(i)]++;
                    numAllTestWordsInTopic[d.getDomain(i)-1]++; // since sampleTestWord() automatically holds out the current token, add it in initially
                    sampleTestWord(d,i,numInTopicPerDoc);
                    numInTopicPerDoc[d.getDomain(i)-1]++;
                    ctr++;
                }
            }
        }
        
        double logLike = -testCorpusSize()*Math.log(R); //factor introduced by taking the average, rather than sum, of probabilities of words
        for (int i = 0; i < testCorpusSize(); i++) {
            logLike += Math.log(probOfWord[i]);
        }
        return -logLike/(Math.log(2) * testCorpusSize());
    }

    public void scrambleTopics() {
        for (Document d : docs) {
            for (int i = 0; i < d.size(); i++) {
                int newTopic = Math.abs(rn.nextInt() % numTopics) + 1;
                int oldTopic = d.getDomain(i);
                d.setDomain(i, newTopic);
                numWordInTopic[oldTopic - 1][d.getToken(i)]--;
                numWordInTopic[newTopic - 1][d.getToken(i)]++;
                numAllWordsInTopic[oldTopic - 1]--;
                numAllWordsInTopic[newTopic - 1]++;
            }
        }
        System.out.println("Scrambled Log Likelihood: " + Double.toString(logLikelihood()));
    }

    public ArrayList<String> topTypesInTopic(int topic, int n) {
        ArrayList<String> typesInTopic = new ArrayList<String>(n);
        int[] inMax = new int[dict.size()];
        for (int i = 0; i < n; i++) {
            int maxIndex = 0;
            for (int j = 0; j < dict.size(); j++) {
                if (inMax[j] == 0) {
                    int currCount = numWordInTopic[topic - 1][j];
                    if (currCount > numWordInTopic[topic - 1][maxIndex]) {
                        maxIndex = j;
                    }
                }
            }
            inMax[maxIndex] = 1;
            typesInTopic.add(dict.lookup(maxIndex));
        }
        return typesInTopic;
    }

    public ArrayList<String> topTypesInObservation(BagOfWords obs, int n) {
        ArrayList<String> typesInObs = new ArrayList<String>(n);
        int[] inMax = new int[dict.size()];
        for (int i = 0; i < n; i++) {
            int maxIndex = 0;
            for (int j = 0; j < dict.size(); j++) {
                if (inMax[j] == 0) {
                    int currCount = obs.get(j);
                    if (currCount > obs.get(maxIndex)) {
                        maxIndex = j;
                    }
                }
            }
            inMax[maxIndex] = 1;
            typesInObs.add(dict.lookup(maxIndex));
        }
        return typesInObs;
    }

    public void printTopTypesInTopic(int n) {
        for (int i = 0; i < numTopics; i++) {
            System.out.print("Topic " + Integer.toString(i + 1) + ": ");
            ArrayList<String> topWordsInTopic = topTypesInTopic(i + 1, n);
            for (int j = 0; j < n; j++) {
                String type = topWordsInTopic.get(j);
                for (int k = 0; k < 6 - type.length(); k++) {
                    System.out.print(" ");
                }
                System.out.print(topWordsInTopic.get(j) + " ");
            }
            System.out.print("\n");
        }
    }

    public void printTopTypesInObservation(ArrayList<BagOfWords> obs, int n) {
        for (int i = 0; i < numTopics; i++) {
            System.out.print("Topic " + Integer.toString(i + 1) + ": ");
            ArrayList<String> topWordsInTopic = topTypesInObservation(obs.get(i), n);
            for (int j = 0; j < n; j++) {
                String type = topWordsInTopic.get(j);
                for (int k = 0; k < 6 - type.length(); k++) {
                    System.out.print(" ");
                }
                System.out.print(topWordsInTopic.get(j) + " ");
            }
            System.out.print("\n");
        }
    }
    
    //return the predictive probability of a word given a topic and all the test and training data
    public double predictiveProbability(int type, int topic) {
        return (numWordInTopic[topic][type] + numTestWordInTopic[topic][type] + beta)/
               (numAllWordsInTopic[topic] + numAllTestWordsInTopic[topic] + beta * dict.size());
    }
    
    public void printTopic(int topic, int n) {
        Type[] types = new Type[dict.size()];
        for (int i = 0; i < dict.size(); i++) {
            types[i] = new Type(dict.lookup(i),predictiveProbability(i,topic));
        }
        
        java.util.Arrays.sort(types,new CompareType());
        for (int i = 0; i < n; i++) {
            System.out.println(types[i].type + "\t\t\t" + types[i].probability);
        }
    }

    public static ArrayList<Document> makeData(int numDocs, int numWords, int numTopics, int wordsInDoc, double alpha, double beta) {
        ArrayList<Document> newDocs = new ArrayList<Document>(numDocs);

        double[] docPriorParams = new double[numTopics];
        for (int i = 0; i < numTopics; i++) {
            docPriorParams[i] = alpha;
        }
        DirichletDistrib docPrior = new DirichletDistrib(docPriorParams);

        ArrayList<DiscreteDistrib> documents = new ArrayList<DiscreteDistrib>(numDocs);
        for (int i = 0; i < numDocs; i++) {
            documents.add(docPrior.sample());
        }

        double[] topicPriorParams = new double[numWords];
        for (int i = 0; i < numWords; i++) {
            topicPriorParams[i] = beta;
        }
        DirichletDistrib topicPrior = new DirichletDistrib(topicPriorParams);

        ArrayList<DiscreteDistrib> topics = new ArrayList<DiscreteDistrib>(numTopics);
        for (int i = 0; i < numTopics; i++) {
            topics.add(topicPrior.sample());
        }

        ArrayList<ArrayList<Integer>> wordTopics = new ArrayList<ArrayList<Integer>>(numDocs);
        ArrayList<ArrayList<Integer>> wordTypes = new ArrayList<ArrayList<Integer>>(numDocs);
        for (int i = 0; i < numDocs; i++) {
            ArrayList<Integer> topicsInDoc = new ArrayList<Integer>(wordsInDoc);
            ArrayList<Integer> typesInDoc = new ArrayList<Integer>(wordsInDoc);
            int topic;
            int type;
            for (int j = 0; j < wordsInDoc; j++) {
                topic = documents.get(i).sampleIndex();
                topicsInDoc.add(topic + 1);
                type = topics.get(topic).sampleIndex();
                typesInDoc.add(type);
            }
            wordTopics.add(topicsInDoc);
            wordTypes.add(typesInDoc);
        }

        for (int i = 0; i < numDocs; i++) {
            newDocs.add(new Document(wordTypes.get(i), wordTopics.get(i), 2, numTopics));
        }

        System.out.println("Most Probable Words In Each Topic: ");
        for (int i = 0; i < numTopics; i++) {
            System.out.print("Topic " + Integer.toString(i + 1) + ": ");
            int[] inMax = new int[numWords];
            for (int j = 0; j < 10; j++) {
                int maxIndex = 0;
                for (int k = 0; k < numWords; k++) {
                    if (inMax[k] == 0) {
                        if (topics.get(i).parameter[k] > topics.get(i).parameter[maxIndex]) {
                            maxIndex = k;
                        }
                    }
                }
                inMax[maxIndex] = 1;
                String type = Integer.toString(maxIndex);
                for (int k = 0; k < 6 - type.length(); k++) {
                    System.out.print(" ");
                }
                System.out.print(type);
            }
            System.out.print("\n");
        }

        return newDocs;
    }

    public static ArrayList<Document> makeDegenerateData(int numDocs, int numTopics, int wordsInDoc) {
        ArrayList<Document> docs = new ArrayList<Document>(numDocs);
        for (int i = 0; i < numDocs; i++) {
            ArrayList<Integer> tokens = new ArrayList<Integer>(wordsInDoc);
            ArrayList<Integer> topics = new ArrayList<Integer>(wordsInDoc);
            for (int j = 0; j < wordsInDoc; j++) {
                int token = Math.abs((rn.nextInt() % numTopics));
                tokens.add(token);
                topics.add(token + 1);
            }
            docs.add(new Document(tokens, topics, 2, numTopics));
        }
        
        for (int i = 0; i < 5; i++) {
            System.out.print("Topic assignments for Document " + Integer.toString(i) + ": ");
            for (int j = 0; j < docs.get(i).size(); j++) {
                System.out.print(Integer.toString(docs.get(i).getDomain(j)) + " ");
            }
            System.out.print("\n");
        }
        
        return docs;
    }
    
    public static ArrayList<Document> makeDegenerateData2(int numDocs, int numTopics, int numWords, int wordsInDoc, double beta) {

        double[] topicPriorParams = new double[numWords];
        for (int i = 0; i < numWords; i++) {
            topicPriorParams[i] = beta;
        }
        DirichletDistrib topicPrior = new DirichletDistrib(topicPriorParams);
        DiscreteDistrib topic = topicPrior.sample();
            
        ArrayList<Document> docs = new ArrayList<Document>(numDocs);
        for (int i = 0; i < numDocs; i++) {
            ArrayList<Integer> tokens = new ArrayList<Integer>(wordsInDoc);
            ArrayList<Integer> topics = new ArrayList<Integer>(wordsInDoc);
            for (int j = 0; j < wordsInDoc; j++) {
                int token = topic.sampleIndex();
                tokens.add(token);
                topics.add(Math.abs(rn.nextInt() % numTopics) + 1);
            }
            docs.add(new Document(tokens, topics, 2, numTopics));
        }
        
        System.out.println("Most Probable Words In Topic: ");
        int[] inMax = new int[numWords];
        for (int j = 0; j < 20; j++) {
            int maxIndex = 0;
            for (int k = 0; k < numWords; k++) {
                if (inMax[k] == 0) {
                    if (topic.parameter[k] > topic.parameter[maxIndex]) {
                        maxIndex = k;
                    }
                }
            }
            inMax[maxIndex] = 1;
            String type = Integer.toString(maxIndex);
            for (int k = 0; k < 6 - type.length(); k++) {
                System.out.print(" ");
            }
            System.out.print(type + " - " + Double.toString(topic.parameter[maxIndex]));
            System.out.print("\n");
        }
        
        for (int i = 0; i < 5; i++) {
            System.out.print("Topic assignments for Document " + Integer.toString(i) + ": ");
            for (int j = 0; j < docs.get(i).size(); j++) {
                System.out.print(Integer.toString(docs.get(i).getDomain(j)) + " ");
            }
            System.out.print("\n");
        }
        
        return docs;
    }

    public ArrayList<Integer> getTopics(int i) {
        return docs.get(i).getDomains();
    }
    
    public int trainingCorpusSize() {
        int size = 0;
        for (Document d : docs) {
            size += d.size();
        }
        return size;
    }

    public int testCorpusSize() {
        int size = 0;
        for (Document d : testDocs) {
            size += d.size();
        }
        return size;
    }
    
    /*public double perWordPerplexity() {
        double logLoss = 0.0;
        double testCorpusSize = 0.0;
        for (Document d : testDocs) {
            testCorpusSize += d.size();
            for (int i = 0; i < d.size(); i++) {
                double averageProbability = 0.0;
                ArrayList<Integer> context = d.getContext(i);
                for (int j = 0; j < numTopics; j++) {
                    averageProbability += scoreClient.score(j + 1, context);
                }
                logLoss += Math.log(averageProbability/numTopics);
            }
        }
        return Math.exp(-logLoss/testCorpusSize);
    }
    
    public double bitsPerWord() {
        return Math.log(perWordPerplexity())/Math.log(2.0);
    }*/

    public static void main(String[] args) {
        /*double alpha = 1.0;
        double beta = 0.1;
        int numDocs = 1000;
        int numWords = 10000;
        int wordsInDoc = 1000;
        int numTopics = 10;
        LDAGibbsEM lda = new LDAGibbsEM(numDocs, numTopics, numWords, wordsInDoc, beta);
        System.out.println("True Log Likelihood: " + Double.toString(lda.logLikelihood()));*/
        /*
         for(int i = 0; i < 100; i++) {
            lda.sampleAlpha();
            lda.sampleBeta();
        }
        System.out.println("Log Likelihood with resampled Alpha and Beta: " + Double.toString(lda.logLikelihood()));
         */
        /*
         ArrayList<Document> docs = new ArrayList<Document>(2);
        Dictionary dict = new Dictionary();
        for (int i = 0; i < 2; i++) {
            ArrayList<Integer> tokens = new ArrayList<Integer>(10);
            ArrayList<Integer> topics = new ArrayList<Integer>(10);
            for (int j = 0; j < 10; j++) {
                tokens.add(i);
                topics.add(i + 1);
            }
            docs.add(new Document(tokens, topics, 2, numTopics));
        }

        dict.lookupOrAddIfNotFound("foo");
        dict.lookupOrAddIfNotFound("bar");

        LDA lda = new LDA(docs, dict, numTopics, alpha, beta);
        */

        //lda.printTopTypesInTopic(20);
        //lda.scrambleTopics();
        //lda.printTopTypesInTopic(10);
        //lda.sample(150);
        //ArrayList<BagOfWords> observation = lda.sampleAndSave(50);
        //lda.printTopTypesInObservation(observation, 10);
        //lda.printTopTypesInTopic(20);
        /*for (int i = 0; i < 2; i++) {
            ArrayList<Integer> topics = lda.getTopics(i);
            System.out.print("Topic assignments for document " + Integer.toString(i + 1) + ": ");
            for (int j = 0; j < 10; j++) {
                System.out.print(Integer.toString(topics.get(j)) + " ");
            }
            System.out.print("\n");
        }*/
        Random rn = new Random(0);
        Dictionary dict = new Dictionary();
        dict.lookupOrAddIfNotFound("blah");
        dict.lookupOrAddIfNotFound("bluh");
        dict.lookupOrAddIfNotFound("bleh");
        dict.lookupOrAddIfNotFound("blih");
        ArrayList<Document> docs = new ArrayList<Document>(10);
        ArrayList<Document> testDocs = new ArrayList<Document>(10);
        for(int i = 0; i < 10; i++) {
            ArrayList<Integer> words  = new ArrayList<Integer>();
            ArrayList<Integer> topics = new ArrayList<Integer>();
            for (int j = 0; j < 100; j++) {
                int topic = Math.abs(rn.nextInt())%2;
                int word = topic == 0 ? Math.abs(rn.nextInt())%2 : (Math.abs(rn.nextInt())%2)+2 ;
                words.add(word);
                topics.add(topic+1);
            }
            docs.add(new Document(words,topics,2,2));
            testDocs.add(new Document(words,topics,2,2));
        }
        LDAGibbsEM bar = new LDAGibbsEM(dict, docs, testDocs, 2);
        System.out.println(bar.leftToRightBitsPerWord(10));
    }
    
    private class Type {
        public String type;
        public double probability;
        public Type(String type, double prob) {
            this.type = type;
            probability = prob;
        }
    }
    
    private class CompareType implements Comparator<Type> {
        public int compare(Type a, Type b) {
            if (a.probability > b.probability) {
                return -1;
            } else if (a.probability == b.probability) {
                return 0;
            }
            return 1;
        }
    }
}
