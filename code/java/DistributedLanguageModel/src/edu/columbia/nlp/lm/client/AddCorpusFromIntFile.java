/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.nlp.lm.client;

import edu.gatsby.nlp.lm.LMIPCProtos.AddRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.AddResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.ErrorResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import edu.gatsby.nlp.lm.LMIPCProtos.Response;
import edu.gatsby.nlp.lm.LMIPCProtos.MessageType;
import edu.gatsby.nlp.lm.LMIPCProtos.TokenListWithCount;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author fwood
 */
public class AddCorpusFromIntFile extends Client {

    static boolean DEBUG = false;
    String fileName;

    public AddCorpusFromIntFile(String fileName) {
        this.fileName = fileName;
    }

    public static void main(String args[]) {
        String usage = "<server> <port> <n of n-gram> <domain> <filename>";
        if (args.length < 5) {
            System.err.println(usage);
            System.exit(-1);
        }


        String hostname = args[0];

        int port = 4041;
        int domain = 1;
        int n = 3;

        try {
            port = Integer.valueOf(args[1]);
            n = Integer.valueOf(args[2]);
            domain = Integer.valueOf(args[3]);
        } catch (Exception e) {
            System.err.println("Usage Error: port, n of n-gram, or domain is not an integer");
            System.err.println(usage);
            System.exit(-1);
        }

        String filename = args[4];

        AddCorpusFromIntFile client = new AddCorpusFromIntFile(filename);

        client.connect(hostname, port);
        try {
            client.add(domain, n);
        } catch (FileNotFoundException fnfe) {
            System.err.println("Usage Error: file " + filename + " not found");
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            System.err.println("Client Error: error reading file " + filename);
            ioe.printStackTrace();
        }
        client.disconnect();
    }

    public void add(int domain, int n) throws FileNotFoundException, IOException {
        BufferedReader lineReader = null;
        TokenListWithCount.Builder tokenlistBuilder = TokenListWithCount.newBuilder();
        Request.Builder requestBuilder = Request.newBuilder();
        AddRequest.Builder addRequestBuilder = AddRequest.newBuilder();

        lineReader = new BufferedReader(new FileReader(fileName));

        // every thousand or so tuples, package up the request and send it off to the server

        String line = "";
        int lineNumber = 0;
        int countsRequestedToBeAdded = 0;
        int numAddRequestsIssued = 0;

        int perMessagecountsRequestedToBeAdded = 0;

        int requestsSent = 0;
        int bytesSent = 0;

        requestBuilder.setType(MessageType.ADD);
        addRequestBuilder.setDomain(domain);
        while ((line = lineReader.readLine()) != null) {
            lineNumber++;
            StringTokenizer strtok = new StringTokenizer(line);
            if (strtok.countTokens() != n + 1) {
                continue;
            } else {
                try {
                    ArrayList<Integer> contextPlusToken = new ArrayList<Integer>(n - 1);
                    Integer count = Integer.valueOf(strtok.nextToken());
                    for (int i = 0; i < n; i++) {
                        contextPlusToken.add(Integer.valueOf(strtok.nextToken()));
                    }

                    tokenlistBuilder.clear();
                    tokenlistBuilder.setCount(count);
                    countsRequestedToBeAdded += count;
                    perMessagecountsRequestedToBeAdded += count;
                    tokenlistBuilder.addAllToken(contextPlusToken);
                    addRequestBuilder.addTuple(tokenlistBuilder.build());

                } catch (Exception e) {
                    System.err.println("Client Error: malformed line (" + lineNumber + ") in " + fileName);
                }

            }

            if (lineNumber > 1 && (lineNumber % 1000) == 0) {
                if (DEBUG) {
                    System.out.println(perMessagecountsRequestedToBeAdded + ", " + countsRequestedToBeAdded);
                }
                perMessagecountsRequestedToBeAdded = 0;
                requestsSent++;
                requestBuilder.setAdd(addRequestBuilder.build());
                Request request = requestBuilder.build();
                bytesSent += request.toByteArray().length;
                streamRequest(request);
                numAddRequestsIssued++;
                requestBuilder.clear();
                addRequestBuilder.clear();
                requestBuilder.setType(MessageType.ADD);
                addRequestBuilder.setDomain(domain);
            }
        }

        if (perMessagecountsRequestedToBeAdded > 0) {
            if (DEBUG) {
                System.out.println(perMessagecountsRequestedToBeAdded + ", " + countsRequestedToBeAdded);
            }
            perMessagecountsRequestedToBeAdded = 0;
            requestsSent++;
            requestBuilder.setAdd(addRequestBuilder.build());
            Request request = requestBuilder.build();
            bytesSent += request.toByteArray().length;
            streamRequest(request);
            numAddRequestsIssued++;
            requestBuilder.clear();
            addRequestBuilder.clear();
            requestBuilder.setType(MessageType.ADD);
            addRequestBuilder.setDomain(domain);
        }
        if (DEBUG) {
            System.out.println("--------");
            System.out.println(countsRequestedToBeAdded);
            System.out.println("--------");
        }
        requestBuilder.clear();
        requestBuilder.setType(MessageType.DISCONNECT);
        Request request = requestBuilder.build();
        requestsSent++;
        bytesSent += request.toByteArray().length;

        streamRequest(request);
        outputStream.flush();
        cs.shutdownOutput();
        if (DEBUG) {
            System.out.println(requestsSent + " requests sent, " + bytesSent + " bytes");
        }

        int countsAdded = 0;
        if (DEBUG) {
            System.out.println("-------");
        }
        for (int r = 0; r < numAddRequestsIssued; r++) {

            Response response = Response.parseFrom(streamResponse());
            AddResponse addResponse = null;
            switch (response.getType()) {
                case ERROR:
                    ErrorResponse error = response.getError();
                    System.err.println("Server error: " + error.getReason());
                    if (error.hasStackTrace()) {
                        System.err.println("Server stack trace: " + error.getStackTrace());
                    }
                    break;
                case ADD:
                    addResponse = response.getAdd();
                    countsAdded += addResponse.getNumAdded();

                    break;
                default:
                    System.err.print("Server Error: response of type " + response.getType() + " returned instead of ADD");
            }

            if (DEBUG) {
                System.out.println(addResponse.getNumAdded());
            }
        }
        parseDisconnect();

        if (countsRequestedToBeAdded != countsAdded) {
            System.err.print("Server Error: Only " + countsAdded + " out of " + countsRequestedToBeAdded + " observations added.");
        }


    }
}
