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
import java.util.Random;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author nicholasbartlett
 */
public class FileRadixInputStream extends InputStream {

    private GZIPInputStream is = null;
    public long bytesRead = 0;
    private int radix;

    public FileRadixInputStream(File f, int radix, long streamLength) throws FileNotFoundException, IOException, InterruptedException{
        is = new GZIPInputStream(new FileInputStream(f));
        this.radix = radix;

        long fileSize = 0;
        int buffer_length = (int) Math.pow(2,16);
        byte[] buffer = new byte[buffer_length];
        if(f.getName().equals("enwik.xml.gz")){
            fileSize = 28722085779L;
        } else {
            int r = -1;
            while((r = is.read(buffer)) > -1){
                fileSize += r;
            }
            is.close();
            is = new GZIPInputStream(new FileInputStream(f));
        }
        
        long start = (long) (new Random().nextDouble() * (fileSize - streamLength - 1));
        start -= start % radix;

        long l = 0;
        while(l < start){
            if((long) buffer_length < start - l){
                l += is.read(buffer);
            } else {
                l += is.read(buffer, 0, (int) (start - l));
            }
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

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException{
        File f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/pap.gz");
        FileRadixInputStream fris = new FileRadixInputStream(f, 4,10000);
    }
}
