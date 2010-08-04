/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public class IntegerPair {

    int first;
    int second;

    public IntegerPair(int c, int t){
        this.first = c;
        this.second = t;
    }

    public int first(){
        return first;
    }

    public int second(){
        return second;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj.getClass() == getClass()){
            if(first == ((IntegerPair) obj).first() && second == ((IntegerPair) obj).second()){
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.first;
        hash = 97 * hash + this.second;
        return hash;
    }    
}
