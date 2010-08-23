/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream which reads a bit at a time.
 *
 * @author nicholasbartlett
 */
public class BufferedBitInputStream {

    InputStream is;
    int currentByte;
    int byteIndex = 8;
    int chop = (1<<8) - 1;

    /**
     * Creates a BufferedBitInputStream with the specified underlying InputStream.
     *
     * @param is underlying InputStream.
     * @throws FileNotFoundException
     */
    public BufferedBitInputStream(InputStream is) throws FileNotFoundException{
        this.is = is;
    }

    /**
     * Reads the next bit.
     *
     * @return either 0 or 1 if there is a next bit, -1 if end of stream
     * @throws IOException
     */
    public int read() throws IOException {
        int returnBit;

        if(byteIndex < 8){
            returnBit = currentByte>>7;
            byteIndex++ ;
        } else {
            currentByte = is.read();
            if(currentByte > -1){
                byteIndex = 1;
                returnBit = currentByte>>7;
            } else {
                returnBit = -1;
            }
        }

        currentByte = currentByte << 1 & chop;
        return returnBit;
    }
}
