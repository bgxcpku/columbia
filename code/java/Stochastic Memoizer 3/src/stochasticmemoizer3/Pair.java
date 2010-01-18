/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stochasticmemoizer3;

/**
 *
 * @author nicholasbartlett
 */
public class Pair<E,V> {
    E e;
    V v;

    public Pair(E e, V v) {
        this.e = e;
        this.v = v;
    }

    public E first() {
        return e;
    }

    public V second() {
        return v;
    }
}
