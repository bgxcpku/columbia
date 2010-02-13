/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.nlp.lm.client;

import edu.gatsby.nlp.lm.LMIPCProtos.ErrorResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.LoadRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.LoadResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import edu.gatsby.nlp.lm.LMIPCProtos.MessageType;
import edu.gatsby.nlp.lm.LMIPCProtos.Response;
import java.io.IOException;

/**
 *
 * @author fwood
 */
public class Load extends Client {

    public static void main(String args[]) {
        String usage = "<server> <port> <filename> (filename on server)";
        if (args.length < 3) {
            System.err.println(usage);
            System.exit(-1);
        }


        String hostname = args[0];

        int port = 4040;

        try {
            port = Integer.valueOf(args[1]);
        } catch (Exception e) {
            System.err.println("Usage Error: port parameter is not an integer");
            System.err.println(usage);
            System.exit(-1);
        }

        String serverFileName = args[2];

        Load client = new Load();

        client.connect(hostname, port);
        client.load(serverFileName);
        client.disconnect();
    }

    public void load(String serverFileName) {

        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(MessageType.LOAD);
        requestBuilder.setLoad(LoadRequest.newBuilder().setFileName(serverFileName).build());
        Request request = requestBuilder.build();
        try {
            streamRequest(request);

            requestBuilder.clear();
            requestBuilder.setType(MessageType.DISCONNECT);
            request = requestBuilder.build();

            streamRequest(request);

            cs.shutdownOutput();
            cs.setSoTimeout(0);


            Response response = Response.parseFrom(streamResponse());
            switch (response.getType()) {
                case ERROR:
                    ErrorResponse error = response.getError();
                    System.err.println("Server error: " + error.getReason());
                    if (error.hasStackTrace()) {
                        System.err.println("Server stack trace: " + error.getStackTrace());
                    }
                    break;
                case LOAD:

                    LoadResponse loadResponse = response.getLoad();

                    System.out.println("Success: Server loaded language model from : " + serverFileName);

                    break;
                default:
                    System.err.print("Server Error: response of type " + response.getType() + " returned instead of SAVE");
            }
            parseDisconnect();



        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }




    }
}
