/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

/**
 *
 * @author nicholasbartlett
 */
public class ShortMap<E> {
    private short[] keys;
    private E[] values;

    public E get(short key){
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

    public E put(short key, E value){
        if(keys == null){
            keys = new short[]{key};
            values = (E[]) new Object[]{value};

            return null;
        } else if(key > keys[keys.length-1]){
            short[] newKeys;
            int l;
            E[] newValues;

            l = keys.length;
            newKeys = new short[l + 1];
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
                short[] newKeys;
                int l;
                E[] newValues;

                l = keys.length;
                newKeys = new short[l + 1];
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

    private int getIndex(short key){
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

    public short[] keys(){
        return keys;
    }

    public E[] values(){
        return values;
    }

    public void set(short[] keys, E[] values){
        assert checkSet(keys, values);

        this.keys = keys;
        this.values = values;
    }

    public boolean checkSet(short[] keys, E[] values){

        for(int i = 0; i < keys.length-1; i++){
            if(keys[i] >= keys[i + 1]){
                return false;
            }
        }

        return keys.length == values.length;
    }

    public void print(){
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
}
