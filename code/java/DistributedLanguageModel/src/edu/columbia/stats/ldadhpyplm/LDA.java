/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stats.ldadhpyplm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import com.aliasi.symbol.*;

/**
 *
 * @author davidpfau
 */
public class LDA implements Serializable {

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
    private static final long serialVersionUID = 2;

    public LDA(Dictionary dict, ArrayList<Document> docs, ArrayList<Document> testDocs, int numTopics) {
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
        
        for (int i = 0; i < 10; i++) {
            sampleAlpha();
            sampleBeta();
        }
    }

    public LDA(int numDocs, int numWords, int numTopics, int wordsInDoc, double alpha, double beta) {
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

    public LDA(int numDocs, int numTopics, int wordsInDoc) {
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
    
    public LDA(int numDocs, int numTopics, int numWords, int wordsInDoc, double beta) {
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
        for (int i = 0; i < n; i++) {
            System.out.println("\nSample " + Integer.toString(i+1) + " out of " + n);
            //System.out.println("Sweep " + Integer.toString(j + 1) + " of " + Integer.toString(i) + ":");
            System.out.println("LDA log likelihood = " + sample());
            System.out.println("alpha = " + Double.toString(alpha) + "\nbeta = " + Double.toString(beta));
        //printTopTypesInTopic(10);
        }
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
        sampleAlpha();
        sampleBeta();
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
            score[j] = (numInTopicPerDoc[j] + alpha) * (numWordInTopic[j][token] + beta) / (numAllWordsInTopic[j] + beta * dict.size());
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
            score[j] = (numInTopicPerDoc[j] + alpha) * (numWordInTopic[j][token] + numTestWordInTopic[j][token] + beta) / (numAllWordsInTopic[j] + numAllTestWordsInTopic[j] + beta * dict.size());
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

    public void sampleAlpha() {
        //Metropolis sampler for alpha
        double proposal;
        double newAlpha = alpha;
        
        double[] alphas     = new double[200];
        double[] alphas_lls = new double[200];
        for (int i = 0; i < 200; i++) {
            alphas[i] = 0.01 * i;
            double ll = -alphas[i];
            for (Document d : docs) {
                for (int t = 1; t <= numTopics; t++) {
                    ll += Gamma.logGamma(d.getNumInDomain(t) + alphas[i]);
                }
                ll -= Gamma.logGamma(d.size() + numTopics * alphas[i]);
            }
            ll += docs.size() * (Gamma.logGamma(numTopics * alphas[i]));
            ll -= docs.size() * numTopics * (Gamma.logGamma(alphas[i]));
            alphas_lls[i] = ll;
        }
        
        
        for (int i = 0; i < 10; i++) {
            proposal = alpha + 0.1 * rn.nextGaussian();
            if (proposal > 0) {
                double llRatio = alpha - proposal;
                for (Document d : docs) {
                    for (int t = 1; t <= numTopics; t++) {
                        llRatio += Gamma.logGamma(d.getNumInDomain(t) + proposal) 
                                 - Gamma.logGamma(d.getNumInDomain(t) + alpha);
                    }
                    llRatio -= Gamma.logGamma(d.size() + numTopics * proposal) 
                             - Gamma.logGamma(d.size() + numTopics * alpha);
                }
                llRatio += docs.size() * (Gamma.logGamma(numTopics * proposal) 
                                        - Gamma.logGamma(numTopics * alpha));
                llRatio -= docs.size() * numTopics * (Gamma.logGamma(proposal) 
                                                    - Gamma.logGamma(alpha));
                if (llRatio > Math.log(rn.nextDouble())) {
                    newAlpha = proposal;
                }
            }
        }
        alpha = newAlpha;
    }

    public void sampleBeta() {
        //Metropolis sampler for beta
        double proposal;
        double newBeta = beta;
        for (int i = 0; i < 10; i++) {
            proposal = beta + 0.1 * rn.nextGaussian();
            double llRatio = beta - proposal;
            for (int t = 0; t < numTopics; t++) {
                for (int j = 0; j < dict.size(); j++) {
                    llRatio += Gamma.logGamma(numWordInTopic[t][j] + proposal) - Gamma.logGamma(numWordInTopic[t][j] + beta);
                }
                llRatio -= Gamma.logGamma(numAllWordsInTopic[t] + dict.size() * proposal) - Gamma.logGamma(numAllWordsInTopic[t] + dict.size() * beta);
            }
            llRatio += numTopics * (Gamma.logGamma(dict.size() * proposal) - Gamma.logGamma(dict.size() * beta));
            llRatio -= numTopics * dict.size() * (Gamma.logGamma(proposal) - Gamma.logGamma(beta));
            if (llRatio > Math.log(rn.nextDouble())) {
                newBeta = proposal;
            }
        }
        beta = newBeta;
    }

    public double logLikelihood() {
        double logLike = 0.0;
        for (int i = 0; i < numTopics; i++) {
            for (int j = 0; j < dict.size(); j++) {
                logLike += Gamma.logGamma(numWordInTopic[i][j] + beta);
            }
            logLike -= Gamma.logGamma(numAllWordsInTopic[i] + dict.size() * beta);
        }
        logLike += numTopics * (Gamma.logGamma(dict.size() * beta) - dict.size() * Gamma.logGamma(beta));

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
                logLike += Gamma.logGamma(numTestWordInTopic[i][j] + numWordInTopic[i][j] + beta);
                logLike -= Gamma.logGamma(numWordInTopic[i][j] + beta); 
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
                logLike += Math.log(numWordInTopic[d.getDomain(i)-1][d.getToken(i)] + beta);
                logLike -= Math.log(numAllWordsInTopic[d.getDomain(i)-1] + dict.size()*beta);
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
    
    /*public double bitsPerWord(int n, int m) {
        double[][] logLikelihoods = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int t = 0; t < 5; t++) {
                sample();
            }
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
        int[] min = new int[input.length]; // array of the index of the minimum for each sub-array
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
    }*/
    
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
                        probOfWord[ctr] += ((numTestWordInTopic[t][d.getToken(i)] + numWordInTopic[t][d.getToken(i)] + beta)
                                           /(numAllTestWordsInTopic[t] + numAllWordsInTopic[t] + dict.size()*beta))
                                          *((numInTopicPerDoc[t] + alpha)
                                           /(i + numTopics*alpha));
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

    public ArrayList<String> getTypes() {
        return dict.words();
    }

    public ArrayList<Document> getDocs() {
        return docs;
    }

    public ArrayList<Document> getTestDocs() {
        return testDocs;
    }

    public int numTopics() {
        return numTopics;
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
        LDA lda = new LDA(numDocs, numTopics, numWords, wordsInDoc, beta);
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
        LDA bar = new LDA(dict, docs, testDocs, 2);
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