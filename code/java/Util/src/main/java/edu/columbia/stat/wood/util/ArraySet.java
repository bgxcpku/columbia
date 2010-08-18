/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

/**
 *
 * @author nicholasbartlett
 */
public class ArraySet <E> {
    private E[] values;
    private IntStack availableIndices;
    private int count;

    public ArraySet(int maxSize){
        values = (E[]) new Object[maxSize];
        count = 0;
    }

    private void initializeAvailableIndices(){
        availableIndices = new IntStack(8);
        for(int i = count; i < values.length; i++){
            availableIndices.push(i);
        }
    }

    public void add(E e){
        if(availableIndices == null){
            values[count++] = e;
        } else if(availableIndices.hasNext()){
            values[availableIndices.pop()] = e;
        } else {
            throw new RuntimeException("Set is full");
        }
    }

    public E get(int index){
        return values[index];
    }

    public void remove(int index){
        values[index] = null;

        if(availableIndices == null) {
            initializeAvailableIndices();
        }

        availableIndices.push(index);
    }

    public int size(){
        if(availableIndices != null) {
            return values.length - availableIndices.size();
        } else {
            return count ;
        }
    }

    public int maxIndex(){
        return values.length - 1;
    }

    public static void main(String[] args){
        ArraySet<Integer> as = new ArraySet(100);

        for(int i = 0; i< 100; i++){
            as.add(new Integer(i));
        }

        System.out.println(as.size());
        
        for(int i =0 ; i< 100; i++){
            System.out.println(as.get(i));
        }

       as.remove(15);

        as.add(new Integer(177));

        System.out.println(as.get(15));
    }
}
