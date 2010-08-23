/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

/**
 *
 * @author nicholasbartlett
 */
public class Pair<F,S> {
    private F first;
    private S second;

    /**
     * @param first first object
     * @param second second object
     */
    public Pair(F first, S second){
        this.first = first;
        this.second = second;
    }

    /**
     * Gets the first object.
     *
     * @return first object
     */
    public F first(){
        return first;
    }

    /**
     * Gets the seond object.
     *
     * @return second object
     */
    public S second(){
        return second;
    }

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        } else if (o.getClass() != this.getClass()){
            return false;
        } else if (((Pair) o).first().equals(first) && ((Pair) o).second().equals(second)){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.first != null ? this.first.hashCode() : 0);
        hash = 41 * hash + (this.second != null ? this.second.hashCode() : 0);
        return hash;
    }
}
