/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

/**
 * 
 * @author nicholasbartlett
 */

public class DoubleStack {
    private double[] stack;
    private int index, l;

    public DoubleStack(){
        stack = new double[1024];
        l = 1024;
        index = -1;
    }

    public boolean hasNext(){
        return index > -1;
    }

    public double peak(){
        return stack[index];
    }

    public void push(double d){
        if(index == l-1){
            double[] newStack;

            newStack = new double[l + 1024];
            System.arraycopy(stack, 0, newStack, 0, l);
            l += 1024;
            stack = newStack;
        }
        
        stack[++index] = d;
    }

    public double pop(){
        return stack[index--];
    }
    
    public int index(){
        return index;
    }
    
    public void setIndex(int index){
        this.index = index;
    }

    public void print(){
        System.out.print("[" + stack[0]);
        for(int i = 1; i <= index; i++){
            System.out.print(", " + stack[i]);
        }
        System.out.println("]");
    }

    public static void main(String[] args){
        DoubleStack bs = new DoubleStack();

        for(int i = 0; i<1025; i++){
            bs.push(i);
        }

        System.out.println(bs.index());
        System.out.println(bs.l);


    }
}
