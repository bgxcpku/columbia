/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package edu.gatsby.nlp.lm.client;

import edu.gatsby.nlp.lm.LMIPCProtos.ErrorResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import edu.gatsby.nlp.lm.LMIPCProtos.MessageType;
import edu.gatsby.nlp.lm.LMIPCProtos.Response;
import edu.gatsby.nlp.lm.LMIPCProtos.ScoreRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.ScoreResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.TokenList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
*
* @author fwood
*
* The same as Score, except it returns the score rather than printing it.
* Modified by David Pfau, 2010
*
*/
public class GetScore extends Client {

    public static void main(String args[]) {
        String usage = "<server> <port> <domain> <int> <int> <int> ... (context tuple in English reading order)";
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
        } catch (Exception e) {
            System.err.println("Usage Error: port or domain parameter is not an integer");
            System.err.println(usage);
            System.exit(-1);
        }

        ArrayList<Integer> tokens = new ArrayList<Integer>(args.length - 3);
        for (int i = 3; i < args.length; i++) {
            try {
                tokens.add(Integer.valueOf(args[i]));
            } catch (Exception e) {
                System.err.println("Usage Error: one of the integer tuple elements is not an integer");
                System.err.println(usage);
                System.exit(-1);
            }
        }

        GetScore client = new GetScore();

        client.connect(hostname, port);
        client.score(domain, tokens);
        client.disconnect();
    }

    public double score(int domain, ArrayList<Integer> tokens) {
        TokenList.Builder tokenlistBuilder = TokenList.newBuilder();
        List<Integer> context = new ArrayList<Integer>();
        if (tokens.size() > 1) {
            context = tokens.subList(0, tokens.size() - 1);
        }

        Integer type = tokens.get(tokens.size() - 1);
        tokenlistBuilder.addAllToken(context);

        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(MessageType.SCORE);
        requestBuilder.setScore(ScoreRequest.newBuilder().setDomain(domain).addType(type).setContext(tokenlistBuilder.build()).build());
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
                case SCORE:
                    ScoreResponse scoreResponse = response.getScore();
                    return scoreResponse.getScoreList().get(0);
                default:
                    System.err.print("Server Error: response of type " + response.getType() + " returned instead of SCORE");
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

        return -1.0;


    }
}

