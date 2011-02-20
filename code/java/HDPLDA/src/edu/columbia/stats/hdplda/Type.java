/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;

/**
 *
 * @author nicholasbartlett
 */

public class Type {

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Type)
            return type == ((Type)obj).type;
        else
            return false;
    }

    @Override
    public int hashCode() {
        return type;
    }



    int type ;
    public Type(int val){
        this.type = val ;
    }

    public int getType() {
        return type;
    }

}
