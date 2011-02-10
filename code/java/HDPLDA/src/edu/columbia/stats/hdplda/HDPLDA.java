/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author fwood
 */
public class HDPLDA {

    public int vocabSize;
    public SetableDouble topLevelConcentration;
    public HashMap<Integer,LowLevelRestaurant> docs = new HashMap<Integer,LowLevelRestaurant>();
    public TopLevelRestaurant topLevelRestaurant ;

    public HDPLDA(int vocabSize, SetableDouble topLevelConcentration) {
        this.vocabSize = vocabSize ;
        this.topLevelConcentration = topLevelConcentration ;



    }

    public void addDocument(Document document, int documentId, SetableDouble documentConcentration) {
        LowLevelRestaurant llr = new LowLevelRestaurant(topLevelRestaurant,documentConcentration);
        llr.addDocument(document);
    }

    public void removeDocument(int documentId) {
        throw new UnsupportedOperationException();
    }

    public double sample() {
        double logProb = 0.0;
        // resample alpha's and gamma

        for(LowLevelRestaurant llr : docs.values()) {
            logProb += llr.reseatCustomers();
        }
        logProb +=topLevelRestaurant.sample();

        return logProb;
    }

    public TreeMap<Integer, Double> topicDistributionInDocument(int documentId) { return null;}

    public TreeMap<Integer, Double> topWordsInTopic(int topicId, int numWords) {return null;}

    public ArrayList<Integer> activeTopics() {return null;}
}
