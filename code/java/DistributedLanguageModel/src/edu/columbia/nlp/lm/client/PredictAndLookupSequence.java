/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.nlp.lm.client;

import edu.gatsby.nlp.lm.LMIPCProtos.ErrorResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.PredictRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.PredictResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import edu.gatsby.nlp.lm.LMIPCProtos.MessageType;
import edu.gatsby.nlp.lm.LMIPCProtos.Response;
import edu.gatsby.nlp.lm.LMIPCProtos.TokenList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author fwood
 */
public class PredictAndLookupSequence extends Client {

    static HashMap<Integer, String> dictionary;

    public static void main(String args[]) {
        String usage = "<server> <port> <dictionary file> <domain> <count> <n as in n-gram> {<starting type (int)> : default 1}";
        if (args.length < 6) {
            System.err.println(usage);
            System.exit(-1);
        }


        String hostname = args[0];
        String dictionary_filename = args[2];
        int port = 4040;
        int domain = 1;
        int count = 1;
        int n = 3;
        int startingType = 1;

        try {
            port = Integer.valueOf(args[1]);
            domain = Integer.valueOf(args[3]);
            count = Integer.valueOf(args[4]);
            n = Integer.valueOf(args[5]);
            if (args.length > 6) {
                startingType = Integer.valueOf(args[6]);
            }
        } catch (Exception e) {
            System.err.println("Usage Error: port, domain, count, or n parameter is not an integer");
            System.err.println(usage);
            System.exit(-1);
        }

        dictionary = new HashMap<Integer, String>();
        try {
            BufferedReader lineReader = null;


            lineReader = new BufferedReader(new FileReader(dictionary_filename));

            String line = "";
            int lineNumber = 0;
            while ((line = lineReader.readLine()) != null) {
                lineNumber++;
                StringTokenizer strtok = new StringTokenizer(line);
                if (strtok.countTokens() != 2) {
                    continue;
                } else {
                    Integer id = Integer.valueOf(strtok.nextToken());
                    String type = strtok.nextToken();
                    dictionary.put(id, type);
                }
            }
            lineReader.close();
        } catch (Exception e) {
            System.err.println("client Error: Couldn't load dictionary file " + dictionary_filename);
            e.printStackTrace();
        }


        PredictAndLookupSequence client = new PredictAndLookupSequence();

        ArrayList<Integer> tokens = new ArrayList<Integer>(1);
        tokens.add(startingType);
        for (int c = 0; c < count; c++) {
            client.connect(hostname, port);
            tokens.add(client.predict(domain, n, tokens));
            client.disconnect();
        }
        for (int c = 0; c < count; c++) {
            System.out.println(tokens.get(c) + ", " + dictionary.get(tokens.get(c)));
        }
    }

    public int predict(int domain, int n, ArrayList<Integer> tokens) {
        TokenList.Builder tokenlistBuilder = TokenList.newBuilder();
        if (tokens.size() >= n) {
            tokenlistBuilder.addAllToken(tokens.subList(tokens.size() - n, tokens.size() - 1));
        } else {
            tokenlistBuilder.addAllToken(tokens);
        }
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(MessageType.PREDICT);
        requestBuilder.setPredict(PredictRequest.newBuilder().setDomain(domain).setNumberOfPredictions(1).setContext(tokenlistBuilder.build()).build());
        Request request = requestBuilder.build();
        try {
            streamRequest(request);

            requestBuilder.clear();
            requestBuilder.setType(MessageType.DISCONNECT);
            request = requestBuilder.build();

            streamRequest(request);

            cs.shutdownOutput();

            Response response = Response.parseFrom(streamResponse());
            switch (response.getType()) {
                case ERROR:
                    ErrorResponse error = response.getError();
                    System.err.println("Server error: " + error.getReason());
                    if (error.hasStackTrace()) {
                        System.err.println("Server stack trace: " + error.getStackTrace());
                    }
                    break;
                case PREDICT:
                    PredictResponse predictResponse = response.getPredict();

                    List<Integer> predictions = predictResponse.getPredictionList();
                    return predictions.get(0);
                default:
                    System.err.print("Server Error: response of type " + response.getType() + " returned instead of ADD");
            }
            parseDisconnect();




        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }


        return -1;

    }
}
