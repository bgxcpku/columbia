/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.pdia;

import edu.columbia.stat.wood.hpyp.HPYP;
import edu.columbia.stat.wood.util.MersenneTwisterFast;
import edu.columbia.stat.wood.util.MutableDouble;
import edu.columbia.stat.wood.util.MutableInt;
import edu.columbia.stat.wood.util.SampleWithoutReplacement;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.math.special.Gamma;

/**
 *
 * @author nicholasbartlett
 */
public class PDIA {

    private MersenneTwisterFast RNG = new MersenneTwisterFast(17);

    private int[][] trainingData;
    private int alphabetSize;
    private int[][] testData;

    //parameters
    private HashMap<IntPair, Integer> delta = new HashMap<IntPair, Integer>(10000);
    private TIntObjectHashMap<int[]> piCounts;

    private HPYP hpyp = new HPYP(1,new MutableDouble[]{new MutableDouble(.5), new MutableDouble(.5)}, new MutableDouble[]{new MutableDouble(10), new MutableDouble(5)}, new IntGeometricDistribution(.01));
    private double beta = 10;

    public void fillPiCounts(){
        int[] counts;
        piCounts = new TIntObjectHashMap<int[]>(10000);

        for (int line = 0; line < trainingData.length; line++){
            int currentState = 0;
            for (int emission : trainingData[line]){
                counts = piCounts.get(currentState);
                if (counts == null) {
                    counts = new int[alphabetSize];
                    counts[emission] = 1;
                    piCounts.put(currentState, counts);
                } else {
                    counts[emission]++;
                }
                currentState = deltaGet(new IntPair(currentState,emission));
            }
        }
    }

    public int deltaGet(IntPair key){
        Integer value = delta.get(key);

        if(value != null){
            return value.intValue();
        } else {
            value = hpyp.draw(new int[]{key.second()});
            delta.put(key, value);
            return value;
        }
    }

    public double dataLogLikelihood(){
        fillPiCounts();

        TIntObjectIterator<int[]> iterator = piCounts.iterator();
        double logLik = 0.0;

        double logGammaBeta  = Gamma.logGamma(beta);
        double logGammaBeta_ = Gamma.logGamma(beta / (double) alphabetSize);
        
        int[] counts;
        int sum;
        while(iterator.hasNext()){
            iterator.advance();

            counts = iterator.value();
            sum = 0;
            for(int c : counts){
                sum += c;
                logLik += Gamma.logGamma(beta / (double) alphabetSize + (double) c);
            }

            logLik -= Gamma.logGamma(beta + sum) + (double) alphabetSize * logGammaBeta_;
            logLik += logGammaBeta;
        }

        return logLik;
    }

    public void sampleDelta(){
        int[] randomOrder = SampleWithoutReplacement.sampleWithoutReplacement(delta.size(),RNG);
        IntPair[] keys = new IntPair[delta.size()];

        int i = 0;
        for(IntPair pair : delta.keySet()){
            keys[randomOrder[i++]] = pair.copy();
        }

        for(IntPair key : keys){
            sampleDeltaEntry(key);
            cleanOutDelta();
        }
        check();
    }

    public void cleanOutDelta(){

        HashSet<IntPair> keysNotInUse= new HashSet<IntPair>();
        for(IntPair key : delta.keySet()){
            keysNotInUse.add(key.copy());
        }
        
        for (int line = 0; line < trainingData.length; line++){
            Integer currentState = 0;
            for (int emission : trainingData[line]){
                IntPair key = new IntPair(currentState, emission);
                currentState = delta.get(key);
                keysNotInUse.remove(key);
            }
        }

        for(IntPair key : keysNotInUse){
            hpyp.unseat(new int[]{key.second()}, delta.get(key));
            delta.remove(key);
        }
    }

    public void sampleDeltaEntry(IntPair key){
        Integer value = delta.get(key);

        if(value == null){
            //means no longer contributes to the likelihood so no need to sample it
            return;
        } else {
            double dll1 = dataLogLikelihood();

            int[] context = new int[]{key.second()};
            hpyp.unseat(context, value);

            Integer suggestedValue = hpyp.draw(context);
            delta.put(key,suggestedValue);

            double dll2 = dataLogLikelihood();

            if(Math.exp(dll2 - dll1) > RNG.nextDouble()){
                return;
            } else {
                hpyp.unseat(context, suggestedValue);
                hpyp.seat(context,value);
                delta.put(key, value);
            }
        }
    }

    public void sampleBeta() {
        double dll1 = dataLogLikelihood();
        double old_beta = beta;
        beta = beta + RNG.nextGaussian();
        if ( beta < 0 ) {
            beta = old_beta;
        } else {
            double dll2 = dataLogLikelihood();
            if ( (dll2 - beta) - (dll1 - old_beta) < Math.log(RNG.nextDouble())) {
                beta = old_beta;
            }
        }
    }

    public void sample(){
        sampleDelta();
        sampleBeta(); // To do - speed this by only calculating data log likelihood when needed
        for (int i = 0; i < 10; i++) {
            hpyp.sampleHyperParams(0.07, .7);
            hpyp.sampleSeatingArrangments();
        }
    }

