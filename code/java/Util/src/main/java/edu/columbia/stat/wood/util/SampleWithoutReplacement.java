/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.util;

import java.util.HashSet;

/**
 *
 * @author nicholasbartlett
 */
public class SampleWithoutReplacement {

    public static int[] SampleWithoutReplacement(int n, MersenneTwisterFast rng) {
        HashSet<Integer> set;
        int[] randomOrder;
        int s;
        double rand, cuSum;

        set = new HashSet<Integer>(n);

        for (int i = 0; i < n; i++) {
            set.add(i);
        }

        randomOrder = new int[n];
        s = n;
        while (s > 0) {
            rand = rng.nextDouble();
            cuSum = 0.0;
            for (Integer i : set) {
                cuSum += 1.0 / (double) s;
                if (cuSum > rand) {
                    randomOrder[n - s] = i;
                    set.remove(i);
                    break;
                }
            }
            s--;
            assert s == set.size();
        }
        return randomOrder;
    }
}
