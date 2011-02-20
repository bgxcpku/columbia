/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author nicholasbartlett
 */
public class Document extends HashMap<Type,SetableInteger> {
    
    public Document(int[] obs){
        super();
        for(int i = 0;i<obs.length;i++)
        {
            Type type = new Type(i);
            SetableInteger count = get(type);
            if(count!= null) {
                count.increment();
            } else {
                put(type,new SetableInteger(1));
            }

        }
    }

    public HashMap<Type,SetableInteger> contents() {
        return this;
    }

    public void plus(Document obsToAdd) {
        HashSet<Type> allKeys = new HashSet<Type>();
        allKeys.addAll(keySet());
        allKeys.addAll(obsToAdd.keySet());
        for(Type type : allKeys) {
            SetableInteger my_count = get(type);
            SetableInteger their_count = obsToAdd.get(type);

            if (my_count == null) {
                if (their_count != null ) {
                    put(type,their_count);
                } else {
                    assert false : "This condition should never be reached";
                }
            } else {
                if (their_count != null ) {
                    my_count.add(their_count);
                } else {
                        // do nothing -- the count is good as is
                }
            }
        }
    }

    public void plus(Type typeToAdd) {
        SetableInteger count = get(typeToAdd);
        if(count!=null)
            count.increment();
        else
            put(typeToAdd,new SetableInteger(1));
    }

  // fails silently if you attempt to subtract a word that isn't there
        public void minus(Type typeToAdd) {
        SetableInteger count = get(typeToAdd);
        if(count!=null)
            count.decrement();
                }


    public void minus(Document obsToAdd) {
        throw new UnsupportedOperationException();
    }

}
