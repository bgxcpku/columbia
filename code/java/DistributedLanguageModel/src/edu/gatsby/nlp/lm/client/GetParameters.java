/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package edu.gatsby.nlp.lm.client;

import edu.gatsby.nlp.lm.LMIPCProtos.ErrorResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import edu.gatsby.nlp.lm.LMIPCProtos.MessageType;
import edu.gatsby.nlp.lm.LMIPCProtos.ParameterRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.ParameterResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.Response;
import java.io.IOException;

/**
*
* @author fwood
*/
public class GetParameters extends Client {

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


        GetParameters client = new GetParameters();

        client.connect(hostname, port);
        client.getParameters();
        client.disconnect();
    }

    public void getParameters() {

        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(MessageType.PARAMETER);
        requestBuilder.setParameter(ParameterRequest.newBuilder().build());
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
                case PARAMETER:
                    ParameterResponse parameterResponse = response.getParameter();
                    System.out.println(parameterResponse.getNames());
                    System.out.println(parameterResponse.getValues());

                    break;
                default:
                    System.err.print("Server Error: response of type " + response.getType() + " returned instead of PARAMETER");
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

