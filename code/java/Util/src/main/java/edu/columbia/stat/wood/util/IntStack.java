/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

/**
 *
 * @author nicholasbartlett
 */
public class IntStack {
    private int[] value;
    private int blockSize, index;

    public IntStack(int blockSize){
        value = new int[blockSize];
        this.blockSize = blockSize;
        index = -1;
    }
    
    public void push(int i){
        if(++index < value.length){
            value[index] = i;
        } else {
            int[] newValue;

            newValue = new int[value.length + blockSize];
            System.arraycopy(value, 0, newValue, 0, value.length);
            value = newValue;
            
            value[index] = i;
        }
    }

    public boolean hasNext(){
        return index > -1;
    }

    public int pop(){
        return value[index--];
    }

    public int peek(){
        return value[index];
    }

    public int size(){
        return index + 1;
    }

    public static void main(String[] args){
        IntStack is = new IntStack(100);
        for(int i = 0; i< 100; i++){
            is.push(i);
        }

        System.out.println(is.size());
        for(int i = 0; i< 100; i++){
            System.out.println(is.pop());
        }
        
        System.out.println(is.size());
    }
}
