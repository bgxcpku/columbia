/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizermain;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */
public class BitReader {

    private int currentByte;
    private int[] bitRep = new int[8];
    private int index = 8;
    private FileInputStream fileInputStream = null;

    public BitReader(String file) throws FileNotFoundException, IOException {
        fileInputStream = new FileInputStream(file);
    }

    public int read() throws IOException {
        if (index == 8) {
            currentByte = fileInputStream.read();
            if (currentByte == -1) {
                return -1;
            }
            index = 0;
            this.fillBitRep();
        }
        return bitRep[index++];
    }

    private void fillBitRep() {
        String binRep = Integer.toBinaryString(currentByte);
        int i = 0;
        while (i < 8 - binRep.length()) {
            bitRep[i++] = 0;
        }
        int j = 0;
        while (i < 8) {
            bitRep[i++] = binRep.charAt(j++) - 48;
        }
    }

    public void close() throws IOException {
        if (fileInputStream != null) {
            fileInputStream.close();
        }
    }
}
