/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import java.util.Arrays;

/**
 *
 * @author nicholasbartlett
 */

public class BigInt {

    private byte[] value;

    public BigInt(){
        value = new byte[]{0};
    }

    public BigInt(byte[] value){
        this.value = value;
    }

    public boolean isZero(){
        return value.length == 1 && value[0] == 0;
    }

    public byte[] value(){
        return value;
    }

    public BigInt next(){
        BigInt retValue;

        if(nextIsLonger()){
            byte[] val;

            val = new byte[value.length + 1];
            val[value.length] = 1;
            retValue = new BigInt(val);
        } else {
            retValue = copyOf();
            retValue.increment();
        }

        return retValue;
    }

    public BigInt previous() {
        BigInt retValue;

        if (value.length == 1) {
            if (value[0] != 0) {
                retValue = new BigInt(new byte[]{(byte) (value[0] - 1)});
            } else {
                throw new IllegalArgumentException("No previous BigInt");
            }
        } else if (previousIsShorter()) {
            byte[] val;

            val = new byte[value.length - 1];
            Arrays.fill(val, (byte) -1);
            retValue = new BigInt(val);
        } else {
            retValue = copyOf();
            retValue.decrement();
        }

        return retValue;
    }

    public BigInt copyOf(){
        byte[] val;
        int l;

        l = value.length;
        val = new byte[l];
        System.arraycopy(value, 0, val, 0, l);
        return new BigInt(val);
    }

    public void print(){
        System.out.print("[" + value[0]);
        for(int i = 1; i < value.length; i++){
            System.out.print(", " + value[i]);
        }
        System.out.println("]");
    }

    @Override
    public boolean equals(Object E){
        if(E == null){
            return false;
        } else if(this.getClass() != E.getClass()){
            return false;
        } else {
            return Arrays.equals(value,((BigInt) E).value());
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Arrays.hashCode(this.value);
        return hash;
    }

    public int intValue(){
        int intValue;

        intValue = 0;
        for(int i = 0; i < value.length; i++){
            intValue += ((int) value[i] & 0xFF) << (8*i);
        }

        return intValue;
    }
    
    private boolean nextIsLonger(){
        for(int i = 0; i < value.length; i++){
            if(value[i] != -1){
                return false;
            }
        }
        return true;
    }

    private void increment(){
        int index;

        index = 0;
        do{
            value[index]++;
        } while(value[index++] == 0);
    }

    private boolean previousIsShorter(){
        for(int i = 0; i < value.length - 1; i++){
            if(value[i] != 0){
                return false;
            }
        }

        if(value[value.length - 1] == 1){
            return true;
        } else {
            return false;
        }
    }

    private void decrement(){
        int index;

        index = 0;
        do{
            value[index]--;
        } while(value[index++] == -1);
    }

    public static void main(String[] args){
        BigInt bi = new BigInt();

        System.out.println(bi.intValue());
        for(int i = 0; i<1025; i++){
            bi = bi.next();
            System.out.println(bi.intValue());
        }
    }
}
