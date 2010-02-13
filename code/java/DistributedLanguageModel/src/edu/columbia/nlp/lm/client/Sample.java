/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.nlp.lm.client;

import edu.gatsby.nlp.lm.LMIPCProtos.AddResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.ErrorResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import edu.gatsby.nlp.lm.LMIPCProtos.MessageType;
import edu.gatsby.nlp.lm.LMIPCProtos.Response;
import edu.gatsby.nlp.lm.LMIPCProtos.SampleRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.SampleResponse;
import java.io.IOException;

/**
 *
 * @author fwood
 */
public class Sample extends Client {

    public static void main(String args[]) {
        String usage = "<server> <port> <sweeps>";
        if (args.length < 3) {
            System.err.println(usage);
            System.exit(-1);
        }


        String hostname = args[0];

        int port = 4040;
        int sweeps = 1;

        try {
            port = Integer.valueOf(args[1]);
            sweeps = Integer.valueOf(args[2]);
        } catch (Exception e) {
            System.err.println("Usage Error: port or sweep parameter is not an integer");
            System.err.println(usage);
            System.exit(-1);
        }



        Sample client = new Sample();
        client.connect(hostname, port);
        client.sample(sweeps);
        client.disconnect();
    }

    public void sample(int sweeps) {


        Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setType(MessageType.SAMPLE);
        requestBuilder.setSample(SampleRequest.newBuilder().setNumberOfSweeps(sweeps).build());
        Request request = requestBuilder.build();
        try {
            streamRequest(request);

            requestBuilder.clear();
            requestBuilder.setType(MessageType.DISCONNECT);
            request = requestBuilder.build();

            streamRequest(request);

            cs.shutdownOutput();

            for (int s = 1; s <= sweeps; s++) {

                Response response = Response.parseFrom(streamResponse());
                switch (response.getType()) {
                    case ERROR:
                        ErrorResponse error = response.getError();
                        System.err.println("Server error: " + error.getReason());
                        if (error.hasStackTrace()) {
                            System.err.println("Server stack trace: " + error.getStackTrace());
                        }
                        break;
                    case SAMPLE:
                        SampleResponse sampleResponse = response.getSample();

                        if (sampleResponse.hasScore()) {
                            System.out.println("Sweep, " + sampleResponse.getSweepsComplete() + "," + sweeps + ", Score, " + sampleResponse.getScore());
                        } else {
                            System.out.println("Sweep " + sampleResponse.getSweepsComplete() + "/" + sweeps);

                        }
                        break;
                    default:
                        System.err.print("Server Error: response of type " + response.getType() + " returned instead of ADD");
                }

            }
            parseDisconnect();


        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }




    }
}
