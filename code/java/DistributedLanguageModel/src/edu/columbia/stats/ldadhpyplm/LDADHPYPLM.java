/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stats.ldadhpyplm;

import edu.gatsby.nlp.lm.LMIPCProtos.AddRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.AddResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.ErrorResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.MessageType;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import edu.gatsby.nlp.lm.LMIPCProtos.Response;
import edu.gatsby.nlp.lm.LMIPCProtos.TokenListWithCount;
import edu.columbia.nlp.lm.client.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author davidpfau
 */
public class LDADHPYPLM extends Client {

    private Dictionary dict;
    private ArrayList<Document> docs;
    private ArrayList<Document> testDocs;
    private int numTopics;
    private int contextLength;
    private double alpha = 1.0;
    private static String hostname = "localhost";
    private int port = 4041;
    private Add addClient;
    private Sample sampleClient;
    private Remove removeClient;
    private Score scoreClient;
    private GetParameters parameterClient;
    private double llTrue = 0.0; //If the data is artificially generated, the true log likelihood under the generative model is saved along with the rest
    private static Random rn = new Random(System.currentTimeMillis());
    private static final long serialVersionUID = 1;
    
    public Dictionary getDict() {
        return dict;
    }

    public ArrayList<Document> getDocs() {
        return docs;
    }

    public int getNumTopics() {
        return numTopics;
    }

    public LDADHPYPLM(String path, String format, int port, int contextLength, int numTopics, int numDocs, int numTestDocs) {
        this.contextLength = contextLength;
        this.numTopics = numTopics;
        this.port = port;
        if (format.equals("AMI")) {
            dict = readDictionary(path + "ami.dict");
            docs = readDocs(path + "ami_corpus",0,numDocs);
            testDocs = readDocs(path + "ami_corpus",numDocs, numDocs + numTestDocs);
        } else if (format.equals("psychreview")) {
            dict = readDictionary(path + "psychreview.dict");
            ArrayList<Document> allDocs = readDocs(path + "psychreview_corpus",0,1280);
            docs = new ArrayList<Document>();
            testDocs = new ArrayList<Document>();
            Random pseudo = new Random(1); //determinism matters
            for (int i = 0; i < numDocs; i++) {
                int docToAdd = pseudo.nextInt(allDocs.size());
                docs.add(allDocs.get(docToAdd));
                allDocs.remove(docToAdd);
            }
            for (int i = 0; i < numTestDocs; i++) {
                int docToAdd = pseudo.nextInt(allDocs.size());
                testDocs.add(allDocs.get(docToAdd));
                allDocs.remove(docToAdd);
            }
            //docs = readDocs(path + "psychreview_corpus", 1000, 1000 + numDocs);
            //testDocs = readDocs(path + "psychreview_corpus", 1000 + numDocs, 1000 + numDocs + numTestDocs);
            System.out.println(trainingCorpusSize() + " tokens in training corpus.  " + testCorpusSize() + " tokens in test corpus.");
            collapse(1,245,"#");
            System.out.println("Collapsed numbers onto one type.  Dictionary size: " + dict.size());
            stripUnseenWords();
            System.out.println("Collapsed rare words onto one type.  Dictionary size: " + dict.size());
        } else if (format.equals("20newsgroups")) {
            dict = new Dictionary();
            dict.addWord("EOF");
            docs = read20NewsgroupsDocs(path + "/20news-bydate-train",numDocs);
            testDocs = read20NewsgroupsDocs(path + "/20news-bydate-test",numTestDocs);
            System.out.println(trainingCorpusSize() + " tokens in training corpus.  " + testCorpusSize() + " tokens in test corpus.");
            stripUnseenWords();
            System.out.println("Collapsed rare words onto one type.  Dictionary size: " + dict.size());
        } else {
            System.out.println("No known corpus " + format);
            System.exit(-1);
        }
        connect(hostname, port);
        add();

        addClient = new Add();
        sampleClient = new Sample();
        scoreClient = new Score();
        removeClient = new Remove();
        parameterClient = new GetParameters();
    }

    public LDADHPYPLM(int numDocs, int numWords, int numTopics, int wordsInDoc, double alpha, double beta, int contextLength) {
        this.contextLength = contextLength;
        this.numTopics = numTopics;
        this.alpha = alpha;

        dict = new Dictionary();
        docs = LDA.makeData(numDocs, numWords, numTopics, wordsInDoc, alpha, beta);
        for (int i = 0; i < numWords; i++) {
            dict.lookupOrAddIfNotFound(Integer.toString(i));
        }
        connect(hostname, port);
        add();

        addClient = new Add();
        sampleClient = new Sample();
        scoreClient = new Score();
        removeClient = new Remove();
        parameterClient = new GetParameters();
    }
    
