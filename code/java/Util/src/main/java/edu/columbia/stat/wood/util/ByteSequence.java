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
public class ByteSequence extends HashMap<BigInt, byte[]> {

    private byte[] currentArray;
    private BigInt currentKey, earliestKey;
    private int index, arraySize;

    public ByteSequence(int arraySize){
        currentArray = new byte[arraySize];
        currentKey = new BigInt();
        earliestKey = new BigInt();
        index = arraySize - 1;
        this.arraySize = arraySize;

        put(currentKey, currentArray);
        
    }

    public void append(byte b){
        if(index > -1){
            currentArray[index--] = b;
        } else {
            currentArray = new byte[arraySize];
            currentKey = currentKey.next();

            currentArray[arraySize-1] = b;
            index = arraySize - 2;
            put(currentKey, currentArray);
        }
    }

    public int blockSize(){
        return arraySize;
    }

    public ByteSequenceBackwardIterator backwardsIterator(){
        return new ByteSequenceBackwardIterator();
    }

    public ByteSequenceBackwardIterator backwardsIterator(BigInt key, int index){
        byte[] array;

        array = get(key);
        if(array == null){
            return null;
        } else {
            return new ByteSequenceBackwardIterator(key, index, array);
        }
    }

    public void shortenSequence(){
        remove(earliestKey);
        earliestKey = earliestKey.next();
    }

    public class ByteSequenceBackwardIterator implements ByteIterator {
        BigInt key = currentKey;
        byte[] array = currentArray;
        int ind = index + 1;

        public ByteSequenceBackwardIterator(){}

        public ByteSequenceBackwardIterator(BigInt key, int index, byte[] array){
            this.key = key;
            this.array = array;
            ind = index;
        }

        public boolean hasNext() {
            if(ind < arraySize){
                return true;
            } else {
                if(key.isZero()){
                    return false;
                } else {
                    key = key.previous();
                    array = get(key);
                    ind = 0;
                    return array != null;
                }
            }
        }

        public byte next() {
            if(ind >= arraySize){
                key = key.previous();
                array = get(key);
                ind = 0;
            }

            return array[ind++];
        }

        public byte peek(){
            if(ind >= arraySize){
                key = key.previous();
                array = get(key);
                ind = 0;
            }

            return array[ind];
        }

        public BigInt key(){
            return key;
        }

        public int index(){
            return ind;
        }

        public int available(){
            return arraySize - ind + (size() - 1) * arraySize;
        }
    }

    public static void main(String[] args){
        
    }
}
