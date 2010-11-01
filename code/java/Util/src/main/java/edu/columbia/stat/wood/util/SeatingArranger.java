/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.util;

/**
 *
 * @author nicholasbartlett
 */
public class SeatingArranger {

    private static double[][] M;
    private static double d;
    private static int c;
    private static int t;
    public static MersenneTwisterFast rng;

    public static int[] getSeatingArrangement(int customers, int tables, double discount) {
        c = customers;
        t = tables;

        d = discount < .001 ? .001 : discount;
        d = d > .999 ? .999 : d;

        int[] sample = new int[tables];

        if (customers < tables || customers <= 0 || tables <= 0) {
            throw new IllegalArgumentException("c = " + customers + ", t = " + tables + ", d = " + discount);
        } else if (tables == 1) {
            sample[0] = customers;
        } else if (tables == customers) {
            for (int i = 0; i < tables; i++) {
                sample[i] = 1;
            }
        } else {

            M = new double[customers][];
            M[customers - 1] = new double[]{1};

            //backward message passing
            for (int i = (c - 2); i > -1; i--) {
                setM(i + 1);
            }

            //forward sampling
            double u, s, r, cuSum;
            boolean up;
            int z = 1;
            sample[0] = 1;

            for (int i = 2; i <= c; i++) {
                s = getM(i, z) * (i - 1 - z * d);
                u = getM(i, z + 1);

                up = rng.nextBoolean(u / (s + u));
                if (up && z < t) {
                    sample[z] = 1;
                    z++;
                } else {
                    r = rng.nextDouble();
                    cuSum = 0.0;
                    for (int j = 0; j < z; j++) {
                        cuSum += ((double) sample[j] - d) / ((double) (i - 1) - z * d);
                        if (cuSum > r) {
                            sample[z - 1]++;
                            break;
                        }

                        if(j == z-1){
                            sample[z-1]++;
                        }
                    }
                }
            }
        }

        assert check(sample);

        return sample;
    }

    public static double getM(int i, int j) {
        int mn = 1 > t - (c - i) ? 1 : t - (c - i);
        int mx = t < i ? t : i;

        if (j < mn || j > mx) {
            return 0.0;
        } else {
            return M[i - 1][j - mn];
        }
    }

    public static void setM(int i) {
        int mn = 1 > t - (c - i) ? 1 : t - (c - i);
        int mx = t < i ? t : i;

        double[] msg = new double[mx - mn + 1];

        double s = 0;
        for (int k = 0; k < (mx - mn + 1); k++) {
            msg[k] = getM(i + 1, mn + k) * (i - (mn + k) * d) + getM(i + 1, mn + k + 1);
            s += msg[k];
        }

        for (int k = 0; k < (mx - mn + 1); k++) {
            msg[k] /= s;
        }

        M[i - 1] = msg;
    }

    public static boolean check(int[] sample) {
        int s = 0;
        for (int ts : sample) {
            s += ts;
            if (ts <= 0) {
                return false;
            }
        }
        System.out.println(s);
        System.out.println(c);
        return s == c;
    }

    public static void main(String[] args) {

        rng = new MersenneTwisterFast(1);

        int[] sample = getSeatingArrangement(10000, 9, .8);
        if (!check(sample)) {
            System.out.println("this sucks");
        }

        System.out.print("[" + sample[0]);
        for (int j = 1; j < sample.length; j++) {
            System.out.print(", " + sample[j]);
        }
        System.out.println("]");



        /*getM(1,5);
        getM(2,5);
        getM(3,5);
        getM(4,5);
        getM(5,5);
        getM(6,5);
        getM(7,5);
        getM(8,5);
        getM(9,5);
        getM(10,5);*/

    }
}
