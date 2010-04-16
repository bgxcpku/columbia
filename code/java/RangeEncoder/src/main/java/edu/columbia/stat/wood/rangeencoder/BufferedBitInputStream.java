/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.rangeencoder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author nicholasbartlett
 */
public class BufferedBitInputStream extends InputStream  {

    BufferedInputStream bis;
    int currentByte;
    int byteIndex = 8;
    int chop = (1<<8) - 1;

    public BufferedBitInputStream(File file) throws FileNotFoundException{
        this.bis = new BufferedInputStream(new FileInputStream(file));
    }

    @Override
    public int read() throws IOException {
        int returnBit;

        if(byteIndex < 8){
            returnBit = currentByte>>7;
            byteIndex++ ;
        } else {
            currentByte = bis.read();
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

    @Override
    public void close() throws IOException{
        if(bis!=null){
            bis.close();
        }
    }
}
