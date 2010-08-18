/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.util;

import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */

public class ByteSequence {

    private HashMap<Integer, byte[]> value;
    private byte[] currentArray;
    private int length, maxLength, index;


    public int startIndex, lastIndex;
    
    public ByteSequence(int maxLength) {
        value = new HashMap<Integer, byte[]>(1024);
        currentArray = new byte[1024];
        value.put(0, currentArray);
        this.maxLength = maxLength;
        startIndex = 0;
        index = 0;
        lastIndex = -1;
    }

    public void append(byte b) {
        if(index == 1024){
            currentArray = new byte[1024];
            index = 0;
            value.put(1024 * value.size(), currentArray);
        }

        currentArray[index++] = b;
        length++;
        lastIndex++;

        if(length > maxLength){
            shortenSequence();
        }
    }

    public byte get(int i) {
        int key;

        key = i / 1024;
        key *= 1024;
    
        return value.get(key)[i - key];
    }

    public int getOverlap(int edgeStart, int edgeLength, int index) {
        int lIndex, rIndex, leftKey, rightKey, overlap;
        byte[] leftArray, rightArray;

        if(edgeStart + edgeLength -1 < startIndex){
            System.out.println("byte sequence cannot get all the information");
            return -1;
        }

        leftKey = (edgeStart + edgeLength - 1) / 1024;
        leftKey *= 1024;
        rightKey = index / 1024;
        rightKey *= 1024;

        lIndex = edgeStart + edgeLength - 1 - leftKey;
        rIndex = index - rightKey;

        leftArray = value.get(leftKey);
        rightArray = value.get(rightKey);

        overlap = 0;
        while (overlap < edgeLength && leftArray[lIndex] == rightArray[rIndex]) {
            overlap++;
            lIndex--;
            rIndex--;

            if (lIndex == -1) {
                lIndex = 1023;
                leftKey -= 1024;
                leftArray = value.get(leftKey);
            }
            
            if(rIndex == -1){
                rIndex = 1023;
                rightKey -= 1024;
                rightArray = value.get(rightKey);
            }
        }

        return overlap;
    }

    private void shortenSequence(){
        value.remove(startIndex);
        startIndex += 1024;
        length -= 1024;
    }
}
