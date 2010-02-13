/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.ldadhpyplm;
import com.aliasi.symbol.*;
import com.aliasi.cluster.LatentDirichletAllocation;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author davidpfau
 */
    public class LingPipeLDA {
        public SymbolTable dict;
        public int[][] docs;
        public int[][] testDocs;
        public int numTopics;
        public double alpha;
        public double beta;
        public LatentDirichletAllocation.GibbsSample currentSample;

        public LingPipeLDA(LDA foo) {
            dict = new MapSymbolTable();
            for (String s : foo.getTypes()) {
                dict.getOrAddSymbol(s);
            }
            alpha = foo.getAlpha();
            beta  = foo.getBeta();
            numTopics = foo.numTopics();

            docs     = new int[foo.getDocs().size()][];
            testDocs = new int[foo.getTestDocs().size()][];
            for (int i = 0; i < docs.length; i++) {
                ArrayList<Integer> words = foo.getDocs().get(i).getTokens();
                docs[i] = new int[words.size()];
                for (int j = 0; j < words.size(); j++) {
                    docs[i][j] = words.get(j);
                }
            }
            for (int i = 0; i < testDocs.length; i++) {
                ArrayList<Integer> words = foo.getTestDocs().get(i).getTokens();
                docs[i] = new int[words.size()];
                for (int j = 0; j < words.size(); j++) {
                    docs[i][j] = words.get(j);
                }
            }
        }

        public void sample(int nsamples) {
            currentSample = LatentDirichletAllocation.gibbsSampler(docs, (short)numTopics, alpha, beta, 200, 5, nsamples, new Random(), new LdaReportingHandler(dict));
        }

        public void sampleTestCorpus(int samples) {
            double[][] topicWordProbs = new double[numTopics][dict.numSymbols()];
            for (int i = 0; i < numTopics; i++) {
                for (int j = 0; j < dict.numSymbols(); j++) {
                    topicWordProbs[i][j] = currentSample.topicWordProb(i,j);
                }
            }
            LatentDirichletAllocation testLDA = new LatentDirichletAllocation(alpha,topicWordProbs);
        }
    }