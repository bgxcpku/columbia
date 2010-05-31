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
*/
public class TestStreamingScore extends Client {

    public static void main(String args[]) {
        String usage = "<server> <port> <domain> <context length> <dictionary size> <num score messages per session> <num sessions>";
        if (args.length < 4) {
            System.err.println(usage);
            System.exit(-1);
        }


        String hostname = args[0];

        int port = 4040;
        int domain = 1;
        int num_sessions = 1;
        int num_score_message_per_session = 1;
        int dictionary_size = 10000;
        int cl = 2;

        try {
            port = Integer.valueOf(args[1]);
            domain = Integer.valueOf(args[2]);
            cl = Integer.valueOf(args[3]);
            dictionary_size = Integer.valueOf(args[4]);
            num_score_message_per_session = Integer.valueOf(args[5]);
            num_sessions = Integer.valueOf(args[6]);
        } catch (Exception e) {
            System.err.println("Usage Error: port, domain, context length, dictionary size, num score messages per session , or num sessions parameter is not an integer");
            System.err.println(usage);
            System.exit(-1);
        }

        for (int s = 0; s < num_sessions; s++) {
            ArrayList<ArrayList<Integer>> tokens = new ArrayList<ArrayList<Integer>>(num_sessions);


            TestStreamingScore client = new TestStreamingScore();

            client.connect(hostname, port);

            for (int m = 0; m < num_score_message_per_session; m++) {
                ArrayList<Integer> foo = new ArrayList<Integer>(cl + 1);
                for (int i = 0; i < (cl + 1); i++) {
                    foo.add((int) (Math.random() * (double) dictionary_size));
                }
                tokens.add(foo);


            }

            client.score(domain, tokens);
            client.report(domain, tokens);

            client.disconnect();
        }
    }

    public void score(int domain, ArrayList<ArrayList<Integer>> tokenList) {
        Request.Builder requestBuilder = Request.newBuilder();
        TokenList.Builder tokenlistBuilder = TokenList.newBuilder();

        for (ArrayList<Integer> tokens : tokenList) {
            tokenlistBuilder.clear();
            List<Integer> context = new ArrayList<Integer>();
            if (tokens.size() > 1) {
                context = tokens.subList(0, tokens.size() - 1);
            }

            Integer type = tokens.get(tokens.size() - 1);
            tokenlistBuilder.addAllToken(context);
            requestBuilder.clear();
            requestBuilder.setType(MessageType.SCORE);
            requestBuilder.setScore(ScoreRequest.newBuilder().setDomain(domain).addType(type).setContext(tokenlistBuilder.build()).build());
            Request request = requestBuilder.build();
            //try {
            streamRequest(request);

            requestBuilder.clear();

        //} catch (IOException e) {
        // e.printStackTrace();
        // System.exit(-1);
        //}

        }

        requestBuilder.clear();
        requestBuilder.setType(MessageType.DISCONNECT);
        Request request = requestBuilder.build();

        streamRequest(request);
        try {
            cs.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void report(int domain, ArrayList<ArrayList<Integer>> tokenList) {
        try {
            for (int i = 0; i < tokenList.size(); i++) {
                Response response = Response.parseFrom(streamResponse());
                Thread.sleep(50);
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
                        for (Double d : scoreResponse.getScoreList()) {
                            System.out.println(d);
                        }
                        break;
                    default:
                        System.err.print("Server Error: response of type " + response.getType() + " returned instead of SCORE");
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
                    System.err.print("Server Error: response of type " + response.getType() + " returned instead of ADD");
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }




    }
}

