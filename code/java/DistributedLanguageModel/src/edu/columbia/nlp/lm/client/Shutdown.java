/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.nlp.lm.client;

import edu.gatsby.nlp.lm.LMIPCProtos.ErrorResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import edu.gatsby.nlp.lm.LMIPCProtos.MessageType;
import edu.gatsby.nlp.lm.LMIPCProtos.Response;
import edu.gatsby.nlp.lm.LMIPCProtos.ShutdownRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.ShutdownResponse;

/**
 *
 * @author fwood
 */
public class Shutdown extends Client {

    public static void main(String args[]) {
        String usage = "<server> <port>";
        if (args.length < 2) {
            System.err.println(usage);
            System.exit(-1);
        }


        String hostname = args[0];

        int port = 4040;

        try {
            port = Integer.valueOf(args[1]);
        } catch (Exception e) {
            System.err.println("Usage Error: port is not an integer");
            System.err.println(usage);
            System.exit(-1);
        }

        Shutdown client = new Shutdown();

        client.connect(hostname, port);
        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(MessageType.SHUTDOWN);
        requestBuilder.setShutdown(ShutdownRequest.newBuilder().build());
        Request request = requestBuilder.build();
        client.streamRequest(request);
        Response response = null;
        try {
            client.cs.shutdownOutput();
            response = Response.parseFrom(client.streamResponse());
        } catch (Exception e) {
            System.err.println("Client Error: attempting to continue.");
            e.printStackTrace();
        }
        switch (response.getType()) {
            case ERROR:
                ErrorResponse error = response.getError();
                System.err.println("Server error: " + error.getReason());
                if (error.hasStackTrace()) {
                    System.err.println("Server stack trace: " + error.getStackTrace());
                }
                break;
            case SHUTDOWN:
                ShutdownResponse shutdownResponse = response.getShutdown();

                break;
            default:
                System.err.print("Server Error: response of type " + response.getType() + " returned instead of ADD");
        }
        try {
            response = Response.parseFrom(client.streamResponse());
        } catch (Exception e) {
            System.err.println("Client Error: attempting to continue.");
            e.printStackTrace();
        }
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

        client.disconnect();
    }
}
