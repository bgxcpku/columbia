/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 
 */

public abstract class HPYPAbstract implements HPYPInterface {

    public double continueSequence(int[] observations) {
        double logLik;

        logLik = 0.0;

        for (int obs : observations) {
            logLik += continueSequence(obs);
        }

        return logLik;
    }

    public int[] generateSequence(Sequence initialContext, int sequenceLength){
        int[] generatedSequence;
        int generatedToken;
        Sequence context;

        generatedSequence = new int[sequenceLength];
        context = initialContext;

        for(int i = 0; i < sequenceLength; i++){
            generatedToken = generate(context, 1)[0];
            generatedSequence[i] = generatedToken;
            context.add(generatedToken);
        }

        return generatedSequence;
    }
}
