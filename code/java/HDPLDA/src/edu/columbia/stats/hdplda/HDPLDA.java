/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 *
 * @author fwood
 */
public class HDPLDA {

    public int vocabSize;
    public double topLevelConcentration;

    public HDPLDA(int vocabSize, SetableDouble topLevelConcentration) {

    }

    public void addDocument(BagOfWordsObservation document, int documentId, SetableDouble documentConcentration) {

    }

    public void removeDocument(int documentId) {}

    public double sample() {return 0;}

    public TreeMap<Integer, Double> topicDistributionInDocument(int documentId) { return null;}

    public TreeMap<Integer, Double> topWordsInTopic(int topicId, int numWords) {return null;}

    public ArrayList<Integer> activeTopics() {return null;}
}
