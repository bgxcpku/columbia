/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.pdia;

/**
 *
 * @author nicholasbartlett
 */
public class IntPair {

    private int f;
    private int s;

    public IntPair(int first, int second){
        f = first;
        s = second;
    }

    public int first(){
        return f;
    }

    public int second(){
        return s;
    }

    public IntPair copy(){
        return new IntPair(f,s);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + this.f;
        hash = 61 * hash + this.s;
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }else if(o.getClass() == this.getClass()){
            IntPair otherPair = (IntPair) o;
            return f == otherPair.first() && s == otherPair.second();
        }else {
            return false;
        }
    }

    @Override
    public String toString(){
        String stringRep = "first = " + f + ", second = " + s + ", hash = " + hashCode();
        return stringRep;
    }
}
