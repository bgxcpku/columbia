/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.ldadhpyplm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author davidpfau
 */
public class Document implements Serializable {
    private ArrayList<Integer> tokens;
    private ArrayList<Integer> domains;
    private int[] numInDomain;
    private int contextLength;
    private static Random rn = new Random();
    private static final long serialVersionUID = 4;
    
    public Document(ArrayList<Integer> tokens, int contextLength, int numDomains) {
        this.tokens = tokens;
        this.contextLength = contextLength;
        this.domains = new ArrayList<Integer>(tokens.size());
        numInDomain = new int[numDomains];
        for(int i = 0; i < tokens.size(); i++) {
            int domain = Math.abs(rn.nextInt()) % numDomains;
            domains.add(domain+1);
            numInDomain[domain]++;
        }
    }
    
    public Document(ArrayList<Integer> tokens, ArrayList<Integer> domains, int contextLength, int numDomains) {
        this.tokens = tokens;
        this.contextLength = contextLength;
        this.domains = new ArrayList<Integer>(tokens.size());
        numInDomain = new int[numDomains];
        for(int i = 0; i < tokens.size(); i++) {
            this.domains.add(domains.get(i)); // since the domain field will be changed, make sure the objects are not just pointers to the same thing
            numInDomain[domains.get(i)-1]++;
        }
    }
    
    public ArrayList<Integer> getTokens() {
        return tokens;
    }
    
    public Integer getToken(int i) {
        return tokens.get(i);
    }
    
    public ArrayList<Integer> getDomains() {
        return domains;
    }
    
    public Integer getDomain(int i) {
        return domains.get(i);
    }
    
    public void setDomain(int i, int domain) {
        int old_domain = domains.get(i);
        domains.set(i, domain);
        // -1 is the "no domain" setting, for when an observation is between assignments
        if(old_domain != -1 && old_domain != 0) {
            numInDomain[old_domain-1]--;
        }
        if(domain != -1 && domain != 0) {
            numInDomain[domain-1]++;
        }
    }
    
    public void setToken(int i, int token) {
        tokens.set(i,token);
    }
    
    public int getNumInDomain(int i) {
        return numInDomain[i-1];
    }
    
    public int[] getNumInDomain() {
        return numInDomain;
    }
    
    public ArrayList<Integer> getContext(int idx) {
        if ( idx >= contextLength ) {
            ArrayList<Integer> ret = new ArrayList<Integer>(contextLength+1);
            for (int i = idx-contextLength; i <= idx; i++) {
                ret.add(tokens.get(i));
            }
            return ret;
        } else {
            ArrayList<Integer> ret = new ArrayList<Integer>(idx+1);
            for (int i = 0; i <= idx; i++) {
                ret.add(tokens.get(i));
            }
            return ret;
        }
    }
    
    public int size() {
        return tokens.size();
    }
    
    public int getContextLength() {
        return contextLength;
    }
}
