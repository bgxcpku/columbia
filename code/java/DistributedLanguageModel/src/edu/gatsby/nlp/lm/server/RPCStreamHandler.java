/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.gatsby.nlp.lm.server;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;
import edu.gatsby.nlp.lm.LMIPCProtos.AddRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.AddResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.DisconnectResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.ErrorResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.LoadRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.LoadResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.MessageType;
import edu.gatsby.nlp.lm.LMIPCProtos.ParameterResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.PredictRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.PredictResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.RemoveRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.RemoveResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import edu.gatsby.nlp.lm.LMIPCProtos.Response;
import edu.gatsby.nlp.lm.LMIPCProtos.SampleRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.SampleResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.SaveRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.SaveResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.ScoreRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.ScoreResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.ShutdownRequest;
import edu.gatsby.nlp.lm.LMIPCProtos.ShutdownResponse;
import edu.gatsby.nlp.lm.LMIPCProtos.TokenListWithCount;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fwood
 */
public class RPCStreamHandler extends Thread {

    private static final long serialVersionUID = 1;
    static boolean DEBUG = false;
    private Socket socket = null;
    protected Server server;

    public RPCStreamHandler(Server server, Socket socket) {
        super("IPCStreamHandler");
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        int requestsHandled = 1;
        int bytesReceived = 0;
        CodedOutputStream out = null;
        CodedInputStream in = null;

        try {
            //socket.setSendBufferSize(1000000);
            OutputStream os = socket.getOutputStream();
            out = CodedOutputStream.newInstance(os);
            in = CodedInputStream.newInstance(socket.getInputStream());
            in.setSizeLimit(Integer.MAX_VALUE);
        } catch (IOException e) {
            server.log_ps.println("Trouble creating coded input and output streams.");
            e.printStackTrace(server.log_ps);
            try {
                out.flush();
                socket.shutdownOutput();
                socket.close();
                return;
            } catch (IOException ee) {
                server.log_ps.println("Could not shutdown socket gracefully after error.  Thread terminating.");
                ee.printStackTrace(server.log_ps);
                return;
            }
        }

        // read in size of subsequent message
        int requestLength;
        try {
            int tag = in.readTag();
            server.log_ps.println("tag (must be positive) : " + tag);
            requestLength = in.readInt32();
            server.log_ps.println("upcoming message size (bytes) : " + requestLength);

        } catch (IOException ioe) {
            server.log_ps.println("Protobuf message length could not be read. Closing connection.");
            ioe.printStackTrace(server.log_ps);
            try {
                out.flush();
                socket.shutdownOutput();
                socket.close();
                return;
            } catch (IOException ee) {
                server.log_ps.println("Could not shutdown socket gracefully after error.  Thread terminating.");
                ee.printStackTrace(server.log_ps);
                return;
            }
        }

        // read and parse message off of line
        byte[] messageBytes = {};
        try {
            messageBytes = in.readRawBytes(requestLength);
        } catch (IOException ioe) {
            server.log_ps.println("Protobuf message of length " + requestLength + " could not be read. Closing connection.");
            ioe.printStackTrace(server.log_ps);
            try {
                out.flush();
                socket.shutdownOutput();
                socket.close();
                return;
            } catch (IOException ee) {
                server.log_ps.println("Could not shutdown socket gracefully after error.  Thread terminating.");
                ee.printStackTrace(server.log_ps);
                return;
            }
        }
        int messagesReceived = 1;
        bytesReceived += messageBytes.length;
        Request request = null;
        try {
            request = Request.parseFrom(messageBytes);
        } catch (InvalidProtocolBufferException ipbe) {
            server.log_ps.println("Bad message format, closing connection.");
            ipbe.printStackTrace(server.log_ps);
            try {
                out.flush();
                socket.shutdownOutput();
                socket.close();
                return;
            } catch (IOException ee) {
                server.log_ps.println("Could not shutdown socket gracefully after error.  Thread terminating.");
                ee.printStackTrace(server.log_ps);
                return;
            }
        }
        MessageType requestType = request.getType();

        AddResponse.Builder addResponseBuilder = AddResponse.newBuilder();
        RemoveResponse.Builder removeResponseBuilder = RemoveResponse.newBuilder();

        ScoreResponse.Builder scoreResponseBuilder = ScoreResponse.newBuilder();
        PredictResponse.Builder predictResponseBuilder = PredictResponse.newBuilder();
        Response.Builder responseBuilder = Response.newBuilder();

        int domain = 1;
        List<Integer> context = null;

        boolean disconnecting = false;

        while (!disconnecting) {
            Response response = null;
            switch (requestType) {
                case ADD:
                    server.log_ps.println("ADD -- req (" + requestsHandled + ") msg size (bytes): " + messageBytes.length);
                    AddRequest addRequest = request.getAdd();
                    domain = addRequest.getDomain();
                    int numAdded = 0;
                    for (TokenListWithCount t : addRequest.getTupleList()) {
                        int countsOfThisObservation = t.getCount();
                        numAdded += countsOfThisObservation;
                        server.getLanguageModel().addWithCount(domain, countsOfThisObservation, t.getTokenList());
                    }
                    if (DEBUG) {
                        System.out.println(numAdded);
                    }


                    addResponseBuilder.clear();
                    addResponseBuilder.setNumAdded(numAdded);
                    responseBuilder.clear();
                    responseBuilder.setType(MessageType.ADD);
                    responseBuilder.setAdd(addResponseBuilder.build());



                    break;
                case REMOVE:
                    server.log_ps.println("REMOVE -- req (" + requestsHandled + ") msg size (bytes): " + messageBytes.length);
                    RemoveRequest removeRequest = request.getRemove();
                    domain = removeRequest.getDomain();
                    int numRemoved = 0;
                    for (TokenListWithCount t : removeRequest.getTupleList()) {
                        int countsOfThisObservation = t.getCount();
                        numRemoved += countsOfThisObservation;
                        server.getLanguageModel().removeWithCount(domain, countsOfThisObservation, t.getTokenList());
                    }
                    if (DEBUG) {
                        System.out.println(numRemoved);
                    }


                    removeResponseBuilder.clear();
                    removeResponseBuilder.setNumAdded(numRemoved);
                    responseBuilder.clear();
                    responseBuilder.setType(MessageType.REMOVE);
                    responseBuilder.setAdd(addResponseBuilder.build());



                    break;
                case PREDICT:
                    server.log_ps.println("PREDICT -- req (" + requestsHandled + ") msg size (bytes): " + messageBytes.length);

                    PredictRequest predictRequest = request.getPredict();
                    context = predictRequest.getContext().getTokenList();
                    domain = predictRequest.getDomain();
                    int numPredictions = predictRequest.getNumberOfPredictions();

                    ArrayList<Integer> predictions = server.getLanguageModel().predict(domain, numPredictions, context);

                    predictResponseBuilder.clear();
                    for (int index = 0; index < predictions.size(); index++) {
                        predictResponseBuilder.addPrediction(predictions.get(index));
                    }

                    responseBuilder.clear();
                    responseBuilder.setType(MessageType.PREDICT);
                    responseBuilder.setPredict(predictResponseBuilder.build());


                    break;
                case SCORE:
                    server.log_ps.println("SCORE -- req (" + requestsHandled + ") msg size (bytes): " + messageBytes.length);

                    ScoreRequest scoreRequest = request.getScore();
                    context = scoreRequest.getContext().getTokenList();
                    List<Integer> tokensInContext = scoreRequest.getTypeList();
                    domain = scoreRequest.getDomain();
                    ArrayList<Double> scores = server.getLanguageModel().score(domain, context, tokensInContext);

                    scoreResponseBuilder.clear();
                    for (int index = 0; index < scores.size(); index++) {
                        scoreResponseBuilder.addScore(scores.get(index));
                    }

                    responseBuilder.clear();
                    responseBuilder.setType(MessageType.SCORE);
                    responseBuilder.setScore(scoreResponseBuilder.build());

                    break;
                case SAMPLE:
                    server.log_ps.println("SAMPLE -- req (" + requestsHandled + ") msg size (bytes): " + messageBytes.length);

                    SampleRequest sampleRequest = request.getSample();
                    int numSweeps = sampleRequest.getNumberOfSweeps();
                    for (int s = 0; s < numSweeps; s++) {
                        server.getLanguageModel().sample(1);
                        SampleResponse.Builder sampleResponseBuilder = SampleResponse.newBuilder();
                        sampleResponseBuilder.setSweepsComplete(s + 1);
                        sampleResponseBuilder.setScore(server.getLanguageModel().score());

                        responseBuilder.clear();
                        responseBuilder.setType(MessageType.SAMPLE);
                        responseBuilder.setSample(sampleResponseBuilder.build());

                        response = responseBuilder.build();
                        byte[] responseBytes = response.toByteArray();
                        server.log_ps.println("SAMPLE -- resp (" + requestsHandled + ") msg size (bytes): " + responseBytes.length);

                        try {
                            out.writeInt32(1, responseBytes.length);
                            response.writeTo(out);
                        } catch (IOException ioe) {
                            server.log_ps.println("Could not send response to sample. Closing connection.");
                            ioe.printStackTrace(server.log_ps);
                            try {
                                out.flush();
                                socket.shutdownOutput();
                                socket.close();
                                return;
                            } catch (IOException ee) {
                                server.log_ps.println("Could not shutdown socket gracefully after error.  Thread terminating.");
                                ee.printStackTrace(server.log_ps);
                                return;
                            }
                        }

                    }
                    break;
                case SAVE:
                    server.log_ps.println("SAVE -- req (" + requestsHandled + ") msg size (bytes): " + messageBytes.length);

                    SaveRequest saveRequest = request.getSave();
                    SaveResponse.Builder saveResponseBuilder = SaveResponse.newBuilder();
                    responseBuilder.clear();

                    try {
                        server.saveToFile(saveRequest.getFileName());
                        saveResponseBuilder.setMessage("success");
                        responseBuilder.setType(MessageType.SAVE);
                        responseBuilder.setSave(saveResponseBuilder.build());
                    } catch (IOException ioe) {
                        responseBuilder.setType(MessageType.ERROR);
                        ErrorResponse.Builder errorResponseBuilder = ErrorResponse.newBuilder();
                        errorResponseBuilder.setReason("Could not save language model to serverfile " + saveRequest.getFileName());
                        errorResponseBuilder.setStackTrace(ioe.toString());
                        responseBuilder.setError(errorResponseBuilder.build());
                    }


                    break;
                case LOAD:
                    server.log_ps.println("LOAD -- req (" + requestsHandled + ") msg size (bytes): " + messageBytes.length);


                    LoadRequest loadRequest = request.getLoad();
                    LoadResponse.Builder loadResponseBuilder = LoadResponse.newBuilder();
                    responseBuilder.clear();

                    try {
                        server.loadFromFile(loadRequest.getFileName());
                        loadResponseBuilder.setMessage("success");
                        responseBuilder.setType(MessageType.LOAD);
                        responseBuilder.setLoad(loadResponseBuilder.build());
                    } catch (IOException ioe) {
                        responseBuilder.setType(MessageType.ERROR);
                        ErrorResponse.Builder errorResponseBuilder = ErrorResponse.newBuilder();
                        errorResponseBuilder.setReason("Could not load language model from server file " + loadRequest.getFileName());
                        errorResponseBuilder.setStackTrace(ioe.toString());
                        responseBuilder.setError(errorResponseBuilder.build());
                    } catch (ClassNotFoundException cnfe) {
                        responseBuilder.setType(MessageType.ERROR);
                        ErrorResponse.Builder errorResponseBuilder = ErrorResponse.newBuilder();
                        errorResponseBuilder.setReason("Could not find Language Model class on server");
                        errorResponseBuilder.setStackTrace(cnfe.toString());
                        responseBuilder.setError(errorResponseBuilder.build());
                    }



                    break;
                case SHUTDOWN:
                    server.log_ps.println("SHUTDOWN -- req (" + requestsHandled + ") msg size (bytes): " + messageBytes.length);


                    ShutdownRequest shutdownRequest = request.getShutdown();
                    ShutdownResponse.Builder shutdownResponseBuilder = ShutdownResponse.newBuilder();
                    responseBuilder.clear();
                    responseBuilder.setType(MessageType.SHUTDOWN);
                    responseBuilder.setShutdown(shutdownResponseBuilder.build());
                    break;

                case PARAMETER:
                    server.log_ps.println("PARAMETER -- req (" + requestsHandled + ") msg size (bytes): " + messageBytes.length);

                    responseBuilder.clear();
                    responseBuilder.setType(MessageType.PARAMETER);
                    ParameterResponse.Builder parameterResponseBuilder = ParameterResponse.newBuilder();
                    parameterResponseBuilder.setNames(server.getLanguageModel().getParameterNames());
                    parameterResponseBuilder.setValues(server.getLanguageModel().getParameterValues());

                    responseBuilder.setParameter(parameterResponseBuilder.build());
                    break;
                    case DISCONNECT:
                    server.log_ps.println("DISCONNECT -- req (" + requestsHandled + ") msg size (bytes): " + messageBytes.length);

                    responseBuilder.clear();
                    responseBuilder.setType(MessageType.DISCONNECT);
                    DisconnectResponse.Builder disconnectResponseBuilder = DisconnectResponse.newBuilder();

                    responseBuilder.setDisconnect(disconnectResponseBuilder.build());
                    disconnecting=true;
                    break;
                default:

            }


            // build and send response
            switch (requestType) {
                case SAMPLE:
                    break;


                default:
                    response = responseBuilder.build();
                    String responseTypeString = response.getType().toString().toUpperCase();


                    byte[] responseBytes = response.toByteArray();
                    server.log_ps.println(responseTypeString + " -- resp (" + requestsHandled + ") msg size (bytes): " + responseBytes.length);
                    try {
                        out.writeInt32(1, responseBytes.length);
                        response.writeTo(out);
                        out.flush();
                    } catch (IOException ioe) {
                        server.log_ps.println("Could not send response to " + request.toString() + " . Closing connection.");
                        ioe.printStackTrace(server.log_ps);
                        try {
                            out.flush();
                            socket.shutdownOutput();
                            socket.close();
                            return;
                        } catch (IOException ee) {
                            server.log_ps.println("Could not shutdown socket gracefully after error.  Thread terminating.");
                            ee.printStackTrace(server.log_ps);
                            return;
                        }
                    }

            }

            if (requestType == MessageType.SHUTDOWN) {
                try {
                    server.serverSocket.close();
                } catch (IOException ioe) {
                    server.log_ps.println("Could not shutdown server socket gracefully on shutdown request, ignoring and shutting down.");
                    ioe.printStackTrace(server.log_ps);
                }
                server.log_ps.flush();
                System.exit(0);
            }


            if (socket.isConnected() && !socket.isInputShutdown() &&!disconnecting) {

                try {
                    int tag = in.readTag();
                    requestLength = in.readInt32();
                    messageBytes = in.readRawBytes(requestLength);
                } catch (IOException ioe) {
                    server.log_ps.println("Could not read message length and/or message body of message " + (requestsHandled + 1) + ". Closing connection.");
                    ioe.printStackTrace(server.log_ps);
                    try {
                        out.flush();
                        socket.shutdownOutput();
                        socket.close();
                        return;
                    } catch (IOException ee) {
                        server.log_ps.println("Could not shutdown socket gracefully after error.  Thread terminating.");
                        ee.printStackTrace(server.log_ps);
                        return;
                    }
                }
                bytesReceived += messageBytes.length;
                try {
                    request = Request.parseFrom(messageBytes);
                } catch (InvalidProtocolBufferException ipbe) {
                    server.log_ps.println("Could not parse message " + (requestsHandled + 1) + ". Will attempt to continue but probably this will be the end of this connection.");
                    ipbe.printStackTrace(server.log_ps);
                }
                requestsHandled++;

                requestType = request.getType();
            } else {
                break;
            }
        }
        try {
            out.flush();
            socket.shutdownOutput();
        } catch (IOException ee) {
            server.log_ps.println("Could not flush and shutdown socket gracefully at end of session, ignoring and continuing to close.");
            ee.printStackTrace(server.log_ps);
            return;
        }
        try {
//            while (!socket.isInputShutdown() && !socket.isClosed()) {
//                server.log_ps.println("Waiting for client to shutdown socket input stream to server before closing socket.");
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException ee) {
//                    server.log_ps.println("Server thread sleep interrupted.  Ignoring");
//
//                }
//            }
//             try {
//                    Thread.sleep(10000); // sleep ten seconds before closing the socket.
//                } catch (InterruptedException ee) {
//                    server.log_ps.println("Server thread sleep interrupted.  Ignoring");
//
//                }
            socket.setSoLinger(true, 100);
            socket.close();
        } catch (IOException eee) {
            server.log_ps.println("Could not close socket gracefully at end of session, ignoring.");
            eee.printStackTrace(server.log_ps);
            return;
        }
        if (DEBUG) {
            System.out.println(requestsHandled + " requests handled, " + bytesReceived + " bytes");
        }
        server.log_ps.println(requestsHandled + " requests handled, " + bytesReceived + " bytes");

    }
}

