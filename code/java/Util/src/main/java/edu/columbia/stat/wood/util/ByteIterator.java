/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

/**
 * Very simple byte iterator interface.
 * @author nicholasbartlett
 */
public interface ByteIterator {

    /**
     * @return true if there is a next element, else false
     */
    public boolean hasNext();

    /**
     * @return next byte
     */
    public byte next();
}
