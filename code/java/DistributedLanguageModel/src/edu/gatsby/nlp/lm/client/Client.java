/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.gatsby.nlp.lm.client;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import edu.gatsby.nlp.lm.LMIPCProtos.Request;
import java.net.Socket;

/**
 *
 * @author fwood
 */
public class Client {

    protected Socket cs;
//    protected BufferedInputStream inputStream;
//    protected BufferedOutputStream outputStream;
    protected CodedInputStream inputStream;
    protected CodedOutputStream outputStream;

    public void connect(String host, int port) {
        try {
            cs = new Socket(host, port);
            cs.setKeepAlive(true);
            cs.setSoTimeout(0);
            cs.setSendBufferSize(2000000);
            cs.setReceiveBufferSize(2000000);

            inputStream = CodedInputStream.newInstance(cs.getInputStream());
            outputStream = CodedOutputStream.newInstance(cs.getOutputStream());

        } catch (Exception e) {
            System.err.println("Could not connect to server " + host + " at port " + port);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public byte[] streamResponse() {
        // byte[] responseBytes;
        try {
            int tag = -1;
            while (tag == -1) {
                try {

                    tag = inputStream.readTag(); // useless
                } catch (java.net.SocketTimeoutException ste) {
                    System.err.println("Socket timed-out: re-started wait for response");
                    ste.printStackTrace();
                }
            }
            int responseLength = inputStream.readInt32();
            byte[] responseBytes = inputStream.readRawBytes(responseLength);

            return responseBytes;
        } catch (Exception e) {
            System.err.println("Error reading response");
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    public void streamRequest(Request request) {
        byte[] requestBytes = request.toByteArray();
        try {
            outputStream.writeInt32(1, requestBytes.length);
            request.writeTo(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            System.err.println("Error sending request");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void disconnect() {
        try {
            if (!cs.isOutputShutdown()) {
                outputStream.flush();
                cs.shutdownOutput();
            }
            if (!cs.isInputShutdown()) {
                cs.shutdownInput();
            }

            cs.close();
        } catch (Exception e) {
            System.err.println("Error disconnecting from socket");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
