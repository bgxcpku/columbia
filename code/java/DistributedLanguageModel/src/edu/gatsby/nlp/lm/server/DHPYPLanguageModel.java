/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.gatsby.nlp.lm.server;

import domainadaptation.BaseDistributionFactory;
import domainadaptation.HPYP;
import domainadaptation.PYPFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author fwood
 */
public abstract class DHPYPLanguageModel extends LanguageModel implements Serializable {
    int context_length = 2;
    double[] corpus_initial_concentrations = {0,0,0, 0, 0, 0, 0, 0,0,0,0};
    double[] corpus_initial_discounts = {1,1,1,1,1,1,1,1,1,1,1};
    HPYP general_hpyp;
    HashMap<Integer, HPYP> hpypForDomain = new HashMap<Integer, HPYP>();
    double[] latent_corpus_initial_concentrations = {0,0,0, 0, 0, 0, 0, 0,0,0,0};
    double[] latent_corpus_initial_discounts = {.5,.6,.7,.8,.9,.95,.975,.98,.99,1,1};
    double[] switch_initial_concentrations = {0,0,0, 0, 0, 0, 0, 0,0,0,0};
    double[] switch_initial_discounts = {1,1,1,1,1,1,1,1,1,1,1};
    int tuplesAdded = 0;
    int countsAdded = 0;
    int vocabSize = 1;
    private static final long serialVersionUID = 1;

    public DHPYPLanguageModel() {
    }

    @Override
    public int add(int domain, List<Integer> tokens) {
        return this.addWithCount(domain, 1, tokens);
    }

    @Override
    public int addWithCount(int domain, int count, List<Integer> tokens) {
        tuplesAdded++;
        countsAdded += count;
        if ((tuplesAdded % 500) == 0) {
            log_ps.println(tuplesAdded + " tuples, " + countsAdded + " counts added.");
        }
        HPYP hpyp = getHPYPForDomain(domain);
        List<Integer> context = tokens.subList(0, tokens.size() - 1);
        Integer type = tokens.subList(tokens.size() - 1, tokens.size()).get(0);
        hpyp.addWordInContextMultipleTimes(context, type, count);
        return count;
    }

    @Override
    public int remove(int domain, List<Integer> tokens) {
        return this.removeWithCount(domain, 1, tokens);
    }

    @Override
    public int removeWithCount(int domain, int count, List<Integer> tokens) {
        tuplesAdded--;
        countsAdded -= count;
        if ((tuplesAdded % 500) == 0) {
            log_ps.println(tuplesAdded + " tuples, " + countsAdded + " counts added.");
        }
        HPYP hpyp = getHPYPForDomain(domain);
        List<Integer> context = tokens.subList(0, tokens.size() - 1);
        Integer type = tokens.subList(tokens.size() - 1, tokens.size()).get(0);
        hpyp.removeWordInContextMultipleTimes(context, type, count);
        return count;

    }

    public Collection<HPYP> getDomainSpecificHPYPs() {
        return hpypForDomain.values();
    }

    public abstract HPYP createHPYPForDomain(int domain);

    public HPYP getHPYPForDomain(int domain) {
        HPYP retHPYP = hpypForDomain.get(domain);
        if (retHPYP == null) {

            retHPYP = createHPYPForDomain(domain);
        }
        return retHPYP;
    }

    public abstract void initializeSwitchArchitecture();

    @Override
    public ArrayList<Integer> predict(int domain, int numSamples, List<Integer> context) {
        HPYP hpyp = getHPYPForDomain(domain);
        ArrayList<Integer> samples = new ArrayList<Integer>(numSamples);
        for (int s = 0; s < numSamples; s++) {
            samples.add(hpyp.sampleNextWordFollowingGivenContext(context));
        }
        return samples;
    }

    @Override
    public void sample(int numSweeps) {
        for (int s = 0; s < numSweeps; s++) {
            for (Integer domain : hpypForDomain.keySet()) {
                HPYP chpyp = hpypForDomain.get(domain);
                log_ps.println("Sampling discount and concentration parameters for domain " + domain);
                chpyp.resampleDiscounts(3);
                chpyp.resampleConcentrations(3);
            }
            for (Integer domain : hpypForDomain.keySet()) {
                HPYP chpyp = hpypForDomain.get(domain);
                log_ps.println("Sampling  seating arrangement for domain " + domain);
                chpyp.resampleSeatingArrangement();
            }
            sampleSwitchVariables();
        }
    }

    public abstract void sampleSwitchVariables();

    @Override
    public ArrayList<Double> score(int domain, List<Integer> context, List<Integer> tokens) {
        HPYP hpyp = getHPYPForDomain(domain);
        ArrayList<Double> scores = new ArrayList<Double>(tokens.size());

        for (int s = 0; s < tokens.size(); s++) {
            scores.add(hpyp.probabilityOfWordFollowingContext(context, tokens.get(s)));
        }
        return scores;
    }

    @Override
    public double score() {
        double model_score = scoreSwitchVariables();
        model_score += general_hpyp.logProbabilityOfSeatingArrangement();
        for (HPYP chpyp : getDomainSpecificHPYPs()) {
            model_score += chpyp.logProbabilityOfSeatingArrangement();
        }
        return model_score;
    }

    public abstract double scoreSwitchVariables();

    @Override
    public void setVocabularySize(int size) {
        vocabSize = size;
    }
}
