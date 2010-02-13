/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.ldadhpyplm;

/**
 *
 * @author davidpfau
 */
public class Token {
    //Just a container class so that a HashMap won't overwrite tokens with the same type
    private int type;
    
    public Token(int type) {
        this.type = type;
    }
    
    public int getType() {
        return type;
    }
}
