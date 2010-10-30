/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.util;

/**
 * Class to provide static method to get a seating arrangement sampled according to
 * the Chinese restaurant process, conditioned on the number of customers, number of tables
 * and discount parameter.
 *
 * @author nicholasbartlett
 */
public class SeatingArranger_old {

    private static double d;
    
    /**
     * Random number generator used in the stochastic operations of this class.
     */
    public static MersenneTwisterFast rng;

    /**
     * Static method to return a seating arrangement sampled from the Chinese restaurant
     * process.  The seating arrangement is represented as an int[] where each element
     * denotes the number of customers sitting at a unique table.  The number of tables
     * will be equal to the length of the returned array.
     * @param c number of customers
     * @param t number of tables
     * @param d discount
     * @return seating arrangement
     */
    @SuppressWarnings("FinalStaticMethod")
    public static final int[] getSeatingArrangement(int c, int t, double d) {
        int[] tsa;
        ZState state;
        int previousZ;
        double r, cuSum, tw;

        if(c == 0 || t == 0 || t > c){
            throw new IllegalArgumentException("illegal: c = " + c + ", t = " + t + ", d = " + d);
        }

        if (t == 1){
            return new int[]{c};
        } else {
            SeatingArranger_old.d = d <= .99 ? d : .99;

            state = new ZState(t, t);
            state.set(0.0, t);

            previousZ = getSeatingArrangement(tsa = new int[t], c - 1, t, t, state);
            
            if (t > previousZ) {
                tsa[t - 1] = 1;
            } else {
                tw = (double) (c - 1) - d * (double) previousZ;
                r = rng.nextDouble();
                cuSum = 0.0;
                for (int i = 0; i < previousZ; i++) {
                    cuSum += ((double) tsa[i] - d) / tw;
                    if (cuSum > r) {
                        tsa[i]++;
                        break;
                    }
                }
            }

            int cu = 0;
            for(int ts : tsa){
                cu += ts;
            }

            assert cu == c;

            return tsa;
        }
    }

    private static int getSeatingArrangement(int[] tsa, int level, int parentMin, int parentMax, ZState parentState) {
        int min, max, previousZ, z;
        double r, cuSum, tw;
        ZState state;

        if (level == 1) {
            tsa[0] = 1;
            return 1;
        }

        min = parentMin > 1 ? parentMin - 1 : 1;
        max = level < parentMax ? level : parentMax;

        state = new ZState(min, max);

        for (int i = min; i <= max; i++) {
            state.set(LogAdd.logAdd(parentState.get(i) + Math.log(((double) level - d * (double) i)), parentState.get(i + 1)), i);
        }

        previousZ = getSeatingArrangement(tsa, level - 1, min, max, state);

        z = state.sample(previousZ, level);

        if (z > previousZ) {
            tsa[z - 1] = 1;
        } else {
            tw = (double) (level - 1) - d * (double) previousZ;
            r = rng.nextDouble();
            cuSum = 0.0;
            for (int i = 0; i < previousZ; i++) {
                cuSum += ((double) tsa[i] - d) / tw;
                if (cuSum > r) {
                    tsa[i]++;
                    break;
                }
            }
        }
        return z;
    }

    private static class ZState {

        private int min;
        private double[] p;
        private int l;

        ZState(int min, int max) {
            this.min = min;
            l = max - min + 1;
            p = new double[l];
        }

        double get(int zValue) {
            int ind;

            ind = zValue - min;
            if (ind > -1 && ind < l) {
                return p[ind];
            } else {
                return Double.NEGATIVE_INFINITY;
            }
        }

        void set(double value, int zValue) {
            int ind;

            ind = zValue - min;
            if (ind > -1 && ind < l) {
                p[ind] = value;
            } else {
                throw new IllegalArgumentException("zValue out of range");
            }
        }

        int sample(int previousZ, int level) {
            int ind;
            double cuSum, max;

            ind = previousZ - min;

            if (ind == l - 1) {
                return min + l - 1;
            } else if (ind == -1) {
                return min;
            } else {
                p[ind] += Math.log((double) level - 1.0 - d * (double) previousZ);

                max = p[ind] > p[ind + 1] ? p[ind] : p[ind + 1];
                p[ind] -= max;
                p[ind + 1] -= max;

                cuSum = Math.exp(p[ind]) / (Math.exp(p[ind]) + Math.exp(p[ind + 1]));

                if (cuSum > rng.nextDouble()) {
                    return min + ind;
                } else {
                    return min + ind + 1;
                }
            }
        }
    }

    public static void main(String[] args){
        SeatingArranger_old.rng = new MersenneTwisterFast(1);
        int[] sa = SeatingArranger_old.getSeatingArrangement(0, 0, .7);
    }
}
