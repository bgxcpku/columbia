/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import edu.columbia.stat.wood.util.ByteDiscreteDistribution;

/**
 *
 * @author nicholasbartlett
 */

public interface ByteSequenceMemoizerInterface {
    
    public void limitMemory(long maxNumberRestaurants, long maxSequenceLength);

    public double continueSequence(byte type);

    public double continueSequence(byte[] types);

    public byte[] generate(byte[] context, int numSamples);

    public byte[] generateSequence(byte[] context, int sequenceLength);

    public ByteDiscreteDistribution predictiveDistribution(byte[] context);

    public double predictiveProbability(byte[] context, byte token);

    public double sequenceProbability(byte[] context , byte[] sequence);

    public void sampleSeatingArrangements(int numSweeps);

    public double sampleDiscounts(int numSweeps);

    public double score();

    public SequenceMemoizerParameters getParameters();
}
