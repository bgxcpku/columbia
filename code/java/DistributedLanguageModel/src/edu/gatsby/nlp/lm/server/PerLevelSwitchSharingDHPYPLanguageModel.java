/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.gatsby.nlp.lm.server;

import dataprocessing.Corpus;
import dataprocessing.Dictionary;
import domainadaptation.BaseDistributionFactory;
import domainadaptation.GraphicalPYP;
import domainadaptation.GraphicalPYPFactory;
import domainadaptation.HHPYPMultipleIndependentSwitchBaseDistributionFactory;
import domainadaptation.HPYP;
import domainadaptation.HPYPBaseDistributionFactory;
import domainadaptation.Measure;
import domainadaptation.Mixture;
import domainadaptation.PYP;
import domainadaptation.PYPFactory;
import domainadaptation.SetableDouble;
import domainadaptation.SuffixTree;
import domainadaptation.UniformDensity;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author fwood
 */
public class PerLevelSwitchSharingDHPYPLanguageModel extends DHPYPLanguageModel implements Serializable {

    private static final long serialVersionUID = 1;

    //HashMap<Integer, Mixture> rootMixtures = new HashMap<Integer, Mixture>();
    @Override
    public HPYP createHPYPForDomain(int domain) {
        HPYP retHPYP = null;
        // create arraylists of the correct size
        double[] corpus_concentration = new double[context_length + 1];
        for (int i = 0; i < context_length + 1; i++) {
            corpus_concentration[i] = corpus_initial_concentrations[i];
        }
        double[] corpus_discount = new double[context_length + 1];
        for (int i = 0; i < context_length + 1; i++) {
            corpus_discount[i] = corpus_initial_discounts[i];
        }
        log_ps.println("Initializing domain specific HPYP model for domain " + domain);
        HPYP corpus_hpyp = new HPYP(context_length, HPYP.setableDoubleArrayListFromDoubleArray(corpus_concentration), HPYP.setableDoubleArrayListFromDoubleArray(corpus_discount), vocabSize);
        ArrayList<Measure> corpus_root_node_distributions = new ArrayList<Measure>();
        corpus_root_node_distributions.add(corpus_hpyp.base_measure);
        corpus_root_node_distributions.add(general_hpyp.root.measure);
        Mixture corpus_root_mixture = new Mixture(switch_pyps.get(0), corpus_root_node_distributions);
        //rootMixtures.put(domain, corpus_root_mixture);
        corpus_hpyp.root.measure = new GraphicalPYP(corpus_root_mixture, corpus_hpyp.concentration.get(0), corpus_hpyp.discount.get(0));
        HHPYPMultipleIndependentSwitchBaseDistributionFactory hmisdf = new HHPYPMultipleIndependentSwitchBaseDistributionFactory(corpus_hpyp, general_hpyp, switch_pyps);
        corpus_hpyp.setBaseDistributionFactory(hmisdf);
        baseDistributionFactories.put(domain, hmisdf);
        GraphicalPYPFactory gpf = new GraphicalPYPFactory();
        corpus_hpyp.setProcessFactory(gpf);
        processFactories.put(domain, gpf);
        corpus_hpyp.setDomain(domain);
        hpypForDomain.put(domain, corpus_hpyp);
        retHPYP = corpus_hpyp;
        return retHPYP;
    }

    public void sampleSwitchVariables() {
        log_ps.println("Sampling discount and concentration parameters for the switch hpyp.");
        for (int cli = 0; cli < context_length + 1; cli++) {
            switch_pyps.get(cli).resampleDiscount(3);
            switch_pyps.get(cli).resampleConcentration(3);
        }
        log_ps.println("Sampling switch corpus seating arrangement.");
        for (int cli = 0; cli < context_length + 1; cli++) {
            switch_pyps.get(cli).resampleSeatingArrangement();
        }
    }

