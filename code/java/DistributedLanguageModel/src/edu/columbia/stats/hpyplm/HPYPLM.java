/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hpyplm;

import edu.columbia.nlp.lm.client.Client;
import edu.columbia.nlp.lm.client.Add;
import edu.columbia.nlp.lm.client.Sample;
import edu.columbia.nlp.lm.client.Score;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author davidpfau
 */
public class HPYPLM extends Client {

    public ArrayList<ArrayList<Integer>> train = new ArrayList<ArrayList<Integer>>();
    public ArrayList<ArrayList<Integer>> test  = new ArrayList<ArrayList<Integer>>();
    public ArrayList<Integer> testContext = new ArrayList<Integer>(); // if we break the data between test and train in the middle of a string, remember the context for the test data
    public HashMap<Character,Integer> alphabet = new HashMap<Character,Integer>();
    public int contextLength;
    public int numSymbols = 0;
    public String hostname = "localhost";
    public int port = 4041;
    public Sample sampleClient;
    private Score scoreClient;

    public HPYPLM(String hostname, int port, String path, int contextLength, int trainLength) {
            Add addClient = new Add();
            sampleClient = new Sample();
            scoreClient = new Score();
            this.contextLength = contextLength;
            this.hostname = hostname;
            this.port = port;

            File f = new File(path);
            FileInputStream fis = null;
            BufferedReader in = null;
            try {
                fis = new FileInputStream(f);
                in = new BufferedReader(new InputStreamReader(fis));
            } catch (java.io.IOException e) {
                System.err.println("Cannot read file " + path);
                e.printStackTrace();
                System.exit(-1);
            }

            connect(hostname,port);
            try {
                String line = null;
                int ctr = 0;
                boolean inTrain = true;
                while ((line = in.readLine()) != null) {
                    ArrayList<Integer> data = new ArrayList<Integer>();
                    ArrayList<Integer> context = new ArrayList<Integer>();
                    for (int i = 0; i < line.length(); i++) {
                        if (alphabet.containsKey(line.charAt(i))) {
                            data.add(alphabet.get(line.charAt(i)));
                            context.add(alphabet.get(line.charAt(i)));
                        } else {
                            data.add(numSymbols);
                            context.add(numSymbols);
                            alphabet.put(line.charAt(i), numSymbols);
                            numSymbols++;
                        }
                        if (context.size() > contextLength + 1) {
                            ArrayList<Integer> newContext = new ArrayList<Integer>();
                            for (int j = 1; j < context.size(); j++) {
                                newContext.add(context.get(j));
                            }
                            context = newContext;
                        }
                        if (inTrain) {
                            addClient.add(0,1,context);
                        }
                        ctr++;
                        if (ctr == trainLength) {
                            inTrain = false;
                            train.add(data);
                            data = new ArrayList<Integer>();
                            testContext = context;
                            disconnect();
                        }
                    }
                    if (inTrain) {
                        train.add(data);
                    } else {
                        test.add(data);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading data file!");
                e.printStackTrace();
                System.exit(-1);
            }
    }

    public double scoreTest() {
        double logLike = 0.0;
        double testLength = 0.0;
        ArrayList<Integer> context;
        connect(hostname,port);
        for (int i = 0; i < test.size(); i++) {
            testLength += test.get(i).size();
            if (i == 0) {
                context = testContext;
            } else {
                context = new ArrayList<Integer>();
            }
            for (int j = 0; j < test.get(i).size(); j++) {
                context.add(test.get(i).get(j));
                if (context.size() > contextLength + 1) {
                    ArrayList<Integer> newContext = new ArrayList<Integer>();
                    for (int k = 1; k < context.size(); k++) {
                        newContext.add(context.get(k));
                    }
                    context = newContext;
                }
                double score = scoreClient.score(0, context);
                logLike += Math.log(score);
            }
        }
        disconnect();
        return -logLike/(Math.log(2)*testLength);
    }

    public static void main(String[] args) {
            String hostname = "localhost";
            String path = args[0]; // path to the data file
            int port = 4041;
            int contextLength = 0;
            int trainLength = 10000;
            try {
                contextLength = Integer.parseInt(args[1]);
                port = Integer.parseInt(args[2]);
                trainLength = Integer.parseInt(args[3]);
            } catch (Exception e) {
                System.err.println("Could not cast 2nd or 3rd argment to integer");
                e.printStackTrace();
                System.exit(-1);
            }
            HPYPLM hpyplm = new HPYPLM(hostname,port,path,contextLength,trainLength);
            hpyplm.connect(hostname, port);
            hpyplm.sampleClient.sample(400);
            hpyplm.disconnect();
            System.out.println("Test bits per character: " + hpyplm.scoreTest());
    }

}
