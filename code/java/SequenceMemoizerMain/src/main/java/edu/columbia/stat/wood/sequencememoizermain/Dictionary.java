/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizermain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author fwood
 */
public class Dictionary implements Serializable {
    ArrayList<String> type_map;
    HashMap<String,Integer> token_to_type_map;
    
    public Dictionary() {
        type_map = new ArrayList<String>();
        token_to_type_map = new HashMap<String,Integer>();
        type_map.add("EOF");
        token_to_type_map.put("EOF", 0);
        type_map.add("EOL");
        token_to_type_map.put("EOL", 1);

    }
    
    public int lookup(String word) {
        return token_to_type_map.get(word);
    }
    
    private Dictionary(ArrayList<String> type_map,HashMap<String,Integer> token_to_type_map) {
        this.type_map = type_map;
        this.token_to_type_map = token_to_type_map;
    }
    
    public Collection<Integer> getKeys() {
        return token_to_type_map.values();
    }
    
    public int lookupOrAddIfNotFound(String word) {
        Integer i = token_to_type_map.get(word);
        if(i!=null) 
            return i;
        else {
            type_map.add(word);
            int index = type_map.size()-1;
            token_to_type_map.put(word, index);
            return index;
        } 
    }
            
    
    public ArrayList<String>  words() {
        return type_map;
    }
    
    public int size() {
        return type_map.size();
    }
    
    public static Dictionary merge(Dictionary a, Dictionary b) {
        Dictionary c = new Dictionary();
        for(String w : a.words())
            c.addWord(w);
        for (String w : b.words())
            c.addWord(w);
        return c;
    }
    
    
    public void addWord(String word) {
        if(token_to_type_map.get(word) == null) {
            type_map.add(word);
            int index = type_map.size()-1;
            token_to_type_map.put(word, index);
        }            
    }
    
    public void addWords(ArrayList<String> words) {
        
        for(String word : words)
            addWord(word);
    }
    
    
    public String lookup(int id) {
        return type_map.get(id);
    }
    
    public ArrayList<String> lookup(int[] ids) {
        ArrayList<String> ret = new ArrayList<String>(ids.length);
        for(int i=0;i<ids.length;i++)
            ret.add(lookup(ids[i]));
        return ret;
    }
}