    public double scoreSwitchVariables() {
        double model_score = 0.0;
        for (int cli = 0; cli < context_length + 1; cli++) {
            model_score += switch_pyps.get(cli).logProbabilityOfSeatingArrangement();
        }
        return model_score;
    }
    HashMap<Integer, BaseDistributionFactory> baseDistributionFactories = new HashMap<Integer, BaseDistributionFactory>();
    HashMap<Integer, PYPFactory> processFactories = new HashMap<Integer, PYPFactory>();
    ArrayList<PYP> switch_pyps = new ArrayList<PYP>();

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(baseDistributionFactories);
        out.writeObject(processFactories);
        out.writeObject(switch_pyps);

    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        baseDistributionFactories = (HashMap<Integer, BaseDistributionFactory>) in.readObject();
        processFactories = (HashMap<Integer, PYPFactory>) in.readObject();
        switch_pyps = (ArrayList<PYP>) in.readObject();
        for (Integer domain : this.hpypForDomain.keySet()) {
            HPYP hpyp = this.hpypForDomain.get(domain);
            hpyp.setBaseDistributionFactory(baseDistributionFactories.get(domain));
            hpyp.setProcessFactory(processFactories.get(domain));
        }
    }

    public PerLevelSwitchSharingDHPYPLanguageModel(int context_length, int vocabSize, PrintStream log_ps) {

        this.log_ps = log_ps;
        setVocabularySize(vocabSize);
        this.context_length = context_length;
//        if (context_length > 6) {
//            System.err.println("Context lengths of greater than 6 currently unsupported.");
//            System.exit(-1);
//        }

        double[] general_corpus_concentration = new double[context_length + 1];
        for (int i = 0; i < context_length + 1; i++) {
            general_corpus_concentration[i] = latent_corpus_initial_concentrations[i];
        }
        double[] general_corpus_discount = new double[context_length + 1];
        for (int i = 0; i < context_length + 1; i++) {
            general_corpus_discount[i] = latent_corpus_initial_discounts[i];
        }

        log_ps.println("Initializing latent HPYP model (domain = 0)");
        ArrayList<SetableDouble> g_concentration = HPYP.setableDoubleArrayListFromDoubleArray(general_corpus_concentration);
        ArrayList<SetableDouble> g_discount = HPYP.setableDoubleArrayListFromDoubleArray(general_corpus_discount);
        general_hpyp = new HPYP(context_length, g_concentration, g_discount, vocabSize);
        HPYPBaseDistributionFactory hbdf = new HPYPBaseDistributionFactory(general_hpyp);
        general_hpyp.setBaseDistributionFactory(hbdf);
        baseDistributionFactories.put(0, hbdf);
        PYPFactory pypf = new PYPFactory();
        general_hpyp.setProcessFactory(pypf);
        processFactories.put(0, pypf);
        general_hpyp.setDomain(0);
        hpypForDomain.put(0, general_hpyp);

        initializeSwitchArchitecture();
    //general_hpyp.verifyConsistency();



    /*   if (arch.equals("hpyp_switch")) {
    run_hhpyp_experiment(c1, c2, tc, the_dictionary, context_length, burnin_sweeps, num_sweeps, results_ps, log_ps, serialization_directoryname, default_filename, do_serialization);
    } else if (arch.equals("single_switch")) {
    run_single_switch_pyp_hhpyp_experiment(c1, c2, tc, the_dictionary, context_length, burnin_sweeps, num_sweeps, results_ps, log_ps, serialization_directoryname, default_filename, do_serialization);
    } else if (arch.equals("independent_switch")) {
    run_multiple_independent_switch_hhpyp_experiment(c1, c2, tc, the_dictionary, context_length, burnin_sweeps, num_sweeps, results_ps, log_ps, serialization_directoryname, default_filename, do_serialization);
    } else {
    System.err.println("Other switch architectures not implemented yet.");
    System.exit(-1);
    } */
    }
    /*
    public static void run_hpyp_experiment(Corpus corpus1, Corpus test_corpus, Dictionary dict, int context_length, int burnin_sweeps, int num_sweeps, PrintStream results_ps, PrintStream log_ps, String serialization_directoryname, String default_filename, boolean do_serialization, boolean resample_discounts, boolean resample_concentrations) {
    Date date = new Date();
    //        double[] concentration = {0, 0, 0};
    //        //double[] discount = {0.43707, 0.7521, 0.78061};
    //        double[] discount = {0.99999, 0.80033, 0.93384};




    double[] concentration = new double[context_length + 1];
    for (int i = 0; i < context_length + 1; i++) {
    concentration[i] = initial_concentrations[i];
    }
    double[] discount = new double[context_length + 1];
    for (int i = 0; i < context_length + 1; i++) {
    discount[i] = initial_discounts[i];
    }

    HPYP model = new HPYP(context_length, HPYP.setableDoubleArrayListFromDoubleArray(concentration), HPYP.setableDoubleArrayListFromDoubleArray(discount), dict.size());
    model.setBaseDistributionFactory(new HPYPBaseDistributionFactory(model));
    model.setProcessFactory(new PYPFactory());
    //        model.setBaseDistributionFactory(new HPYPGraphicalMixtureBaseDistributionTestFactory(model));
    //        model.setProcessFactory(new GraphicalPYPFactory());

    model.addCorpus(corpus1.getTokens());

    int[] test_tokens = test_corpus.getTokens();

    //        log_ps.print("Num customers:Num tables -- ");
    //        int[] num_tables_per_num_customers = new int[model.num_tokens_added];
    //        model.root.computeSeatHistogram(num_tables_per_num_customers);
    //        int j = 0;
    //        while (num_tables_per_num_customers[j] != 0 || j == 0) {
    //            log_ps.print(j + ":" + num_tables_per_num_customers[j] + " ");
    //            j++;
    //        }
    // sweep #, discount 1, ..., n, concentration 1, ..., n, model score, sample test perplexity, average test perplexity, free mem, total mem, current time in msec

    results_ps.print("-1, ");
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(model.discount.get(d).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(model.concentration.get(c).getValue() + ", ");
    }
    double model_score = model.logProbabilityOfSeatingArrangement();
    double current_sample_training_perplexity = model.perplexity(corpus1.getTokens());
    log_ps.println("Training data perplexity : " + current_sample_training_perplexity);
    results_ps.print(model_score + ", ");
    double model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) model.num_tokens_added - (double) context_length)));
    results_ps.print(model_perplexity + ", ");
    double t_and_a_perplexity = model.perplexity(test_tokens);
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
    results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
    date = new Date();
    results_ps.println(date.getTime());

    log_ps.println("Beginning sampler burn-in");
    for (int s = 0; s < burnin_sweeps; s++) {
    log_ps.println("Burn-in Sweep " + (s + 1) + " started.");
    if (resample_discounts) {
    model.resampleDiscounts(5);
    }

    if (resample_concentrations) {
    model.resampleConcentrations(5);
    }


    model.resampleSeatingArrangement();

    results_ps.print(s + ", ");
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(model.discount.get(d).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(model.concentration.get(c).getValue() + ", ");
    }
    model_score = model.logProbabilityOfSeatingArrangement();
    current_sample_training_perplexity = model.perplexity(corpus1.getTokens());
    log_ps.println("Training data perplexity : " + current_sample_training_perplexity);
    results_ps.print(model_score + ", ");
    model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) model.num_tokens_added - (double) context_length)));
    results_ps.print(model_perplexity + ", ");
    t_and_a_perplexity = model.perplexity(test_tokens);
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
    results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
    date = new Date();
    results_ps.println(date.getTime());

    }


    double[] partial_perplexities = new double[test_tokens.length];
    for (int s = 0; s < num_sweeps; s++) {
    log_ps.println("Post burn-in sweep " + (s + 1) + " started.");


    if (resample_discounts) {
    model.resampleDiscounts(5);
    }
    if (resample_concentrations) {
    model.resampleConcentrations(5);
    }
    model.resampleSeatingArrangement();

    //            num_tables_per_num_customers = new int[model.num_tokens_added];
    //            model.root.computeSeatHistogram(num_tables_per_num_customers);
    //            j = 0;
    //            while (num_tables_per_num_customers[j] != 0 || j == 0) {
    //                log_ps.print(j + ":" + num_tables_per_num_customers[j] + " ");
    //                j++;
    //            }
    //            log_ps.println();

    //double current_sample_training_perplexity = model.perplexity(APData.training_corpus);
    double current_sample_test_perplexity = model.perplexity(test_tokens);
    current_sample_training_perplexity = model.perplexity(corpus1.getTokens());
    log_ps.println("Training data perplexity : " + current_sample_training_perplexity);
    //            log_ps.println("Joint probability seating arrangment  = " + model.logProbabilityOfSeatingArrangement());
    //            log_ps.println("Sample " + (s + 1) + ": test perplexity = " + current_sample_test_perplexity);

    model.partialPerplexity(test_tokens, partial_perplexities);
    double average_perplexity = 0.0;
    for (int i = context_length; i < partial_perplexities.length; i++) {
    average_perplexity += Math.log(partial_perplexities[i] / (double) (s + 1));
    }
    average_perplexity = java.lang.Math.pow(2, -average_perplexity / (SuffixTree.LOG_E_2 * ((double) partial_perplexities.length - (double) context_length)));

    results_ps.print(s + ", ");
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(model.discount.get(d).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(model.concentration.get(c).getValue() + ", ");
    }
    model_score = model.logProbabilityOfSeatingArrangement();
    results_ps.print(model_score + ", ");
    model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) model.num_tokens_added - (double) context_length)));
    results_ps.print(model_perplexity + ", ");
    results_ps.print(current_sample_test_perplexity + ", ");
    results_ps.print(average_perplexity + ", ");
    results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
    results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
    date = new Date();
    results_ps.println(date.getTime());


    if (do_serialization) {
    model.serializeToFile(serialization_directoryname + default_filename + s);
    }

    }
    }

    public static void run_hhpyp_experiment(Corpus corpus1, Corpus corpus2, Corpus test_corpus, Dictionary dict, int context_length, int burnin_sweeps, int num_sweeps, PrintStream results_ps, PrintStream log_ps, String serialization_directoryname, String default_filename, boolean do_serialization) {
    Date date = new Date();
    //        double[] concentration = {0, 0, 0};
    //        //double[] discount = {0.43707, 0.7521, 0.78061};
    //        double[] discount = {0.99999, 0.80033, 0.93384};

    //        double[] corpus1_initial_concentrations = {2, 5, 5, 0, 0, 0, 0, 0};
    //        double[] corpus1_initial_discounts = {.8, .8, .0001, .95, .95, .95, .95, .95};
    //        double[] corpus2_initial_concentrations = {2, 5, 5, 0, 0, 0, 0, 0};
    //        double[] corpus2_initial_discounts = {.8, .8, .0001, .95, .95, .95, .95, .95};
    //        double[] general_corpus_initial_concentrations = {2, 5, 5, 0, 0, 0, 0, 0};
    //        double[] general_corpus_initial_discounts = {.8, .0001, .0001, .95, .95, .95, .95, .95};
    //        double[] switch_initial_concentrations = {0, -.9999, -.9999, 0, 0, 0, 0, 0};
    //        double[] switch_initial_discounts = {.25, .99999, .99999, .95, .95, .95, .95, .95};

    double[] corpus1_initial_concentrations = {50, 2, 2, 0, 0, 0, 0, 0};
    double[] corpus1_initial_discounts = {.6, .8, .9, .95, .95, .95, .95, .95};
    double[] corpus2_initial_concentrations = {50, 2, 2, 0, 0, 0, 0, 0};
    double[] corpus2_initial_discounts = {.6, .8, .9, .95, .95, .95, .95, .95};
    double[] general_corpus_initial_concentrations = {50, 2, 2, 0, 0, 0, 0, 0};
    double[] general_corpus_initial_discounts = {.6, .8, .95, .95, .95, .95, .95, .95};
    double[] switch_initial_concentrations = {1, 1, 1, 0, 0, 0, 0, 0};
    double[] switch_initial_discounts = {.5, .5, .5, .95, .95, .95, .95, .95};


    double[] corpus1_concentration = new double[context_length + 1];
    double[] corpus2_concentration = new double[context_length + 1];
    double[] general_corpus_concentration = new double[context_length + 1];
    double[] switch_concentration = new double[context_length + 1];
    for (int i = 0; i < context_length + 1; i++) {
    corpus1_concentration[i] = corpus1_initial_concentrations[i];
    corpus2_concentration[i] = corpus2_initial_concentrations[i];
    general_corpus_concentration[i] = general_corpus_initial_concentrations[i];
    switch_concentration[i] = switch_initial_concentrations[i];
    }
    double[] corpus1_discount = new double[context_length + 1];
    double[] corpus2_discount = new double[context_length + 1];
    double[] general_corpus_discount = new double[context_length + 1];
    double[] switch_discount = new double[context_length + 1];
    for (int i = 0; i < context_length + 1; i++) {
    corpus1_discount[i] = corpus1_initial_discounts[i];
    corpus2_discount[i] = corpus2_initial_discounts[i];
    general_corpus_discount[i] = general_corpus_initial_discounts[i];
    switch_discount[i] = switch_initial_discounts[i];
    }

    //        HPYP model = new HPYP(context_length, HPYP.setableDoubleArrayListFromDoubleArray(concentration), HPYP.setableDoubleArrayListFromDoubleArray(discount), corpus1.dictionary.size());
    //        model.setBaseDistributionFactory(new HPYPBaseDistributionFactory(model));
    //        model.setProcessFactory(new PYPFactory());
    ////        model.setBaseDistributionFactory(new HPYPGraphicalMixtureBaseDistributionTestFactory(model));
    ////        model.setProcessFactory(new GraphicalPYPFactory());


    log_ps.println("Initializing general HPYP model");
    ArrayList<SetableDouble> g_concentration = HPYP.setableDoubleArrayListFromDoubleArray(general_corpus_concentration);
    ArrayList<SetableDouble> g_discount = HPYP.setableDoubleArrayListFromDoubleArray(general_corpus_discount);
    HPYP general_hpyp = new HPYP(context_length, g_concentration, g_discount, dict.size());
    general_hpyp.setBaseDistributionFactory(new HPYPBaseDistributionFactory(general_hpyp));
    general_hpyp.setProcessFactory(new PYPFactory());
    general_hpyp.setDomain(-1);
    //general_hpyp.verifyConsistency();

    log_ps.println("Initializing HPYP model for switch variables");
    ArrayList<SetableDouble> s_concentration = HPYP.setableDoubleArrayListFromDoubleArray(switch_concentration);
    ArrayList<SetableDouble> s_discount = HPYP.setableDoubleArrayListFromDoubleArray(switch_discount);
    HPYP switch_hpyp = new HPYP(context_length, s_concentration, s_discount, 2);
    switch_hpyp.setBaseDistributionFactory(new HPYPBaseDistributionFactory(switch_hpyp));
    switch_hpyp.setProcessFactory(new PYPFactory());
    switch_hpyp.setDomain(-2);
    //switch_hpyp.verifyConsistency();

    log_ps.println("Initializing domain specific HPYP model for corpus 1");
    HPYP corpus1_hpyp = new HPYP(context_length, HPYP.setableDoubleArrayListFromDoubleArray(corpus1_concentration), HPYP.setableDoubleArrayListFromDoubleArray(corpus1_discount), dict.size());
    ArrayList<Measure> corpus1_root_node_distributions = new ArrayList<Measure>();
    corpus1_root_node_distributions.add(corpus1_hpyp.base_measure);
    corpus1_root_node_distributions.add(general_hpyp.root.measure);
    Mixture corpus1_root_mixture = new Mixture(switch_hpyp.root.measure, corpus1_root_node_distributions);
    corpus1_hpyp.root.measure = new GraphicalPYP(corpus1_root_mixture, corpus1_hpyp.concentration.get(0), corpus1_hpyp.discount.get(0));
    corpus1_hpyp.setBaseDistributionFactory(new HHPYPBaseDistributionFactory(corpus1_hpyp, general_hpyp, switch_hpyp));
    corpus1_hpyp.setProcessFactory(new GraphicalPYPFactory());
    corpus1_hpyp.setDomain(0);


    log_ps.println("Initializing domain specific HPYP model for corpus 2");
    HPYP corpus2_hpyp = new HPYP(context_length, HPYP.setableDoubleArrayListFromDoubleArray(corpus2_concentration), HPYP.setableDoubleArrayListFromDoubleArray(corpus2_discount), dict.size());
    ArrayList<Measure> corpus2_root_node_distributions = new ArrayList<Measure>();
    corpus2_root_node_distributions.add(corpus2_hpyp.base_measure);
    corpus2_root_node_distributions.add(general_hpyp.root.measure);
    Mixture corpus2_root_mixture = new Mixture(switch_hpyp.root.measure, corpus2_root_node_distributions);
     *
    corpus2_hpyp.root.measure = new GraphicalPYP(corpus2_root_mixture, corpus2_hpyp.concentration.get(0), corpus2_hpyp.discount.get(0));
    corpus2_hpyp.setBaseDistributionFactory(new HHPYPBaseDistributionFactory(corpus2_hpyp, general_hpyp, switch_hpyp));
    corpus2_hpyp.setProcessFactory(new GraphicalPYPFactory());
    corpus2_hpyp.setDomain(1);

    corpus1_hpyp.addCorpus(corpus1.getTokens(), log_ps);
    corpus2_hpyp.addCorpus(corpus2.getTokens(), log_ps);

    int[] test_tokens = test_corpus.getTokens();

    //        log_ps.print("Num customers:Num tables -- ");
    //        int[] num_tables_per_num_customers = new int[model.num_tokens_added];
    //        model.root.computeSeatHistogram(num_tables_per_num_customers);
    //        int j = 0;
    //        while (num_tables_per_num_customers[j] != 0 || j == 0) {
    //            log_ps.print(j + ":" + num_tables_per_num_customers[j] + " ");
    //            j++;
    //        }
    // sweep #, discount 1, ..., n, concentration 1, ..., n, model score, sample test perplexity, average test perplexity, free mem, total mem, current time in msec

    results_ps.print("-1, ");
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(general_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(switch_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus1_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus2_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(general_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(switch_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus1_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus2_hpyp.concentration.get(c).getValue() + ", ");
    }
    double model_score = general_hpyp.logProbabilityOfSeatingArrangement() + switch_hpyp.logProbabilityOfSeatingArrangement() + corpus1_hpyp.logProbabilityOfSeatingArrangement() + corpus2_hpyp.logProbabilityOfSeatingArrangement();
    results_ps.print(model_score + ", ");
    double model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) corpus1_hpyp.num_tokens_added + (double) corpus2_hpyp.num_tokens_added - (double) 2 * context_length)));
    results_ps.print(model_perplexity + ", ");

    double t_and_a_perplexity = corpus2_hpyp.perplexity(test_tokens);
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
    results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
    date = new Date();
    results_ps.println(date.getTime());

    log_ps.println("Beginning sampler burn-in");
    for (int s = 0; s < burnin_sweeps; s++) {
    log_ps.println("Burn-in Sweep " + (s + 1) + " started.");



    log_ps.println("Sampling discount and concentration parameters for the general hpyp.");
    general_hpyp.resampleDiscounts(3);
    general_hpyp.resampleConcentrations(3);
    //resampleConcentrations(g_concentration, g_discount,  general_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    //resampleDiscounts(g_concentration, g_discount,  general_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    log_ps.println("Sampling discount and concentration parameters for the switch hpyp.");
    switch_hpyp.resampleDiscounts(3);
    switch_hpyp.resampleConcentrations(3);
    //resampleConcentrations(s_concentration, s_discount,  switch_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    //resampleDiscounts(s_concentration, s_discount,  switch_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    log_ps.println("Sampling discount and concentration parameters for corpus 1.");
    corpus1_hpyp.resampleDiscounts(3);
    corpus1_hpyp.resampleConcentrations(3);
    log_ps.println("Sampling discount and concentration parameters for corpus 2.");
    corpus2_hpyp.resampleDiscounts(3);
    corpus2_hpyp.resampleConcentrations(3);

    log_ps.println("Sampling corpus 1 seating arrangement.");
    corpus1_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling corpus 2 seating arrangement.");
    corpus2_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling general corpus seating arrangement.");
    general_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling switch corpus seating arrangement.");
    switch_hpyp.resampleSeatingArrangement();



    results_ps.print(s + ", ");
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(general_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(switch_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus1_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus2_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(general_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(switch_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus1_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus2_hpyp.concentration.get(c).getValue() + ", ");
    }
    model_score = general_hpyp.logProbabilityOfSeatingArrangement() + switch_hpyp.logProbabilityOfSeatingArrangement() + corpus1_hpyp.logProbabilityOfSeatingArrangement() + corpus2_hpyp.logProbabilityOfSeatingArrangement();
    results_ps.print(model_score + ", ");
    model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) corpus1_hpyp.num_tokens_added + (double) corpus2_hpyp.num_tokens_added - (double) 2 * context_length)));
    results_ps.print(model_perplexity + ", ");
    t_and_a_perplexity = corpus2_hpyp.perplexity(test_tokens);
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
    results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
    date = new Date();
    results_ps.println(date.getTime());

    }


    double[] partial_perplexities = new double[test_tokens.length];
    for (int s = 0; s < num_sweeps; s++) {
    log_ps.println("Post burn-in sweep " + (s + 1) + " started.");

    log_ps.println("Sampling discount and concentration parameters for the general hpyp.");
    general_hpyp.resampleDiscounts(3);
    general_hpyp.resampleConcentrations(3);
    //resampleConcentrations(g_concentration, g_discount,  general_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    //resampleDiscounts(g_concentration, g_discount,  general_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    log_ps.println("Sampling discount and concentration parameters for the switch hpyp.");
    switch_hpyp.resampleDiscounts(3);
    switch_hpyp.resampleConcentrations(3);
    //resampleConcentrations(s_concentration, s_discount,  switch_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    //resampleDiscounts(s_concentration, s_discount,  switch_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    log_ps.println("Sampling discount and concentration parameters for corpus 1.");
    corpus1_hpyp.resampleDiscounts(3);
    corpus1_hpyp.resampleConcentrations(3);
    log_ps.println("Sampling discount and concentration parameters for corpus 2.");
    corpus2_hpyp.resampleDiscounts(3);
    corpus2_hpyp.resampleConcentrations(3);

    log_ps.println("Sampling corpus 1 seating arrangement.");
    corpus1_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling corpus 2 seating arrangement.");
    corpus2_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling general corpus seating arrangement.");
    general_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling switch corpus seating arrangement.");
    switch_hpyp.resampleSeatingArrangement();

    //            num_tables_per_num_customers = new int[model.num_tokens_added];
    //            model.root.computeSeatHistogram(num_tables_per_num_customers);
    //            j = 0;
    //            while (num_tables_per_num_customers[j] != 0 || j == 0) {
    //                log_ps.print(j + ":" + num_tables_per_num_customers[j] + " ");
    //                j++;
    //            }
    //            log_ps.println();

    //double current_sample_training_perplexity = model.perplexity(APData.training_corpus);
    double current_sample_test_perplexity = corpus2_hpyp.perplexity(test_tokens);
    //            log_ps.println("Joint probability seating arrangment  = " + model.logProbabilityOfSeatingArrangement());
    //            log_ps.println("Sample " + (s + 1) + ": test perplexity = " + current_sample_test_perplexity);

    corpus2_hpyp.partialPerplexity(test_tokens, partial_perplexities);
    double average_perplexity = 0.0;
    for (int i = context_length; i < partial_perplexities.length; i++) {
    average_perplexity += Math.log(partial_perplexities[i] / (double) (s + 1));
    }
    average_perplexity = java.lang.Math.pow(2, -average_perplexity / (SuffixTree.LOG_E_2 * ((double) partial_perplexities.length - (double) context_length)));

    results_ps.print(s + ", ");
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(general_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(switch_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus1_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus2_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(general_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(switch_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus1_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus2_hpyp.concentration.get(c).getValue() + ", ");
    }
    model_score = general_hpyp.logProbabilityOfSeatingArrangement() + switch_hpyp.logProbabilityOfSeatingArrangement() + corpus1_hpyp.logProbabilityOfSeatingArrangement() + corpus2_hpyp.logProbabilityOfSeatingArrangement();
    results_ps.print(model_score + ", ");
    model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) corpus1_hpyp.num_tokens_added + (double) corpus2_hpyp.num_tokens_added - (double) 2 * context_length)));
    results_ps.print(model_perplexity + ", ");
    results_ps.print(current_sample_test_perplexity + ", ");
    results_ps.print(average_perplexity + ", ");
    results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
    results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
    date = new Date();
    results_ps.println(date.getTime());

    if ((do_serialization && s < 5) || (do_serialization && s % 10 == 0)) {
    corpus1_hpyp.serializeToFile(serialization_directoryname + "corpus_1-" + s);
    corpus2_hpyp.serializeToFile(serialization_directoryname + "corpus_2-" + s);
    general_hpyp.serializeToFile(serialization_directoryname + "general-" + s);
    switch_hpyp.serializeToFile(serialization_directoryname + "switch-" + s);
    }

    }
    }

    public static void run_single_switch_pyp_hhpyp_experiment(Corpus corpus1, Corpus corpus2, Corpus test_corpus, Dictionary dict, int context_length, int burnin_sweeps, int num_sweeps, PrintStream results_ps, PrintStream log_ps, String serialization_directoryname, String default_filename, boolean do_serialization) {
    Date date = new Date();
    //        double[] concentration = {0, 0, 0};
    //        //double[] discount = {0.43707, 0.7521, 0.78061};
    //        double[] discount = {0.99999, 0.80033, 0.93384};

    //        double[] corpus1_initial_concentrations = {2, 5, 5, 0, 0, 0, 0, 0};
    //        double[] corpus1_initial_discounts = {.8, .8, .0001, .95, .95, .95, .95, .95};
    //        double[] corpus2_initial_concentrations = {2, 5, 5, 0, 0, 0, 0, 0};
    //        double[] corpus2_initial_discounts = {.8, .8, .0001, .95, .95, .95, .95, .95};
    //        double[] general_corpus_initial_concentrations = {2, 5, 5, 0, 0, 0, 0, 0};
    //        double[] general_corpus_initial_discounts = {.8, .0001, .0001, .95, .95, .95, .95, .95};
    //        double[] switch_initial_concentrations = {0, -.9999, -.9999, 0, 0, 0, 0, 0};
    //        double[] switch_initial_discounts = {.25, .99999, .99999, .95, .95, .95, .95, .95};

    double[] corpus1_initial_concentrations = {50, 2, 2, 0, 0, 0, 0, 0};
    double[] corpus1_initial_discounts = {.6, .8, .9, .95, .95, .95, .95, .95};
    double[] corpus2_initial_concentrations = {50, 2, 2, 0, 0, 0, 0, 0};
    double[] corpus2_initial_discounts = {.6, .8, .9, .95, .95, .95, .95, .95};
    double[] general_corpus_initial_concentrations = {50, 2, 2, 0, 0, 0, 0, 0};
    double[] general_corpus_initial_discounts = {.6, .8, .95, .95, .95, .95, .95, .95};
    double[] switch_initial_concentrations = {1, 1, 1, 0, 0, 0, 0, 0};
    double[] switch_initial_discounts = {.5, .5, .5, .95, .95, .95, .95, .95};


    double[] corpus1_concentration = new double[context_length + 1];
    double[] corpus2_concentration = new double[context_length + 1];
    double[] general_corpus_concentration = new double[context_length + 1];
    double[] switch_concentration = new double[context_length + 1];
    for (int i = 0; i < context_length + 1; i++) {
    corpus1_concentration[i] = corpus1_initial_concentrations[i];
    corpus2_concentration[i] = corpus2_initial_concentrations[i];
    general_corpus_concentration[i] = general_corpus_initial_concentrations[i];
    switch_concentration[i] = switch_initial_concentrations[i];
    }
    double[] corpus1_discount = new double[context_length + 1];
    double[] corpus2_discount = new double[context_length + 1];
    double[] general_corpus_discount = new double[context_length + 1];
    double[] switch_discount = new double[context_length + 1];
    for (int i = 0; i < context_length + 1; i++) {
    corpus1_discount[i] = corpus1_initial_discounts[i];
    corpus2_discount[i] = corpus2_initial_discounts[i];
    general_corpus_discount[i] = general_corpus_initial_discounts[i];
    switch_discount[i] = switch_initial_discounts[i];
    }

    //        HPYP model = new HPYP(context_length, HPYP.setableDoubleArrayListFromDoubleArray(concentration), HPYP.setableDoubleArrayListFromDoubleArray(discount), corpus1.dictionary.size());
    //        model.setBaseDistributionFactory(new HPYPBaseDistributionFactory(model));
    //        model.setProcessFactory(new PYPFactory());
    ////        model.setBaseDistributionFactory(new HPYPGraphicalMixtureBaseDistributionTestFactory(model));
    ////        model.setProcessFactory(new GraphicalPYPFactory());


    log_ps.println("Initializing general HPYP model");
    ArrayList<SetableDouble> g_concentration = HPYP.setableDoubleArrayListFromDoubleArray(general_corpus_concentration);
    ArrayList<SetableDouble> g_discount = HPYP.setableDoubleArrayListFromDoubleArray(general_corpus_discount);
    HPYP general_hpyp = new HPYP(context_length, g_concentration, g_discount, dict.size());
    general_hpyp.setBaseDistributionFactory(new HPYPBaseDistributionFactory(general_hpyp));
    general_hpyp.setProcessFactory(new PYPFactory());
    general_hpyp.setDomain(-1);
    //general_hpyp.verifyConsistency();

    log_ps.println("Initializing PYP model for switch variables");
    //        ArrayList<SetableDouble> s_concentration = HPYP.setableDoubleArrayListFromDoubleArray(switch_concentration);
    //        ArrayList<SetableDouble> s_discount = HPYP.setableDoubleArrayListFromDoubleArray(switch_discount);
    //
    //        HPYP switch_hpyp = new HPYP(context_length, s_concentration, s_discount, 2);
    //        switch_hpyp.setBaseDistributionFactory(new HPYPBaseDistributionFactory(switch_hpyp));
    //        switch_hpyp.setProcessFactory(new PYPFactory());
    //        switch_hpyp.setDomain(-2);
    //        //switch_hpyp.verifyConsistency();

    UniformDensity switch_pyp_basedistribution = new UniformDensity(2);
    PYP switch_pyp = new PYP(switch_pyp_basedistribution, new SetableDouble(switch_concentration[0]), new SetableDouble(switch_discount[0]));


    log_ps.println("Initializing domain specific HPYP model for corpus 1");
    HPYP corpus1_hpyp = new HPYP(context_length, HPYP.setableDoubleArrayListFromDoubleArray(corpus1_concentration), HPYP.setableDoubleArrayListFromDoubleArray(corpus1_discount), dict.size());
    ArrayList<Measure> corpus1_root_node_distributions = new ArrayList<Measure>();
    corpus1_root_node_distributions.add(corpus1_hpyp.base_measure);
    corpus1_root_node_distributions.add(general_hpyp.root.measure);
    Mixture corpus1_root_mixture = new Mixture(switch_pyp, corpus1_root_node_distributions);
    corpus1_hpyp.root.measure = new GraphicalPYP(corpus1_root_mixture, corpus1_hpyp.concentration.get(0), corpus1_hpyp.discount.get(0));
    corpus1_hpyp.setBaseDistributionFactory(new HHPYPSingleSwitchBaseDistributionFactory(corpus1_hpyp, general_hpyp, switch_pyp));
    corpus1_hpyp.setProcessFactory(new GraphicalPYPFactory());
    corpus1_hpyp.setDomain(0);


    log_ps.println("Initializing domain specific HPYP model for corpus 2");
    HPYP corpus2_hpyp = new HPYP(context_length, HPYP.setableDoubleArrayListFromDoubleArray(corpus2_concentration), HPYP.setableDoubleArrayListFromDoubleArray(corpus2_discount), dict.size());
    ArrayList<Measure> corpus2_root_node_distributions = new ArrayList<Measure>();
    corpus2_root_node_distributions.add(corpus1_hpyp.base_measure);
    corpus2_root_node_distributions.add(general_hpyp.root.measure);
    Mixture corpus2_root_mixture = new Mixture(switch_pyp, corpus2_root_node_distributions);
    corpus2_hpyp.root.measure = new GraphicalPYP(corpus2_root_mixture, corpus2_hpyp.concentration.get(0), corpus2_hpyp.discount.get(0));
    corpus2_hpyp.setBaseDistributionFactory(new HHPYPSingleSwitchBaseDistributionFactory(corpus2_hpyp, general_hpyp, switch_pyp));
    corpus2_hpyp.setProcessFactory(new GraphicalPYPFactory());
    corpus2_hpyp.setDomain(1);

    corpus1_hpyp.addCorpus(corpus1.getTokens(), log_ps);
    corpus2_hpyp.addCorpus(corpus2.getTokens(), log_ps);

    int[] test_tokens = test_corpus.getTokens();

    //        log_ps.print("Num customers:Num tables -- ");
    //        int[] num_tables_per_num_customers = new int[model.num_tokens_added];
    //        model.root.computeSeatHistogram(num_tables_per_num_customers);
    //        int j = 0;
    //        while (num_tables_per_num_customers[j] != 0 || j == 0) {
    //            log_ps.print(j + ":" + num_tables_per_num_customers[j] + " ");
    //            j++;
    //        }
    // sweep #, discount 1, ..., n, concentration 1, ..., n, model score, sample test perplexity, average test perplexity, free mem, total mem, current time in msec

    results_ps.print("-1, ");
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(general_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(switch_pyp.discount.getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus1_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus2_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(general_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(switch_pyp.concentration.getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus1_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus2_hpyp.concentration.get(c).getValue() + ", ");
    }
    double model_score = general_hpyp.logProbabilityOfSeatingArrangement() + switch_pyp.logProbabilityOfSeatingArrangement() + corpus1_hpyp.logProbabilityOfSeatingArrangement() + corpus2_hpyp.logProbabilityOfSeatingArrangement();
    results_ps.print(model_score + ", ");
    double model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) corpus1_hpyp.num_tokens_added + (double) corpus2_hpyp.num_tokens_added - (double) 2 * context_length)));
    results_ps.print(model_perplexity + ", ");

    double t_and_a_perplexity = corpus2_hpyp.perplexity(test_tokens);
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
    results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
    date = new Date();
    results_ps.println(date.getTime());

    log_ps.println("Beginning sampler burn-in");
    for (int s = 0; s < burnin_sweeps; s++) {
    log_ps.println("Burn-in Sweep " + (s + 1) + " started.");



    log_ps.println("Sampling discount and concentration parameters for the general hpyp.");
    general_hpyp.resampleDiscounts(3);
    general_hpyp.resampleConcentrations(3);
    //resampleConcentrations(g_concentration, g_discount,  general_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    //resampleDiscounts(g_concentration, g_discount,  general_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    log_ps.println("Sampling discount and concentration parameters for the switch hpyp.");
    switch_pyp.resampleDiscount(3);
    switch_pyp.resampleConcentration(3);
    //resampleConcentrations(s_concentration, s_discount,  switch_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    //resampleDiscounts(s_concentration, s_discount,  switch_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    log_ps.println("Sampling discount and concentration parameters for corpus 1.");
    corpus1_hpyp.resampleDiscounts(3);
    corpus1_hpyp.resampleConcentrations(3);
    log_ps.println("Sampling discount and concentration parameters for corpus 2.");
    corpus2_hpyp.resampleDiscounts(3);
    corpus2_hpyp.resampleConcentrations(3);

    log_ps.println("Sampling corpus 1 seating arrangement.");
    corpus1_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling corpus 2 seating arrangement.");
    corpus2_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling general corpus seating arrangement.");
    general_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling switch corpus seating arrangement.");
    switch_pyp.resampleSeatingArrangement();



    results_ps.print(s + ", ");
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(general_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(switch_pyp.discount.getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus1_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus2_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(general_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(switch_pyp.concentration.getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus1_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus2_hpyp.concentration.get(c).getValue() + ", ");
    }
    model_score = general_hpyp.logProbabilityOfSeatingArrangement() + switch_pyp.logProbabilityOfSeatingArrangement() + corpus1_hpyp.logProbabilityOfSeatingArrangement() + corpus2_hpyp.logProbabilityOfSeatingArrangement();
    results_ps.print(model_score + ", ");
    model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) corpus1_hpyp.num_tokens_added + (double) corpus2_hpyp.num_tokens_added - (double) 2 * context_length)));
    results_ps.print(model_perplexity + ", ");
    t_and_a_perplexity = corpus2_hpyp.perplexity(test_tokens);
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
    results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
    date = new Date();
    results_ps.println(date.getTime());

    }


    double[] partial_perplexities = new double[test_tokens.length];
    for (int s = 0; s < num_sweeps; s++) {
    log_ps.println("Post burn-in sweep " + (s + 1) + " started.");

    log_ps.println("Sampling discount and concentration parameters for the general hpyp.");
    general_hpyp.resampleDiscounts(3);
    general_hpyp.resampleConcentrations(3);
    //resampleConcentrations(g_concentration, g_discount,  general_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    //resampleDiscounts(g_concentration, g_discount,  general_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    log_ps.println("Sampling discount and concentration parameters for the switch hpyp.");
    switch_pyp.resampleDiscount(3);
    switch_pyp.resampleConcentration(3);
    //resampleConcentrations(s_concentration, s_discount,  switch_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    //resampleDiscounts(s_concentration, s_discount,  switch_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    log_ps.println("Sampling discount and concentration parameters for corpus 1.");
    corpus1_hpyp.resampleDiscounts(3);
    corpus1_hpyp.resampleConcentrations(3);
    log_ps.println("Sampling discount and concentration parameters for corpus 2.");
    corpus2_hpyp.resampleDiscounts(3);
    corpus2_hpyp.resampleConcentrations(3);

    log_ps.println("Sampling corpus 1 seating arrangement.");
    corpus1_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling corpus 2 seating arrangement.");
    corpus2_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling general corpus seating arrangement.");
    general_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling switch corpus seating arrangement.");
    switch_pyp.resampleSeatingArrangement();

    //            num_tables_per_num_customers = new int[model.num_tokens_added];
    //            model.root.computeSeatHistogram(num_tables_per_num_customers);
    //            j = 0;
    //            while (num_tables_per_num_customers[j] != 0 || j == 0) {
    //                log_ps.print(j + ":" + num_tables_per_num_customers[j] + " ");
    //                j++;
    //            }
    //            log_ps.println();

    //double current_sample_training_perplexity = model.perplexity(APData.training_corpus);
    double current_sample_test_perplexity = corpus2_hpyp.perplexity(test_tokens);
    //            log_ps.println("Joint probability seating arrangment  = " + model.logProbabilityOfSeatingArrangement());
    //            log_ps.println("Sample " + (s + 1) + ": test perplexity = " + current_sample_test_perplexity);

    corpus2_hpyp.partialPerplexity(test_tokens, partial_perplexities);
    double average_perplexity = 0.0;
    for (int i = context_length; i < partial_perplexities.length; i++) {
    average_perplexity += Math.log(partial_perplexities[i] / (double) (s + 1));
    }
    average_perplexity = java.lang.Math.pow(2, -average_perplexity / (SuffixTree.LOG_E_2 * ((double) partial_perplexities.length - (double) context_length)));

    results_ps.print(s + ", ");
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(general_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(switch_pyp.discount.getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus1_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus2_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(general_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(switch_pyp.concentration.getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus1_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus2_hpyp.concentration.get(c).getValue() + ", ");
    }
    model_score = general_hpyp.logProbabilityOfSeatingArrangement() + switch_pyp.logProbabilityOfSeatingArrangement() + corpus1_hpyp.logProbabilityOfSeatingArrangement() + corpus2_hpyp.logProbabilityOfSeatingArrangement();
    results_ps.print(model_score + ", ");
    model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) corpus1_hpyp.num_tokens_added + (double) corpus2_hpyp.num_tokens_added - (double) 2 * context_length)));
    results_ps.print(model_perplexity + ", ");
    results_ps.print(current_sample_test_perplexity + ", ");
    results_ps.print(average_perplexity + ", ");
    results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
    results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
    date = new Date();
    results_ps.println(date.getTime());

    if ((do_serialization && s < 5) || (do_serialization && s % 10 == 0)) {
    corpus1_hpyp.serializeToFile(serialization_directoryname + "corpus_1-" + s);
    corpus2_hpyp.serializeToFile(serialization_directoryname + "corpus_2-" + s);
    general_hpyp.serializeToFile(serialization_directoryname + "general-" + s);
    switch_pyp.serializeToFile(serialization_directoryname + "switch-" + s);
    }

    }
    }
     */

    public void initializeSwitchArchitecture() {  // multiple independent switch
        //Date date = new Date();

        double[] switch_concentration = new double[context_length + 1];
        for (int i = 0; i < context_length + 1; i++) {
            switch_concentration[i] = switch_initial_concentrations[i];
        }
        double[] switch_discount = new double[context_length + 1];
        for (int i = 0; i < context_length + 1; i++) {
            switch_discount[i] = switch_initial_discounts[i];
        }

        log_ps.println("Initializing PYP model for switch variables");
        UniformDensity switch_pyp_basedistribution = new UniformDensity(2);

        for (int cli = 0; cli < context_length + 1; cli++) {
            switch_pyps.add(new PYP(switch_pyp_basedistribution, new SetableDouble(switch_concentration[cli]), new SetableDouble(switch_discount[cli])));
        }
    /*PrintStream results_ps = log_ps;

    results_ps.print("-1, ");
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(general_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(switch_pyps.get(d).discount.getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus1_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus2_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(general_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(switch_pyps.get(c).concentration.getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus1_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus2_hpyp.concentration.get(c).getValue() + ", ");
    }
    double model_score = 0.0;
    for (int cli = 0; cli < context_length + 1; cli++) {
    model_score += switch_pyps.get(cli).logProbabilityOfSeatingArrangement();
    }
    model_score += general_hpyp.logProbabilityOfSeatingArrangement() + corpus1_hpyp.logProbabilityOfSeatingArrangement() + corpus2_hpyp.logProbabilityOfSeatingArrangement();
    results_ps.print(model_score + ", ");
    double model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) corpus1_hpyp.num_tokens_added + (double) corpus2_hpyp.num_tokens_added - (double) 2 * context_length)));
    results_ps.print(model_perplexity + ", ");

    double t_and_a_perplexity = corpus2_hpyp.perplexity(test_tokens);
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
    results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
    date = new Date();
    results_ps.println(date.getTime());

    log_ps.println("Beginning sampler burn-in");
    for (int s = 0; s < burnin_sweeps; s++) {
    log_ps.println("Burn-in Sweep " + (s + 1) + " started.");



    log_ps.println("Sampling discount and concentration parameters for the general hpyp.");
    general_hpyp.resampleDiscounts(3);
    general_hpyp.resampleConcentrations(3);
    //resampleConcentrations(g_concentration, g_discount,  general_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    //resampleDiscounts(g_concentration, g_discount,  general_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    log_ps.println("Sampling discount and concentration parameters for the switch hpyp.");
    for (int cli = 0; cli < context_length + 1; cli++) {
    switch_pyps.get(cli).resampleDiscount(3);
    switch_pyps.get(cli).resampleConcentration(3);
    }
    //resampleConcentrations(s_concentration, s_discount,  switch_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    //resampleDiscounts(s_concentration, s_discount,  switch_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    log_ps.println("Sampling discount and concentration parameters for corpus 1.");
    corpus1_hpyp.resampleDiscounts(3);
    corpus1_hpyp.resampleConcentrations(3);
    log_ps.println("Sampling discount and concentration parameters for corpus 2.");
    corpus2_hpyp.resampleDiscounts(3);
    corpus2_hpyp.resampleConcentrations(3);

    log_ps.println("Sampling corpus 1 seating arrangement.");
    corpus1_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling corpus 2 seating arrangement.");
    corpus2_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling general corpus seating arrangement.");
    general_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling switch corpus seating arrangement.");
    for (int cli = 0; cli < context_length + 1; cli++) {
    switch_pyps.get(cli).resampleSeatingArrangement();
    }


    results_ps.print(s + ", ");
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(general_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(switch_pyps.get(d).discount.getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus1_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus2_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(general_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(switch_pyps.get(c).concentration.getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus1_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus2_hpyp.concentration.get(c).getValue() + ", ");
    }
    model_score = 0.0;
    for (int cli = 0; cli < context_length + 1; cli++) {
    model_score += switch_pyps.get(cli).logProbabilityOfSeatingArrangement();
    }

    model_score += general_hpyp.logProbabilityOfSeatingArrangement() + corpus1_hpyp.logProbabilityOfSeatingArrangement() + corpus2_hpyp.logProbabilityOfSeatingArrangement();
    results_ps.print(model_score + ", ");
    model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) corpus1_hpyp.num_tokens_added + (double) corpus2_hpyp.num_tokens_added - (double) 2 * context_length)));
    results_ps.print(model_perplexity + ", ");
    t_and_a_perplexity = corpus2_hpyp.perplexity(test_tokens);
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(t_and_a_perplexity + ", ");
    results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
    results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
    date = new Date();
    results_ps.println(date.getTime());

    }


    double[] partial_perplexities = new double[test_tokens.length];
    for (int s = 0; s < num_sweeps; s++) {
    log_ps.println("Post burn-in sweep " + (s + 1) + " started.");

    log_ps.println("Sampling discount and concentration parameters for the general hpyp.");
    general_hpyp.resampleDiscounts(3);
    general_hpyp.resampleConcentrations(3);
    //resampleConcentrations(g_concentration, g_discount,  general_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    //resampleDiscounts(g_concentration, g_discount,  general_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    log_ps.println("Sampling discount and concentration parameters for the switch hpyp.");
    for (int cli = 0; cli < context_length + 1; cli++) {
    switch_pyps.get(cli).resampleDiscount(3);
    switch_pyps.get(cli).resampleConcentration(3);
    }
    //resampleConcentrations(s_concentration, s_discount,  switch_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    //resampleDiscounts(s_concentration, s_discount,  switch_hpyp,  corpus1_hpyp,  corpus2_hpyp, 3);
    log_ps.println("Sampling discount and concentration parameters for corpus 1.");
    corpus1_hpyp.resampleDiscounts(3);
    corpus1_hpyp.resampleConcentrations(3);
    log_ps.println("Sampling discount and concentration parameters for corpus 2.");
    corpus2_hpyp.resampleDiscounts(3);
    corpus2_hpyp.resampleConcentrations(3);

    log_ps.println("Sampling corpus 1 seating arrangement.");
    corpus1_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling corpus 2 seating arrangement.");
    corpus2_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling general corpus seating arrangement.");
    general_hpyp.resampleSeatingArrangement();
    log_ps.println("Sampling switch corpus seating arrangement.");
    for (int cli = 0; cli < context_length + 1; cli++) {
    switch_pyps.get(cli).resampleSeatingArrangement();
    }

    //            num_tables_per_num_customers = new int[model.num_tokens_added];
    //            model.root.computeSeatHistogram(num_tables_per_num_customers);
    //            j = 0;
    //            while (num_tables_per_num_customers[j] != 0 || j == 0) {
    //                log_ps.print(j + ":" + num_tables_per_num_customers[j] + " ");
    //                j++;
    //            }
    //            log_ps.println();

    //double current_sample_training_perplexity = model.perplexity(APData.training_corpus);
    double current_sample_test_perplexity = corpus2_hpyp.perplexity(test_tokens);
    //            log_ps.println("Joint probability seating arrangment  = " + model.logProbabilityOfSeatingArrangement());
    //            log_ps.println("Sample " + (s + 1) + ": test perplexity = " + current_sample_test_perplexity);

    corpus2_hpyp.partialPerplexity(test_tokens, partial_perplexities);
    double average_perplexity = 0.0;
    for (int i = context_length; i < partial_perplexities.length; i++) {
    average_perplexity += Math.log(partial_perplexities[i] / (double) (s + 1));
    }
    average_perplexity = java.lang.Math.pow(2, -average_perplexity / (SuffixTree.LOG_E_2 * ((double) partial_perplexities.length - (double) context_length)));

    results_ps.print(s + ", ");
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(general_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(switch_pyps.get(d).discount.getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus1_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int d = 0; d < context_length + 1; d++) {
    results_ps.print(corpus2_hpyp.discount.get(d).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(general_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(switch_pyps.get(c).concentration.getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus1_hpyp.concentration.get(c).getValue() + ", ");
    }
    for (int c = 0; c < context_length + 1; c++) {
    results_ps.print(corpus2_hpyp.concentration.get(c).getValue() + ", ");
    }
    model_score = 0.0;
    for (int cli = 0; cli < context_length + 1; cli++) {
    model_score += switch_pyps.get(cli).logProbabilityOfSeatingArrangement();
    }
    model_score += general_hpyp.logProbabilityOfSeatingArrangement() + corpus1_hpyp.logProbabilityOfSeatingArrangement() + corpus2_hpyp.logProbabilityOfSeatingArrangement();
    results_ps.print(model_score + ", ");
    model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) corpus1_hpyp.num_tokens_added + (double) corpus2_hpyp.num_tokens_added - (double) 2 * context_length)));
    results_ps.print(model_perplexity + ", ");
    results_ps.print(current_sample_test_perplexity + ", ");
    results_ps.print(average_perplexity + ", ");
    results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
    results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
    date = new Date();
    results_ps.println(date.getTime());

    if ((do_serialization && s < 5) || (do_serialization && s % 10 == 0)) {
    corpus1_hpyp.serializeToFile(serialization_directoryname + "corpus_1-" + s);
    corpus2_hpyp.serializeToFile(serialization_directoryname + "corpus_2-" + s);
    general_hpyp.serializeToFile(serialization_directoryname + "general-" + s);
    for (int cli = 0; cli < context_length + 1; cli++) {
    switch_pyps.get(cli).serializeToFile(serialization_directoryname + "switch-" + cli + "-" + s);
    }
    }

    }*/
    }

    // log file format
    // sweep #, discount 1, ..., n, concentration 1, ..., n, model score, sample test perplexity, average test perplexity
    public static void run_hpyp_experiment(Corpus corpus1, Corpus corpus2, Corpus test_corpus, Dictionary dict, int context_length, int burnin_sweeps, int num_sweeps, PrintStream results_ps, PrintStream log_ps, String serialization_directoryname, String default_filename, boolean do_serialization, boolean resample_discounts, boolean resample_concentrations) {
        Date date = new Date();
//        double[] concentration = {0, 0, 0};
//        //double[] discount = {0.43707, 0.7521, 0.78061};
//        double[] discount = {0.99999, 0.80033, 0.93384};

        double[] initial_concentrations = {50, 2, 2, 0, 0, 0, 0, 0};
        double[] initial_discounts = {.6, .7, .9, .95, .95, .95, .95, .95};


        double[] concentration = new double[context_length + 1];
        for (int i = 0; i < context_length + 1; i++) {
            concentration[i] = initial_concentrations[i];
        }
        double[] discount = new double[context_length + 1];
        for (int i = 0; i < context_length + 1; i++) {
            discount[i] = initial_discounts[i];
        }

        HPYP model = new HPYP(context_length, HPYP.setableDoubleArrayListFromDoubleArray(concentration), HPYP.setableDoubleArrayListFromDoubleArray(discount), dict.size());
        model.setBaseDistributionFactory(new HPYPBaseDistributionFactory(model));
        model.setProcessFactory(new PYPFactory());
//        model.setBaseDistributionFactory(new HPYPGraphicalMixtureBaseDistributionTestFactory(model));
//        model.setProcessFactory(new GraphicalPYPFactory());

        model.addCorpus(corpus1.getTokens(), log_ps);
        model.addCorpus(corpus2.getTokens(), log_ps);

        int[] test_tokens = test_corpus.getTokens();

        log_ps.print("Num customers:Num tables -- ");
        int[] num_tables_per_num_customers = new int[model.num_tokens_added];
        model.root.computeSeatHistogram(num_tables_per_num_customers);
        int j = 0;
        while (num_tables_per_num_customers[j] != 0 || j == 0) {
            log_ps.print(j + ":" + num_tables_per_num_customers[j] + " ");
            j++;
        }
        // sweep #, discount 1, ..., n, concentration 1, ..., n, model score, sample test perplexity, average test perplexity, free mem, total mem, current time in msec

        results_ps.print("-1, ");
        for (int d = 0; d < context_length + 1; d++) {
            results_ps.print(model.discount.get(d).getValue() + ", ");
        }
        for (int c = 0; c < context_length + 1; c++) {
            results_ps.print(model.concentration.get(c).getValue() + ", ");
        }
        double model_score = model.logProbabilityOfSeatingArrangement();
        results_ps.print(model_score + ", ");
        double model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) model.num_tokens_added - (double) context_length)));
        results_ps.print(model_perplexity + ", ");
        double t_and_a_perplexity = model.perplexity(test_tokens);
        results_ps.print(t_and_a_perplexity + ", ");
        results_ps.print(t_and_a_perplexity + ", ");
        results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
        results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
        date = new Date();
        results_ps.println(date.getTime());

        log_ps.println("Beginning sampler burn-in");
        for (int s = 0; s < burnin_sweeps; s++) {
            log_ps.println("Burn-in Sweep " + (s + 1) + " started.");
            if (resample_discounts) {
                model.resampleDiscounts(5);
            }
            if (resample_concentrations) {
                model.resampleConcentrations(5);
            }
            model.resampleSeatingArrangement();

            results_ps.print(s + ", ");
            for (int d = 0; d < context_length + 1; d++) {
                results_ps.print(model.discount.get(d).getValue() + ", ");
            }
            for (int c = 0; c < context_length + 1; c++) {
                results_ps.print(model.concentration.get(c).getValue() + ", ");
            }
            model_score = model.logProbabilityOfSeatingArrangement();
            results_ps.print(model_score + ", ");
            model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) model.num_tokens_added - (double) context_length)));
            results_ps.print(model_perplexity + ", ");
            t_and_a_perplexity = model.perplexity(test_tokens);
            results_ps.print(t_and_a_perplexity + ", ");
            results_ps.print(t_and_a_perplexity + ", ");
            results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
            results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
            date = new Date();
            results_ps.println(date.getTime());

        }


        double[] partial_perplexities = new double[test_tokens.length];
        for (int s = 0; s < num_sweeps; s++) {
            log_ps.println("Post burn-in sweep " + (s + 1) + " started.");
            if (resample_discounts) {
                model.resampleDiscounts(5);
            }
            if (resample_concentrations) {
                model.resampleConcentrations(5);
            }
            model.resampleSeatingArrangement();

            num_tables_per_num_customers = new int[model.num_tokens_added];
            model.root.computeSeatHistogram(num_tables_per_num_customers);
            j = 0;
            while (num_tables_per_num_customers[j] != 0 || j == 0) {
                log_ps.print(j + ":" + num_tables_per_num_customers[j] + " ");
                j++;
            }
            log_ps.println();

            //double current_sample_training_perplexity = model.perplexity(APData.training_corpus);
            double current_sample_test_perplexity = model.perplexity(test_tokens);
//            log_ps.println("Joint probability seating arrangment  = " + model.logProbabilityOfSeatingArrangement());
//            log_ps.println("Sample " + (s + 1) + ": test perplexity = " + current_sample_test_perplexity);

            model.partialPerplexity(test_tokens, partial_perplexities);
            double average_perplexity = 0.0;
            for (int i = context_length; i < partial_perplexities.length; i++) {
                average_perplexity += Math.log(partial_perplexities[i] / (double) (s + 1));
            }
            average_perplexity = java.lang.Math.pow(2, -average_perplexity / (SuffixTree.LOG_E_2 * ((double) partial_perplexities.length - (double) context_length)));

            results_ps.print(s + ", ");
            for (int d = 0; d < context_length + 1; d++) {
                results_ps.print(model.discount.get(d).getValue() + ", ");
            }
            for (int c = 0; c < context_length + 1; c++) {
                results_ps.print(model.concentration.get(c).getValue() + ", ");
            }
            model_score = model.logProbabilityOfSeatingArrangement();
            results_ps.print(model_score + ", ");
            model_perplexity = java.lang.Math.pow(2, -model_score / (SuffixTree.LOG_E_2 * ((double) model.num_tokens_added - (double) context_length)));
            results_ps.print(model_perplexity + ", ");
            results_ps.print(current_sample_test_perplexity + ", ");
            results_ps.print(average_perplexity + ", ");
            results_ps.print(Runtime.getRuntime().freeMemory() + ", ");
            results_ps.print(Runtime.getRuntime().totalMemory() + ", ");
            date = new Date();
            results_ps.println(date.getTime());


            if ((do_serialization && s < 5) || (do_serialization && s % 10 == 0)) {
                model.serializeToFile(serialization_directoryname + s);
            }

        }
    }

    @Override
    public String getParameterNames() {
        String ret_string = "";

        for (Entry<Integer, HPYP> entry : hpypForDomain.entrySet()) {
            int domain = entry.getKey();

            for (int d = 0; d < context_length + 1; d++) {
                ret_string = ret_string.concat("domain " + domain + " discount depth " + d + ", ");
            }
            for (int c = 0; c < context_length + 1; c++) {
                ret_string = ret_string.concat("domain " + domain + " concentration depth " + c + ", ");
            }
        }
        int depth = 0;
        for (PYP pyp : switch_pyps) {
            ret_string = ret_string.concat("switch discount depth " + depth + ", ");
            depth++;
        }
        depth = 0;
        for (PYP pyp : switch_pyps) {
            ret_string = ret_string.concat("switch concentration depth " + depth + ", ");
            depth++;
        }
        return ret_string;
    }

    @Override
    public String getParameterValues() {
        String ret_string = "";

        for (Entry<Integer, HPYP> entry : hpypForDomain.entrySet()) {
            HPYP model = entry.getValue();

            for (int d = 0; d < context_length + 1; d++) {
                ret_string = ret_string.concat(model.discount.get(d).getValue() + ", ");
            }
            for (int c = 0; c < context_length + 1; c++) {
                ret_string = ret_string.concat(model.concentration.get(c).getValue() + ", ");
            }
        }

        int depth = 0;
        for (PYP pyp : switch_pyps) {
            ret_string = ret_string.concat(pyp.discount.getValue() + ", ");
        }
        for (PYP pyp : switch_pyps) {
            ret_string = ret_string.concat(pyp.concentration.getValue() + ", ");
        }
        return ret_string;
    }
}

