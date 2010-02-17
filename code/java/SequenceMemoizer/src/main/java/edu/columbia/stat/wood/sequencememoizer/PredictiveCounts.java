/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public class PredictiveCounts {
    public double[] typeNum;
    public double discount;
    public double concentration;
    public int cust;
    public int tables;

    public void increment(int type, boolean tableAdd){
        if(tableAdd){
            typeNum[type] += 1-discount;
            cust++;
            tables++;
        } else {
            typeNum[type]++;
            cust++;
        }
    }

    public void decrement(int type, boolean tableDelete){
        if(tableDelete){
            typeNum[type] -= 1-discount;
            cust--;
            tables--;
        } else {
            typeNum[type]--;
            cust--;
        }
    }
}