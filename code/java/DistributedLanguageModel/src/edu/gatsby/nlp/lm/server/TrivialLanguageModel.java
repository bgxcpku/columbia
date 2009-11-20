/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.gatsby.nlp.lm.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fwood
 */
public class TrivialLanguageModel extends LanguageModel implements Serializable {
private static final long serialVersionUID = 1;
    @Override
    public int remove(int domain, List<Integer> tokens) {
        return 1;
    }

    @Override
    public int removeWithCount(int domain, int count, List<Integer> tokens) {
        return count;
    }

    @Override
    public String getParameterNames() {
        return "vocabsize";
    }

    @Override
    public String getParameterValues() {
        return new String("" + (1.0 / invVocabSize));
    }

    @Override
    public void setVocabularySize(int size) {
        invVocabSize = 1.0 / (double) size;
    }

    @Override
    public void sample(int numSweeps) {
    }

    @Override
    public int addWithCount(int domain, int count, List<Integer> tokens) {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int numAdded = 0;
        for (int i = 0; i < count; i++) {
            numAdded += add(domain, tokens);
        }
        return numAdded;
    }
    double invVocabSize;

    public TrivialLanguageModel(int vocabSize) {
        invVocabSize = 1.0 / (double) vocabSize;
    }

    @Override
    public double score() {
        return -1.0;
    }

    @Override
    public int add(int domain, List<Integer> tokens) {
        return tokens.size();
    }

    @Override
    public ArrayList<Integer> predict(int domain, int numSamples, List<Integer> context) {
        ArrayList<Integer> samples = new ArrayList<Integer>(numSamples);
        for (int i = 0; i < numSamples; i++) {
            samples.add((int) Math.round(Math.random() * (1.0 / this.invVocabSize)));
        }
        return samples;
    }

    @Override
    public ArrayList<Double> score(int domain, List<Integer> context, List<Integer> tokens) {
        ArrayList<Double> scores = new ArrayList<Double>(tokens.size());
        for (int token : tokens) {
            scores.add(invVocabSize);
        }
        return scores;
    }
}
