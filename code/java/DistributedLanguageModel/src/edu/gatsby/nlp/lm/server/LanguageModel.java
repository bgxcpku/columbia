/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.gatsby.nlp.lm.server;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fwood
 */
public abstract class LanguageModel implements Serializable {
private static final long serialVersionUID = 1;
    transient PrintStream log_ps = System.out;

    public abstract int remove(int domain, List<Integer> tokens);

    public abstract int removeWithCount(int domain, int count, List<Integer> tokens);

    public abstract int add(int domain, List<Integer> tokens);

    public abstract int addWithCount(int domain, int count, List<Integer> tokens);

    public abstract ArrayList<Integer> predict(int domain, int numSamples, List<Integer> context);

    public abstract ArrayList<Double> score(int domain, List<Integer> context, List<Integer> tokens);

    public abstract void sample(int numSweeps);

    public abstract double score();

    public abstract void setVocabularySize(int size);

    public abstract String getParameterNames();

    public abstract String getParameterValues();
}