    public LDADHPYPLM(int numTopics, int contextLength) {
        this.contextLength = contextLength;
        this.numTopics = numTopics;

        dict = new Dictionary();
        docs = new ArrayList<Document>(100);
        for (int i = 0; i < 10000; i++) {
            dict.lookupOrAddIfNotFound(Integer.toString(i));
        }
        for (int i = 0; i < 100; i++) {
            ArrayList<Integer> tokens = new ArrayList<Integer>(100);
            for (int j = 0; j < 100; j++) {
                tokens.add(j + 100*i);
            }
            docs.add(new Document(tokens, contextLength, numTopics));
         }
        connect(hostname, port);
        add();

        addClient = new Add();
        sampleClient = new Sample();
        scoreClient = new Score();
        removeClient = new Remove();
        parameterClient = new GetParameters();
    }

    //Generates model data by sampling languagel model, then removes observations from language model and replaces it with model data
    public double makeData() {
        connect(hostname, port);
        safeSample(100);
        disconnect();

        double[] topicsPerDocPriorParams = new double[numTopics];
        for (int i = 0; i < numTopics; i++) {
            topicsPerDocPriorParams[i] = alpha;
        }
        DirichletDistrib topicsPerDocPrior = new DirichletDistrib(topicsPerDocPriorParams);
        ArrayList<DiscreteDistrib> topicsPerDoc = new ArrayList<DiscreteDistrib>(docs.size());
        for (int i = 0; i < docs.size(); i++) {
            topicsPerDoc.add(topicsPerDocPrior.sample());
        }

        ArrayList<Document> newDocs = new ArrayList<Document>(docs.size());
        connect(hostname, port);
        Predict predictClient = new Predict();
        for (int i = 0; i < docs.size(); i++) {
            ArrayList<Integer> topics = new ArrayList<Integer>(docs.get(i).size());
            ArrayList<Integer> tokens = new ArrayList<Integer>(docs.get(i).size());
            ArrayList<Integer> context = new ArrayList<Integer>();
            DiscreteDistrib topicDistrib = topicsPerDoc.get(i);
            for (int j = 0; j < docs.get(i).size(); j++) {
                int topic = topicDistrib.sampleIndex() + 1;
                topics.add(topic);
                int token = predictClient.predict(topic, 1, context).get(0);
                tokens.add(token);
                if (context.size() == contextLength) {
                    for (int k = 0; k < contextLength - 1; k++) {
                        context.add(k, context.get(k + 1));
                    }
                    context.add(contextLength - 1, token);
                } else {
                    context.add(token);
                }
            }
            newDocs.add(new Document(tokens, topics, contextLength, numTopics));
        }
        System.out.println("Created model data.");

        ArrayList<Document> oldDocs = docs;
        docs = newDocs;
        llTrue = logLikelihood();

        connect(hostname, port);
        for (Document d : oldDocs) {
            for (int j = 0; j < d.size(); j++) {
                safeRemove(d.getDomain(j), 1, d.getContext(j));
            }
        }
        System.out.println("Removed existing data.");
        add();

        return llTrue;
    }

    public void save(String filename) {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(filename + "_client");
            out = new ObjectOutputStream(fos);
            out.writeObject(this);
            out.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        connect(hostname, port);
        Save saveClient = new Save();
        saveClient.save(filename + "_server");
    }

