/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.dcc2010;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author nicholasbartlett
 */
public class FileRadixInputStream extends InputStream {

    private GZIPInputStream is = null;
    public static long bytesRead = 0;
    private int radix;
    private long streamLength = Long.MAX_VALUE;

    public FileRadixInputStream(File f, int radix) throws FileNotFoundException, IOException{
        is = new GZIPInputStream(new FileInputStream(f));
        this.radix = radix;
    }

    public FileRadixInputStream(File f, int radix, long streamLength) throws FileNotFoundException, IOException{
        this(f,radix);
        if(streamLength > -1){
            this.streamLength = streamLength;
        }
    }

    public long readLong() throws IOException {
        long l = 0;
        int b;
        
        for(int i = 0; i < radix; i++){
            b = is.read();
            if(b > -1){
                l <<= 8;
                l += b;
            } else {
                return -1;
            }
        }

        bytesRead += radix;

        return l;
    }

    @Override
    public void close() throws IOException{
        is.close();
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
