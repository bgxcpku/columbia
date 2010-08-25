/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author nicholasbartlett
 */
public class ByteMap<E>{
    private byte[] keys;
    private E[] values;

    public boolean isEmpty(){
        return keys == null;
    }

    public int size(){
        if(keys != null){
            return keys.length;
        } else{
            return 0;
        }
    }

    public E get(byte key){
        if(keys == null || key > keys[keys.length-1]){
            return null;
        } else {
            int index;

            index = getIndex(key);

            if(keys[index] == key){
                return values[index];
            } else {
                return null;
            }
        }
    }

    public E put(byte key, E value){
        if(keys == null){
            keys = new byte[]{key};
            values = (E[]) new Object[]{value};

            return null;
        } else if(key > keys[keys.length-1]){
            byte[] newKeys;
            int l;
            E[] newValues;

            l = keys.length;
            newKeys = new byte[l + 1];
            newValues = (E[]) new Object[l + 1];

            System.arraycopy(keys, 0, newKeys, 0, l);
            System.arraycopy(values, 0, newValues, 0, l);
            newKeys[l] = key;
            newValues[l] = value;

            keys = newKeys;
            values = newValues;

            return null;
        } else {
            int index;

            index = getIndex(key);

            if(keys[index] == key){
                E returnValue;

                returnValue = values[index];
                values[index] = value;

                return returnValue;
            } else {
                byte[] newKeys;
                int l;
                E[] newValues;

                l = keys.length;
                newKeys = new byte[l + 1];
                newValues = (E[]) new Object[l + 1];

                System.arraycopy(keys, 0, newKeys, 0, index);
                System.arraycopy(values, 0, newValues, 0, index);
                newKeys[index] = key;
                newValues[index] = value;
                System.arraycopy(keys, index, newKeys,index + 1, l - index);
                System.arraycopy(values, index, newValues, index + 1, l-index);

                keys = newKeys;
                values = newValues;
                return null;
            }
        }
    }

    public void remove(byte key){
        if(keys == null){
            throw new IllegalArgumentException("Key to remove is not in map");
        } else if (keys.length == 1){
            if(key == keys[0]){
                 keys = null;
                 values = null;
            } else {
                throw new IllegalArgumentException("Key to remove is not in map");
            }
        } else if(key > keys[keys.length-1]){
            throw new IllegalArgumentException("Key to remove is not in map");
        } else {
            int index;

            index = getIndex(key);
            if(key != keys[index]){
                throw new IllegalArgumentException("Key to remove is not in map");
            } else {
                byte[] newKeys;
                E[] newValues;
                int l;

                l = keys.length;
                newKeys = new byte[l - 1];
                newValues = (E[]) new Object[l - 1];

                System.arraycopy(keys, 0, newKeys, 0, index);
                System.arraycopy(keys, index + 1, newKeys, index, l - index - 1);
                System.arraycopy(values, 0, newValues, 0, index);
                System.arraycopy(values, index + 1, newValues, index, l-index -1);

                keys = newKeys;
                values = newValues;
            }
        }
    }

    private int getIndex(byte key){
        int l, r, midPoint;

        l = 0;
        r = keys.length - 1;

        assert key <= keys[keys.length-1];

        while(l < r){
            midPoint =(l + r) / 2;
            if(key > keys[midPoint]){
                l = midPoint + 1;
            } else {
                r = midPoint;
            }
        }

        return l;
    }

    public byte[] keys(){
        return keys;
    }

    public Collection<E> values(){
        HashSet<E> valueSet;

        valueSet = new HashSet<E>();
        if(values != null){
            for(int i = 0; i < values.length; i++){
                valueSet.add(values[i]);
            }
        }

        if(values != null){
            assert valueSet.size() == values.length;
        } else {
            assert valueSet.size() == 0;
        }

        return valueSet;
    }

    public Object[] arrayValues(){
        return values;
    }

    public void set(byte[] keys, E[] values){
        assert checkSet(keys, values);

        this.keys = keys;
        this.values = values;
    }

    public boolean checkSet(byte[] keys, E[] values){

        for(int i = 0; i < keys.length-1; i++){
            if(keys[i] >= keys[i + 1]){
                return false;
            }
        }

        return keys.length == values.length;
    }

    public void print(){
        if(values == null){
            System.out.println("Keys : " + keys);
            System.out.println("Values : " + values);
            return;
        }

        System.out.print("Keys : [" + keys[0]);
        for(int i = 1; i < keys.length; i++){
            System.out.print(", " + keys[i]);
        }
        System.out.println("]");

        System.out.print("Values : [" + values[0]);
        for(int i = 1; i < values.length; i++){
            System.out.print(", " + values[i]);
        }
        System.out.println("]");
    }

    public E getRandomValue(MersenneTwisterFast rng){
        double r = rng.nextDouble(), cuSum = 0.0, l = keys.length;

        for(E value : values){
            cuSum += 1.0 / l;
            if(cuSum > r){
                return value;
            }
        }

        throw new RuntimeException("Should not make it to this part of the method.");
    }

    public static void main(String[] args){
        ByteMap bm = new ByteMap();

        for(int i = 0; i < 25; i++){
            bm.put((byte) i, new Integer(i));
        }

        bm.print();
        System.out.println(bm.size());

        for(int i = 24; i> -1; i--){
            bm.remove((byte) i);
            bm.print();
        }

        System.out.println(bm.size());
    }
}