    public static LDADHPYPLM load(String filename, int port) {
        FileInputStream fis = null;
        ObjectInputStream in = null;
        LDADHPYPLM ret = null;
        try {
            fis = new FileInputStream(filename + "_client");
            in = new ObjectInputStream(fis);
            ret = (LDADHPYPLM) in.readObject();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Load loadClient = new Load();
        loadClient.connect(hostname, port);
        loadClient.load(filename + "_server");

        return ret;
    }

    public void add() {
        try {
            TokenListWithCount.Builder tokenlistBuilder = TokenListWithCount.newBuilder();
            Request.Builder requestBuilder = Request.newBuilder();
            AddRequest.Builder addRequestBuilder = AddRequest.newBuilder();

            int docsAdded = 0;
            int wordsAdded = 0;
            for (Document d : docs) {
                for (int i = 0; i < d.size(); i++) {
                    requestBuilder.setType(MessageType.ADD);
                    addRequestBuilder.setDomain(d.getDomain(i));
                    tokenlistBuilder.clear();
                    tokenlistBuilder.setCount(1);
                    tokenlistBuilder.addAllToken(d.getContext(i));
                    addRequestBuilder.addTuple(tokenlistBuilder.build());

                    requestBuilder.setAdd(addRequestBuilder.build());
                    Request request = requestBuilder.build();
                    streamRequest(request);
                    requestBuilder.clear();
                    addRequestBuilder.clear();
                    wordsAdded++;
                }
                docsAdded++;
                if (docsAdded % 1000 == 0) {
                    System.out.println("Added " + Integer.toString(docsAdded) + " of " + Integer.toString(docs.size()) + " documents to language model");
                }
            }

            requestBuilder.clear();
            requestBuilder.setType(MessageType.DISCONNECT);
            Request request = requestBuilder.build();
            streamRequest(request);
            outputStream.flush();
            cs.shutdownOutput();

            for (int i = 0; i < wordsAdded; i++) {
                Response response = Response.parseFrom(streamResponse());
                AddResponse addResponse = null;
                switch (response.getType()) {
                    case ERROR:
                        ErrorResponse error = response.getError();
                        System.err.println("Server error: " + error.getReason());
                        if (error.hasStackTrace()) {
                            System.err.println("Server stack trace: " + error.getStackTrace());
                        }
                        break;
                    case ADD:
                        addResponse = response.getAdd();
                        break;
                    default:
                        System.err.print("Server Error: response of type " + response.getType() + " returned instead of ADD");
                }
                if (i % 1000 == 0) {
                    System.out.println("Got " + Integer.toString(i) + " responses from server out of " + Integer.toString(wordsAdded));
                }
            }
            parseDisconnect();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Add finished!");
    }

    private Dictionary readDictionary(String path) {
        File f = new File(path);
        FileInputStream fis = null;
        BufferedReader in = null;
        try {
            fis = new FileInputStream(f);
            in = new BufferedReader(new InputStreamReader(fis));
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File " + path + " not found");
            //e.printStackTrace();
            System.exit(-1);
        }

        Dictionary ret = new Dictionary();

        try {
            String line = in.readLine();
            while (line != null) {
                line = line.trim();
                String[] numAndWord = line.split(" ");
                if (numAndWord.length == 1) {
                    ret.addWord(numAndWord[0]); // add a dummy word to keep things in order
                } else {
                    ret.addWord(numAndWord[1]);
                }
                line = in.readLine();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Loaded " + ret.size() + " words into dictionary.");
        return ret;
    }

    private ArrayList<Document> readAMIDocs(String path) {
        return readDocs(path,0,68806);
    }
    
    private ArrayList<Document> readPsychReviewDocs(String path) {
        return readDocs(path,0,1280);
    }
    
    private ArrayList<Document> read20NewsgroupsDocs(String path, int n) {
        Random newRand = new Random(0); //used just to pick the docs, so it is deterministic
        File f = new File(path);
        String[] groups = f.list();

        ArrayList<String> postsInGroups = new ArrayList<String>();
        for (int i = 0; i < groups.length; i++) {
            if (!groups[i].startsWith(".")) {
                f = new File(path + "/" + groups[i]);
                String[] namesInGroup = f.list();
                for (int j = 0; j < namesInGroup.length; j++) {
                    if (!namesInGroup[j].startsWith(".")) {
                        postsInGroups.add("/" + groups[i] + "/" + namesInGroup[j]);
                    }
                }
            }
        }
        
        ArrayList<Document> newDocs = new ArrayList<Document>();
        for (int i = 0; i < n; i++) {
            newDocs.add(readTextFile(path + postsInGroups.get(newRand.nextInt(postsInGroups.size()))));
        }

        return newDocs;
    }

    private Document readTextFile(String path) {
        File f = new File(path);
        FileInputStream fis = null;
        BufferedReader in = null;
        try {
            fis = new FileInputStream(f);
            in = new BufferedReader(new InputStreamReader(fis));
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File " + path + " not found");
            //e.printStackTrace();
            System.exit(-1);
        }
        
        ArrayList<Integer> tokens = new ArrayList<Integer>();
        try {
            String line = null;
            while((line = in.readLine()) != null) {
                String[] tokensInLine = line.split("[^a-zA-Z0-9]"); //split along all non-alphanumeric characters
                for (int i = 0; i < tokensInLine.length; i++) {
                    if(!tokensInLine[i].equals("")) {
                        String token = tokensInLine[i];
                        if(Character.isDigit(token.charAt(0))) {
                            token = "#";
                        }
                        int type = dict.lookupOrAddIfNotFound(token);
                        tokens.add(type);
                    }
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        tokens.add(dict.lookupOrAddIfNotFound("EOF"));
        
        return new Document(tokens,contextLength,numTopics);
    }
    
    private ArrayList<Document> readDocs(String path, int start, int end) {
        File f = new File(path);
        FileInputStream fis = null;
        BufferedReader in = null;
        try {
            fis = new FileInputStream(f);
            in = new BufferedReader(new InputStreamReader(fis));
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File " + path + " not found");
            //e.printStackTrace();
            System.exit(-1);
        }

        try {
            String line = in.readLine();
            String[] tokenStrings = line.split(" ");
            ArrayList<Integer> tokens = new ArrayList<Integer>(tokenStrings.length);
            int eof = dict.lookup("EOF"); //Since AMI is just one big file, each "document" is one line
            int numWordsSoFar = 0;
            LinkedList<Integer> numWordsInDoc = new LinkedList();
            for (String s : tokenStrings) {
                if (s.trim().length() != 0) {
                    int token = Integer.parseInt(s.trim());
                    tokens.add(token);
                    if (token == eof) {
                        numWordsInDoc.add(numWordsSoFar);
                        numWordsSoFar = 0;
                    } else {
                        numWordsSoFar++;
                    }
                }
            }

            ArrayList<Document> ret = new ArrayList<Document>(numWordsInDoc.size());
            ArrayList<Integer> words = new ArrayList<Integer>(numWordsInDoc.removeFirst());
            int totalDocs = numWordsInDoc.size();
            for (Integer i : tokens) {
                if (numWordsInDoc.size() > totalDocs - end) {
                    if (numWordsInDoc.size() <= totalDocs - start) {
                        if (i == eof) {
                            ret.add(new Document(words, contextLength, numTopics));
                            //System.out.println("Created document with " + Integer.toString(words.size()) + " tokens");
                            words = new ArrayList<Integer>(numWordsInDoc.removeFirst());
                        } else {
                            words.add(i);
                        }
                    } else {
                        if (i == eof) {
                            numWordsInDoc.removeFirst();
                        }
                    }
                }
            }
            System.out.println("Created " + Integer.toString(ret.size()) + " documents");

            return ret;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return new ArrayList<Document>();
    }
    
    private void collapse(int start, int end, String type) {
        //Collapses types start to end onto a single type.
        //Used for the Psych Review dataset to reduce all numbers onto a single type
        Dictionary newDict = new Dictionary();
        for (int i = 0; i < start; i ++) {
            newDict.addWord(dict.lookup(i));
        }
        newDict.addWord(type);
        for (int i = end + 1; i < dict.size(); i++) {
            newDict.addWord(dict.lookup(i));
        }
        
        dict = newDict;
        
        for (Document d : docs) {
            for (int i = 0; i < d.size(); i++) {
                int token = d.getToken(i);
                if (token > start) {
                    d.setToken(i,Math.max(start,token+start-end));
                }
            }
        }
        
        for (Document d : testDocs) {
            for (int i = 0; i < d.size(); i++) {
                int token = d.getToken(i);
                if (token > start) {
                    d.setToken(i,Math.max(start,token+start-end));
                }
            }
        }
    }
    
    private void stripUnseenWords() {
        int[] wordCount = new int[dict.size()];
        for (Document d : docs) {
            for (int i = 0; i < d.size(); i++) {
                if (wordCount[d.getToken(i)] < 2) {
                    wordCount[d.getToken(i)]++;
                }
            }
        }
        
        HashMap<Integer,Integer> oldToNewType = new HashMap<Integer,Integer>();
        Dictionary newDict = new Dictionary();
        for (int i = 0; i < dict.size(); i++) {
            if(wordCount[i] < 2) {
                oldToNewType.put(i, newDict.lookupOrAddIfNotFound("?"));
            } else {
                newDict.addWord(dict.lookup(i));
                oldToNewType.put(i, newDict.lookup(dict.lookup(i)));
            }
        }
        
        dict = newDict;
        for (Document d : docs) {
            for (int i = 0; i < d.size(); i++) {
                d.setToken(i, oldToNewType.get(d.getToken(i)));
            }
        }
        
        for (Document d : testDocs) {
            for (int i = 0; i < d.size(); i++) {
                d.setToken(i, oldToNewType.get(d.getToken(i)));
            }
        }
    }

    public void sample(int n) {
        for (int i = 0; i < n; i++) {
            System.out.println("\nSample " + Integer.toString(i+1) + " out of " + n);
            sample();
        }
    }

    public void sample() {
        connect(hostname,port);
        safeSample(1);
        connect(hostname,port);
        for (Document d : docs) {
            //System.out.print("{");
            for (int i = 0; i < d.size(); i++) {
                sampleWord(d, i);
                if (i != d.size() - 1) {
                    //System.out.print(", ");
                }
            }
        }
        disconnect();

        for(int i = 0; i < 10; i++) {
            sampleAlpha();
        }
        //System.out.println("Alpha = " + Double.toString(alpha));
        //System.out.println("LDA-DHPYPLM Log Likelihood = " + Double.toString(logLikelihood()));
    }
    
    public double sampleTestCorpus() {
        connect(hostname,port);
        for (Document d : testDocs) {
            for (int i = 0; i < d.size(); i++) {
                safeAdd(d.getDomain(i), 1, d.getContext(i));
            }
        }
        safeSample(1);
        connect(hostname,port);
        for (Document d : testDocs) {
            for (int i = 0; i < d.size(); i++) {
                sampleWord(d, i);
            }
        }
        double logLike = logLikelihoodTest();
        connect(hostname,port);
        for (Document d : testDocs) {
            for (int i = 0; i < d.size(); i++) {
                safeRemove(d.getDomain(i), 1, d.getContext(i));
            }
        }
        disconnect();
        return logLike;
    }
    
    public void sampleWord(Document d, int i) {
        sampleWord(d, i, d.getNumInDomain());
    }

    public void sampleWord(Document d, int i, int[] numInTopic) {
        ArrayList<Integer> contextAndToken = d.getContext(i);
        safeRemove(d.getDomain(i), 1, contextAndToken);
        d.setDomain(i, -1); // remove observation from count

        double[] score = new double[numTopics];
        double[] cumSum = new double[numTopics];
        for (int j = 0; j < numTopics; j++) {
            score[j] = (numInTopic[j] + alpha) * safeScore(j + 1, contextAndToken);
            if (j == 0) {
                cumSum[j] = score[j];
            } else {
                cumSum[j] = score[j] + cumSum[j - 1];
            }
        }

        int newTopic = 1;
        double sample = Math.random() * cumSum[numTopics - 1];
        for (int j = 0; j < numTopics; j++) {
            if (sample < cumSum[j]) {
                newTopic = j + 1;
                break;
            }
        }

        d.setDomain(i, newTopic);
        //System.out.print(Integer.toString(newTopic));
        safeAdd(newTopic, 1, contextAndToken);
    }

    public void sampleAlpha() {
        //Metropolis sampler for alpha
        double proposal;
        double newAlpha = alpha;
        for (int i = 0; i < 10; i++) {
            proposal = alpha + 0.1 * rn.nextGaussian();
            double llRatio = alpha - proposal;
            for (Document d : docs) {
                for (int t = 1; t <= numTopics; t++) {
                    llRatio += Gamma.logGamma(d.getNumInDomain(t) + proposal) - Gamma.logGamma(d.getNumInDomain(t) + alpha);
                }
                llRatio -= Gamma.logGamma(d.size() + numTopics * proposal) - Gamma.logGamma(d.size() + numTopics * alpha);
            }
            llRatio += docs.size() * (Gamma.logGamma(numTopics * proposal) - Gamma.logGamma(numTopics * alpha));
            llRatio -= docs.size() * numTopics * (Gamma.logGamma(proposal) - Gamma.logGamma(alpha));
            if (llRatio > Math.log(rn.nextDouble())) {
                newAlpha = proposal;
            }
        }
        alpha = newAlpha;
    }

    public double logLikelihood() {
        double logLike = 0.0;
        connect(hostname, port);
        for (Document d : docs) {
            for (int i = 0; i < d.size(); i++) {
                logLike += Math.log(safeScore(d.getDomain(i), d.getContext(i)));
            }
        }
        /*Parameters params = new Parameters(parameterClient.getParameters(), contextLength, numTopics);
        double paramSum = 0.0;
        for (int i = 0; i <= contextLength; i++) {
            for (int t = 0; t <= numTopics; t++) {
                paramSum += params.concentration[t][i];
            }
            paramSum += params.switchConcentration[i];
        }
        System.out.println(paramSum);*/
        disconnect();
        return logLike;
    }
    
    public double logLikelihoodTest() {
        double logLike = 0.0;
        connect(hostname, port);
        for (Document d : testDocs) {
            for (int i = 0; i < d.size(); i++) {
                logLike += Math.log(safeScore(d.getDomain(i), d.getContext(i)));
            }
        }
        disconnect();
        return logLike;
    }
    
    public void readTestDocs(String path, String format, int numDocs) {
        if(format.equals("AMI")) {
            testDocs = readDocs(path + "ami_corpus",docs.size(),docs.size()+numDocs);
        }
    }
    
    public double bitsPerWord(int n, int m) {
        double[][] logLikelihoods = new double[n][m];
        for (int i = 0; i < n; i++) {
            sample();
            for (int j = 0; j < m; j++) {
                logLikelihoods[i][j] = sampleTestCorpus();
            }
        }
        return -avgmin(logLikelihoods)/(Math.log(2) * testCorpusSize());
    }
    
    // takes the minimum of each sub-array and returns the average of exp of those minima
    public double avgmin(double[][] input) {
        int max = 0; // index of the subarray with the greatest minimum
        int[] min = new int[input.length]; // array of the index of the minimum for each sub-array
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                if (input[i][j] < input[i][min[i]]) {
                    min[i] = j;
                }
            }
            if (input[i][min[i]] > input[max][min[max]]) {
                max = i;
            }
        }
        double expSum = 0.0;
        for (int i = 0; i < input.length; i++) {
            expSum += Math.exp(input[i][min[i]] - input[max][min[max]]);
        }
        return input[max][min[max]] + Math.log(expSum/input.length);
    }
    
    // Wrong! Do not use!
    /*public double bitsPerWord() {
        double logLoss = 0.0;
        connect(hostname,port);
        for (Document d : testDocs) {
            //System.out.print("{");
            for (int i = 0; i < d.size(); i++) {
                ArrayList<Integer> context = d.getContext(i);
                addClient.add(d.getDomain(i), 1, context);
                sampleWord(d,i);
                if (i != d.size() - 1) {
                    //System.out.print(", ");
                }
                logLoss += Math.log(scoreClient.score(d.getDomain(i), context));
            }
            //System.out.println("}");
            sampleClient.sample(1);
        }
        
        for (Document d : testDocs) {
            for (int i = 0; i < d.size(); i++) {
                removeClient.remove(d.getDomain(i), 1, d.getContext(i));
            }
        }
        disconnect();
        return -logLoss/(Math.log(2.0) * testCorpusSize());
    }*/
    
    //implements left-to-right algorithm of Wallach '09 with r particles
    public double leftToRightBitsPerWord(int R) {
        System.out.println("Work, fucker.");
        double[] probOfWord = new double[testCorpusSize()];
        connect(hostname,port);
        for (int r = 0; r < R; r++) {
            System.out.println("Sampling particle " + r + " out of " + R);
            int ctr = 0; // total number of tokens iterated over, in all documents
            for (Document d : testDocs) {
                int[] numInTopicPerDoc = new int[numTopics]; // number of times that topic appears in this document
                for (int i = 0; i < d.size(); i++) {
                    for (int j = 0; j < i; j++) {
                        numInTopicPerDoc[d.getDomain(j)-1]--;
                        sampleWord(d,j,numInTopicPerDoc);
                        numInTopicPerDoc[d.getDomain(j)-1]++;
                    }
                    for (int t = 0; t < numTopics; t++) {
                        probOfWord[ctr] += (safeScore(t+1, d.getContext(i)))
                                          *((numInTopicPerDoc[t] + alpha)
                                           /(i + numTopics*alpha));
                    }
                    safeAdd(d.getDomain(i), 1, d.getContext(i));
                    sampleWord(d,i,numInTopicPerDoc);
                    numInTopicPerDoc[d.getDomain(i)-1]++;
                    ctr++;
                    System.out.println("Sampled token " + ctr);
                }
            }
            
            //clear all tokens from the test corpus and start again
            connect(hostname,port);
            for (Document d : testDocs) {
                for (int i = 0; i < d.size(); i++) {
                    safeRemove(d.getDomain(i), 1, d.getContext(i));
                }
            }
        }
        disconnect();
        
        double logLike = -testCorpusSize()*Math.log(R); //factor introduced by taking the average, rather than sum, of probabilities of words
        for (int i = 0; i < testCorpusSize(); i++) {
            logLike += Math.log(probOfWord[i]);
        }
        return -logLike/(Math.log(2) * testCorpusSize());
    }
    
    // Wrong! Do not use!
    //Rather than sampling the language-topic model then the bits per word, 
    //interleaves sampling the state of the training data and the probability
    //of the observations, then averages the probabilities to calculate the 
    //perplexity (in expectation over the hidden states).  The probability is
    //calculated n times.
    /*public double bitsPerWord(int n) {
        double[][] logScores = new double[testCorpusSize()][n];
        for (int k = 0; k < n; k++) {
            System.out.println("\nCalculating LDA-DHPYPLM bits-per-word: Iteration " + Integer.toString(k+1) + "/" + n);
            sample(5);
            int ctr = 0;
            for (Document d : testDocs) {
                connect(hostname, port);
                for (int i = 0; i < d.size(); i++) {
                    ArrayList<Integer> context = d.getContext(i);
                    addClient.add(d.getDomain(i), 1, context);
                    sampleWord(d, i);
                    logScores[ctr][k] = Math.log(scoreClient.score(d.getDomain(i), context));
                    ctr++;
                }
                sampleClient.sample(1);
            }

            connect(hostname, port);
            for (Document d : testDocs) {
                for (int i = 0; i < d.size(); i++) {
                    removeClient.remove(d.getDomain(i), 1, d.getContext(i));
                }
            }
            disconnect();
        }
        
        double logLoss = 0.0;
        for (int i = 0; i < testCorpusSize(); i++) {
            double totalLogScore = 0.0;
            for (int k = 0; k < n; k++) {
                totalLogScore += logScores[i][k];
            }
            logLoss += totalLogScore/n;
        }
        return -logLoss / (Math.log(2.0) * testCorpusSize());
    }*/

    public double llTrue() {
        return llTrue;
    }

    /*public void testScore() {
        //connect(hostname,port);
        //sampleClient.sample(20);
        double cumSum = 0.0;
        ArrayList<Integer> context = docs.get(0).getContext(2);
        int domain = docs.get(0).getDomain(2);
        System.out.println("Token: " + docs.get(0).getToken(2));
        connect(hostname, port);
        for (int i = 0; i < dict.size(); i++) {
            context.set(2, i);
            double score = scoreClient.score(domain, context);
            cumSum += score;
            System.out.println(Double.toString(score));
        }

        scoreClient.disconnectAndParse();
        System.out.println("Sum: " + cumSum);
    }*/
    
    public ArrayList<Document> getTestDocs(){
        return testDocs;
    }
    
    public int trainingCorpusSize() {
        int size = 0;
        for (Document d : docs) {
            size += d.size();
        }
        return size;
    }

    public int testCorpusSize() {
        int size = 0;
        for (Document d : testDocs) {
            size += d.size();
        }
        return size;
    }
    
    public void clear() {
        connect(hostname,port);
        for (Document d: docs) {
            for (int i = 0; i < d.size(); i++) {
                safeRemove(d.getDomain(i),1,d.getContext(i));
            }
        }
        removeClient.disconnectAndParse();
    }
    
    public double domainFreeBitsPerWord() {
        double logLike = 0.0;
        connect(hostname,port);
        for (Document d : testDocs) {
            for (int i = 0; i < d.size(); i++) {
                logLike += Math.log(safeScore(0, d.getContext(i)));
            }
        }
        disconnect();
        return -logLike/(Math.log(2)*testCorpusSize());
    }
    
    public void safeRemove(int domain, int count, ArrayList<Integer> contextAndToken) {
        boolean complete = false;
        while(!complete) {
            try {
                removeClient.remove(domain, count, contextAndToken);
                complete = true;
            } catch (Exception e) {
                System.err.println("Failed to remove properly.  Reconnecting...");
                disconnect();
                connect(hostname, port);

            }
        }
    }
    
    public void safeAdd(int domain, int count, ArrayList<Integer> contextAndToken) {
        boolean complete = false;
        while(!complete) {
            try {
                addClient.add(domain, count, contextAndToken);
                complete = true;
            } catch (Exception e) {
                System.err.println("Failed to add properly.  Reconnecting...");
                disconnect();
                connect(hostname, port);

            }
        }
    }
    
    public double safeScore(int domain, ArrayList<Integer> contextAndToken) {
        while(true) {
            try {
                return scoreClient.score(domain, contextAndToken);
            } catch (Exception e) {
                System.err.println("Failed to score properly.  Reconnecting...");
                disconnect();
                connect(hostname, port);

            }
        }
    }
    
    public void safeSample(int n) {
        boolean complete = false;
        while(!complete) {
            try {
                sampleClient.sample(n);
                complete = true;
            } catch (Exception e) {
                System.err.println("Failed to sample properly.  Reconnecting...");
                disconnect();
                connect(hostname, port);

            }
        }
    }
    
    // collapses everything onto domain 0
    public void noTopics() {
        for (Document d : docs) {
            for (int i = 0; i < d.size(); i++) {
                d.setDomain(i, 0);
            }
        }
        for (Document d : testDocs) {
            for (int i = 0; i < d.size(); i++) {
                d.setDomain(i, 0);
            }
        }
    }
    
    public static void main(String[] args) {
        String usage = "<corpus format> <corpus path> <port> <context length> <# topics> <# training docs> <# test docs>";
        if (args.length != 7) {
            System.err.println("Improperly formated arguments: " + usage);
            System.exit(-1);
        }
        
        String format = args[0];
        String path   = args[1];
        
        int port            = 4040;
        int contextLength   = 2;
        int numTopics       = 10;
        int numTrainingDocs = 100;
        int numTestDocs     = 50;
        
        try {
            port            = Integer.parseInt(args[2]);
            contextLength   = Integer.parseInt(args[3]);
            numTopics       = Integer.parseInt(args[4]);
            numTrainingDocs = Integer.parseInt(args[5]);
            numTestDocs     = Integer.parseInt(args[6]);
        } catch (Exception e) {
            System.err.println("Port number, context length, number of topics, traning documents, or testing documents is not an integer");
            System.err.println(usage);
        }
        
        //LDADHPYPLM foo = new LDADHPYPLM(path, format, port, contextLength, numTopics, numTrainingDocs, numTestDocs);
        //LDA bar = new LDA(foo.getDict(), foo.getDocs(), foo.getTestDocs(), numTopics);
        
        //foo.sample(Math.min((140-numTopics)/2,50));
        LDADHPYPLM foo = LDADHPYPLM.load("/Users/davidpfau/Documents/Wood Group/run5/"+numTopics+"_topic/"+numTopics+"_topic/LDA-DHPYPLM_"+format+"_"+numTopics+"_topics_100_train_50test", port);
        port = 3000; // switch to new server
        foo.noTopics();
        foo.add();
        foo.connect(hostname,port);
        foo.safeSample(500);
        foo.disconnect();
        System.out.println("HPYPLM bits per word: " + Double.toString(-foo.logLikelihoodTest()/(Math.log(2)*foo.testCorpusSize())));
        //double foo_bpw = foo.leftToRightBitsPerWord(3);
        //System.out.println("---------------------------");
        //System.out.println("Bits Per Word: " + foo_bpw);
        //System.out.println("---------------------------");
        //System.out.println("Bits Per Word, 5 topics, 20 newsgroups: " + foo.leftToRightBitsPerWord(5));
        //foo = LDADHPYPLM.load("/Users/davidpfau/Documents/Wood Group/run5/" + ntopics[i] + "_topic/" + ntopics[i] + "_topic/LDA-DHPYPLM_psychreview_" + ntopics[i] + "_topics_100_train_50test", 4040+ntopics[i]);

        //LDADHPYPLM foo = LDADHPYPLM.load("psychreview_unsampled",port);
        /*LDA[] bars = new LDA[9];
        double[] evidence = new double[9];
        int[] nTopics = {120,100,80,60,40,20,10,5,1};
        for (int i = 0; i < 9; i++) {
            bars[i] = new LDA(foo.getDict(), foo.getDocs(), foo.getTestDocs(), nTopics[i]);
            bars[i].sample(50);
            evidence[i] = bars[i].evidence(100);
        }
        
        for (int i = 0; i < 9; i++) {
            System.out.println("LDA evidence for " + nTopics[i] + " topics: " + evidence[i]);
        }*/
        
        /*bar.sample(200);
        bar.sampleTestCorpus(200);
        for (int i = 0; i < 20; i++) {
            System.out.println("\nTop Types In Topic " + i + ":");
            bar.printTopic(i,20);
        }*/

        /*foo.sample(50);
        bar.sample(100);
        bar.sampleTestCorpus(100);
        
        double[] fooBitsPerWord = new double[3];
        double[] barBitsPerWord = new double[30];
        
        for (int i = 0; i < 30; i++) {
            fooBitsPerWord[i] = foo.bitsPerWord(20,20);
            barBitsPerWord[i] = bar.bitsPerWord(30,30);
            System.out.println("\n---------------------------------------");
            System.out.println("LDA-DHPYPLM bits per word: " + fooBitsPerWord[i]);
            System.out.println("LDA bits per word: "         + barBitsPerWord[i]);
            System.out.println("---------------------------------------\n");
        }
        
        foo.save("LDA-DHPYPLM_" + format + "_" + numTopics + "_topics_" + numTrainingDocs + "_train_" + numTestDocs + "test");
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream("LDA_" + format + "_" + numTopics + "_topics_" + numTrainingDocs + "_train_" + numTestDocs + "test");
            out = new ObjectOutputStream(fos);
            out.writeObject(bar);
            out.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Error saving LDA");
            System.exit(-1);
        }
        
        for (int i = 0; i < 30; i++) {
            System.out.println("\nLDA-DHPYPLM bits per word: " + fooBitsPerWord[i]);
            System.out.println("LDA bits per word: "         + barBitsPerWord[i]);
        }
        
        System.out.println("\nI ran, I saved, I conquered.");*/
    }
}
