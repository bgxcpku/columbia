/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.util.Iterator;

/**
 * Iterator object for the PDF of a discrete distribution.
 * 
 * @author nicholasbartlett
 */
public abstract class DiscretePDFIterator implements Iterator<Pair<Integer, Double>> {
    
    /**
     * Unsupported.
     */
    public void remove(){
        throw new UnsupportedOperationException("This method is unsupported");
    }
    
}
