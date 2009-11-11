/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.gatsby.nlp.lm.client;

import edu.gatsby.nlp.lm.LMIPCProtos.ErrorResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.MessageType;
import edu.gatsby.nlp.lm.LMIPCProtos.PredictRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.PredictResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import edu.gatsby.nlp.lm.LMIPCProtos.Response;
import edu.gatsby.nlp.lm.LMIPCProtos.TokenList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fwood
 */
public class Predict extends Client {

    public static void main(String args[]) {
        String usage = "<server> <port> <domain> <count> <int> <int> <int> ... (context tuple in English reading order)";
        if (args.length < 4) {
            System.err.println(usage);
            System.exit(-1);
        }


        String hostname = args[0];

        int port = 4040;
        int domain = 1;
        int count = 1;

        try {
            port = Integer.valueOf(args[1]);
            domain = Integer.valueOf(args[2]);
            count = Integer.valueOf(args[3]);
        } catch (Exception e) {
            System.err.println("Usage Error: port, domain, or count parameters is not an integer");
            System.err.println(usage);
            System.exit(-1);
        }

        ArrayList<Integer> tokens = new ArrayList<Integer>(args.length - 4);
        for (int i = 4; i < args.length; i++) {
            try {
                tokens.add(Integer.valueOf(args[i]));
            } catch (Exception e) {
                System.err.println("Usage Error: one of the integer tuple elements is not an integer");
                System.err.println(usage);
                System.exit(-1);
            }
        }

        Predict client = new Predict();

        client.connect(hostname, port);
        client.predict(domain, count, tokens);
        client.disconnect();
    }

    public void predict(int domain, int count, ArrayList<Integer> tokens) {
        TokenList.Builder tokenlistBuilder = TokenList.newBuilder();
        tokenlistBuilder.addAllToken(tokens);

        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(MessageType.PREDICT);
        requestBuilder.setPredict(PredictRequest.newBuilder().setDomain(domain).setNumberOfPredictions(count).setContext(tokenlistBuilder.build()).build());
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
                    for (Integer prediction : predictions) {
                        System.out.println(prediction);
                    }

                    break;
                default:
                    System.err.print("Server Error: response of type " + response.getType() + " returned instead of PREDICT");
            }
            response = Response.parseFrom(streamResponse());
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
                    System.err.print("Server Error: response of type " + response.getType() + " returned instead of ADD");
            }




        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }




    }
}
