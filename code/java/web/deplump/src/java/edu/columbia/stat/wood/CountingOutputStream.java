/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author fwood
 */
public class CountingOutputStream extends BufferedOutputStream {

    int numBytesWritten = 0;

    public CountingOutputStream(OutputStream out, int size) {
        super(out, size);
    }

    public CountingOutputStream(OutputStream out) {
        super(out);
    }



    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        numBytesWritten = numBytesWritten+len;
        super.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
                numBytesWritten = numBytesWritten+b.length;

        super.write(b);
    }

    @Override
    public void write(int b) throws IOException {
                        numBytesWritten = numBytesWritten+1;

        super.write(b);
    }

}