    public void check(){
        HashMap<Integer, MutableInt> countMap = new HashMap<Integer,MutableInt>();
        for(IntPair key : delta.keySet()){
            MutableInt value = countMap.get(key.second());
            if(value == null){
                value = new MutableInt(0);
                countMap.put(key.second(),value);
            }
            value.increment();
        }

        for(int key : hpyp.ecr.keys()){
            assert hpyp.ecr.get(key).customers() == countMap.get(key).value();
        }
    }

    public double[][] ppTest() {
        fillPiCounts();

        double[][] pp = new double[testData.length][];

        for (int line = 0; line < testData.length; line++) {
            int currentState = 0;
            pp[line] = new double[testData[line].length];
            int emissionInd = 0;
            for (int emission : testData[line]) {
                int[] cnts = piCounts.get(currentState);
                if (cnts == null) {
                    cnts = new int[alphabetSize];
                    piCounts.put(currentState, cnts);
                }
                int total = 0;
                for (int c : cnts) {
                    total += c;
                }
                pp[line][emissionInd++] = ((double) cnts[emission] + (beta / (double) alphabetSize)) / ((double) total + beta);
                cnts[emission]++;

                currentState = deltaGet(new IntPair(currentState, emission));
            }
        }
        return pp;
    }

    public double[][] ppTest(int samples, int passes, int jump, String filePrefix){
        double[][] pp = null;

        for(int s = 0; s < samples; s++){
            System.out.println("sample = " + s*jump);
            for (int j = 0; j < jump; j++) {
                sample();
            }
            try {
                File file = new File(filePrefix + s*jump + ".sample");
                FileWriter fw = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fw);
                out.write(toString());
            } catch (IOException e) {
                System.err.println("Couldn't write PDIA Params.");
                e.printStackTrace();
            }
            for(int p = 0; p < passes; p++){
                if(p == 0 && s == 0){
                    pp = ppTest();
                } else {
                    double[][] addpp = ppTest();
                    for(int i = 0; i < testData.length; i++){
                        for(int j = 0; j < testData[i].length; j++){
                            pp[i][j] += addpp[i][j];
                        }
                    }
                }
                cleanOutDelta();
            }
        }
        
        for (int i = 0; i < testData.length; i++) {
            for (int j = 0; j < testData[i].length; j++) {
                pp[i][j] /= (double) samples * (double) passes;
            }
        }

        return pp;
    }

    public double scoreTest(int samples, int passes, int jump, String filePrefix){
        double[][] pp = ppTest(samples, passes, jump, filePrefix);

        double ll = 0.0;
        int total = 0;
        for (int i = 0; i < testData.length; i++) {
            for (int j = 0; j < testData[i].length; j++) {
                ll += Math.log(pp[i][j]);
                total++;
            }
        }

        ll *= -1;
        ll /= Math.log(2);
        ll /= (double) total;
        return ll;
    }

    public double score(){
        return dataLogLikelihood() + hpyp.score();
    }

    @Override
    public String toString() {
        String toStr = "Beta: " + beta + "\n";
        toStr += "Root Restaurant:\n" + hpyp.get(new int[]{}).toString();
        for (int i = 0; i < alphabetSize; i++) {
            toStr += "Restaurant " + i + ":\n" + hpyp.get(new int[]{i}).toString();
        }
        toStr += "Delta:\n";
        for ( IntPair pair : delta.keySet() ) {
            toStr += pair.toString() + " -> " + delta.get(pair) + "\n";
        }
        return toStr;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException{
        //Read in the data to trainingData int array
        String path = args[0];
        if (path.charAt(path.length() - 1) != '/') {
            path = path + "/";
        }
        File f = new File(path + "aiw.train");
        File g = new File(path + "aiw.test");

        BufferedReader br = null;
        THashMap<Character,Integer> dictionary = new THashMap<Character,Integer>();
        int[][] trainingData = new int[100][];
        int[][] testData = new int[50][];

        try{
            br = new BufferedReader(new FileReader(f));

            String line;
            int l = 0;
            Integer s;

            while((line = br.readLine()) != null){
                trainingData[l++] = new int[line.length()];

                for(int i = 0; i < line.length(); i++){
                    s = dictionary.get(line.charAt(i));
                    if(s == null){
                        s = dictionary.size();
                        dictionary.put(line.charAt(i),s);
                    }
                    trainingData[l - 1][i] = s;
                }
            }

            br.close();

            br = new BufferedReader(new FileReader(g));
            l = 0;

            while((line = br.readLine()) != null){
                testData[l++] = new int[line.length()];

                for(int i = 0; i < line.length(); i++){
                    s = dictionary.get(line.charAt(i));
                    if(s == null){
                        s = dictionary.size();
                        dictionary.put(line.charAt(i),s);
                    }
                    testData[l - 1][i] = s;
                }
            }
        } finally {
            br.close();
        }

        //Set up PDIA
        PDIA pdia = new PDIA();
        pdia.trainingData = trainingData;
        pdia.testData = testData;
        pdia.alphabetSize = 27;
        pdia.fillPiCounts();


        System.out.println(pdia.piCounts.size());

        for (int i = 0; i < 200; i++){
            pdia.sample(); // burn in
            System.out.println("Burn-in sample " + i);
        }
        for(int i = 0; i < 100; i++){
            System.out.print(pdia.scoreTest(10,10,10,path+"aiw"));
            System.out.print(", ");
            System.out.print(pdia.piCounts.size());
            System.out.print(", ");
            System.out.println(pdia.score());
        }
    }
}
