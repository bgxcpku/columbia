/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */
public class LogGeneralizedSterlingNumbers {
    private HashMap<Pair<Integer, Integer>, Double> lookup ;
    private double d;

    public LogGeneralizedSterlingNumbers(double discount){
        if(discount <= 0.0 || discount >= 1.0){
            throw new IllegalArgumentException("Discount must be in (0,1.0)");
        }
        d = discount;
        lookup = new HashMap<Pair<Integer,Integer>, Double>();
    }

    public double get(int c, int t){
        if(c <= 0 || t <= 0 || c < t){
            return Double.NEGATIVE_INFINITY;
        } else if (c == t){
            return 0.0;
        } else {
            Double answer;
            Pair key;
        
            key = new Pair(c, t);
            answer = lookup.get(key);

            if(answer == null){
                answer = LogAdd.logAdd(get(c-1,t-1), Math.log(c - 1 - d * t) + get(c-1, t));
                lookup.put(key,answer);
            }
            return answer;
        }
    }

    public static void main(String[] args){
        LogGeneralizedSterlingNumbers j = new LogGeneralizedSterlingNumbers(0.75);

        System.out.println(j.get(2005, 708));
        System.out.println(j.lookup.size());
    }
}
