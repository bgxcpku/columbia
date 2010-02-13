/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.nlp.lm.client;

import edu.gatsby.nlp.lm.LMIPCProtos.Response;
import edu.gatsby.nlp.lm.LMIPCProtos.RemoveRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.RemoveResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.ErrorResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import edu.gatsby.nlp.lm.LMIPCProtos.MessageType;
import edu.gatsby.nlp.lm.LMIPCProtos.TokenListWithCount;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author fwood
 */
public class Remove extends Client {

    public static void main(String args[]) {
        String usage = "<server> <port> <domain> <count> <int> <int> <int> ... (context tuple in English reading order)";
        if (args.length < 5) {
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

        Remove client = new Remove();

        client.connect(hostname, port);
        client.remove(domain, count, tokens);
        client.disconnect();
    }

    public void remove(int domain, int count, ArrayList<Integer> tokens) {
        TokenListWithCount.Builder tokenlistBuilder = TokenListWithCount.newBuilder();
        tokenlistBuilder.setCount(count);
        tokenlistBuilder.addAllToken(tokens);

        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(MessageType.REMOVE);
        requestBuilder.setRemove(RemoveRequest.newBuilder().setDomain(domain).addTuple(tokenlistBuilder.build()).build());
        Request request = requestBuilder.build();

        streamRequest(request);
        parseRemove(count);

    }

    public void disconnectAndParse() {
        try {
            Request.Builder requestBuilder = Request.newBuilder();
            requestBuilder.setType(MessageType.DISCONNECT);
            Request request = requestBuilder.build();
            streamRequest(request);

            cs.shutdownOutput();
            parseDisconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseRemove(int count) {
        try {
            Response response = Response.parseFrom(streamResponse());
            switch (response.getType()) {
                case ERROR:
                    ErrorResponse error = response.getError();
                    System.err.println("Server error: " + error.getReason());
                    if (error.hasStackTrace()) {
                        System.err.println("Server stack trace: " + error.getStackTrace());
                    }
                    break;
                case REMOVE:
                    RemoveResponse removeResponse = response.getRemove();

                    if (removeResponse.getNumAdded() != count) {
                        System.err.print("Server Error: Only " + removeResponse.getNumAdded() + " out of " + count + " observations added.");
                    }
                    break;
                default:
                    System.err.print("Server Error: response of type " + response.getType() + " returned instead of REMOVE");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
