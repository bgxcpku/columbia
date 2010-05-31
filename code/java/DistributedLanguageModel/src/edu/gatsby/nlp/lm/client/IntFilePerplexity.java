/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package edu.gatsby.nlp.lm.client;

import com.google.protobuf.CodedInputStream;
import domainadaptation.SuffixTree;
import edu.gatsby.nlp.lm.LMIPCProtos.ErrorResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import edu.gatsby.nlp.lm.LMIPCProtos.Response;
import edu.gatsby.nlp.lm.LMIPCProtos.MessageType;
import edu.gatsby.nlp.lm.LMIPCProtos.ScoreRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.ScoreResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.TokenList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

/**
*
* @author fwood
*/
public class IntFilePerplexity extends Client {

    static boolean DEBUG = false;
    String fileName;
    Vector<Integer> tupleCounts = new Vector<Integer>();
    Vector<Request> scoreRequests = new Vector<Request>();
    Request disconnectRequest;

    public IntFilePerplexity(String fileName, int n, int domain) throws FileNotFoundException, IOException {
        this.fileName = fileName;
        this.n = n;
        BufferedReader lineReader = null;

        lineReader = new BufferedReader(new FileReader(fileName));

        // every thousand or so tuples, package up the request and send it off to the server

        String line = "";
        int lineNumber = 0;

        while ((line = lineReader.readLine()) != null) {
            lineNumber++;
            StringTokenizer strtok = new StringTokenizer(line);
            if (strtok.countTokens() != n + 1) {
                continue;
            } else {
                try {
                    ArrayList<Integer> contextPlusToken = new ArrayList<Integer>(n - 2);
                    Integer count = Integer.valueOf(strtok.nextToken());
                    tupleCounts.add(count);
                    for (int i = 0; i < n - 1; i++) {
                        contextPlusToken.add(Integer.valueOf(strtok.nextToken()));
                    }
                    TokenList.Builder tokenlistBuilder = TokenList.newBuilder();

                    tokenlistBuilder.addAllToken(contextPlusToken);
                    TokenList tokenList = tokenlistBuilder.build();
                    ScoreRequest.Builder scoreRequestBuilder = ScoreRequest.newBuilder();

                    scoreRequestBuilder.setDomain(domain);
                    scoreRequestBuilder.setContext(tokenList);
                    int type = Integer.valueOf(strtok.nextToken());
                    scoreRequestBuilder.addType(type);
                    Request.Builder requestBuilder = Request.newBuilder();
                    requestBuilder.setType(MessageType.SCORE);

                    requestBuilder.setScore(scoreRequestBuilder.build());
                    scoreRequests.add(requestBuilder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Client Error: malformed line (" + lineNumber + ") in " + fileName);
                }
            }
        }
        Request.Builder requestBuilder = Request.newBuilder();

        requestBuilder.clear();
        requestBuilder.setType(MessageType.DISCONNECT);
        disconnectRequest = requestBuilder.build();

    }

    public void partialPerplexity(int[] document, double[] partial_perplexities) {
    }
    int n = 3;
    int domain = 1;

    public static void main(String args[]) {
        String usage = "<server> <port> <n of n-gram> <domain> <filename> <samples>";
        if (args.length < 6) {
            System.err.println(usage);
            System.exit(-1);
        }

        Sample sampleClient = new Sample();

        String hostname = args[0];

        int port = 4040;
        int domain = 1;
        int samples = 1;
        String filename = args[4];
        int n = 3;
        try {
            port = Integer.valueOf(args[1]);
            n = Integer.valueOf(args[2]);
            domain = Integer.valueOf(args[3]);
            samples = Integer.valueOf(args[5]);
        } catch (Exception e) {
            System.err.println("Usage Error: port, n of n-gram, or domain is not an integer");
            System.err.println(usage);
            System.exit(-1);
        }
        IntFilePerplexity client = null;
        try {
            client = new IntFilePerplexity(filename, n, domain);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }


        client.connect(hostname, port);
        try {
            double[] partialPerplexity = new double[client.tupleCounts.size()];
            double averagePerplexity = 0.0;
            for (int s = 0; s <= samples; s++) {
                int totalCount = 0;
                for (int b = 0; b < client.tupleCounts.size(); b += 1000) {
                    client.connect(hostname, port);
                    int upper_lim = (b + 1000 > client.tupleCounts.size() ? client.tupleCounts.size() : (b + 1000));

                    client.partialPerplexity(partialPerplexity, b,upper_lim);
                    client.disconnect();


                    for (int i = b; i < upper_lim; i++) {
                        averagePerplexity += client.tupleCounts.get(i) * Math.log(partialPerplexity[i] / (double) (s + 1));
                        totalCount += client.tupleCounts.get(i);
                    }
                }
                double averageLogLoss = -averagePerplexity / (SuffixTree.LOG_E_2 * ((double) totalCount));
                averagePerplexity = java.lang.Math.pow(2, averageLogLoss);
                System.out.println(s + ", " + averagePerplexity + ", "+averageLogLoss);

                if (s > 0 && s != samples) {
                    sampleClient.connect(hostname, port);
                    sampleClient.sample(1);
                    sampleClient.disconnect();
// Request.Builder requestBuilder = Request.newBuilder();
// requestBuilder.setType(MessageType.SAMPLE);
// requestBuilder.setSample(SampleRequest.newBuilder().setNumberOfSweeps(1).build());
// Request request = requestBuilder.build();
// client.streamRequest(request);
// Response.Builder responseBuilder = Response.newBuilder();
// responseBuilder.setType(MessageType.SAMPLE);
// Response response = responseBuilder.build();
// Response.parseFrom(streamResponse());
                }
            }
        } catch (IOException ioe) {
            System.err.println("Client Error: error reading file " + filename);
            ioe.printStackTrace();
        }
    }

    public void partialPerplexity(double[] partialPerplexity, int start, int end) throws IOException {
        int requestsSent = 0;

        for (int r = start; r < end; r++) {
            Request request = scoreRequests.get(r);
            streamRequest(request);
            if (DEBUG && (requestsSent % 1000) == 0) {
                System.out.println((start+requestsSent) + "/" + scoreRequests.size() + " score requests sent.");
            }
            requestsSent++;
        }
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(MessageType.DISCONNECT);
        disconnectRequest = requestBuilder.build();
        streamRequest(disconnectRequest);

        cs.shutdownOutput();

        int index = start;
        for (int r = start; r < end; r++) {
            Response response = Response.parseFrom(streamResponse());
            switch (response.getType()) {
                case ERROR:
                    ErrorResponse error = response.getError();
                    System.err.println("Server error: " + error.getReason());
                    if (error.hasStackTrace()) {
                        System.err.println("Server stack trace: " + error.getStackTrace());
                    }
                    break;
                case SCORE:
                    ScoreResponse scoreResponse = response.getScore();
                    partialPerplexity[index] = partialPerplexity[index] + scoreResponse.getScoreList().get(0);
                    index++;

                    break;
                default:
                    System.err.print("Server Error: response of type " + response.getType() + " returned instead of SCORE");
                    System.exit(-1);
            }
        }
        Response response = Response.parseFrom(streamResponse());
        switch (response.getType()) {
            case ERROR:
                ErrorResponse error = response.getError();
                System.err.println("Server error: " + error.getReason());
                if (error.hasStackTrace()) {
                    System.err.println("Server stack trace: " + error.getStackTrace());
                }
                break;
            case DISCONNECT:
                // do nothing
                break;
            default:
                System.err.print("Server Error: response of type " + response.getType() + " returned instead of SCORE");
                System.exit(-1);
        }





    }
}

