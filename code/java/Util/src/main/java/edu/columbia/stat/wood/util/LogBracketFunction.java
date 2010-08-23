/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;

/**
 *
 * @author nicholasbartlett
 */
public class LogBracketFunction {

    public static double logBracketFunction(double arg, int superScript, double subScript){
        double lbf = 0.0;

        for(int i = 0; i < superScript; i++){
            lbf += Math.log(arg);
            arg += subScript;
        }

        return lbf;
    }
}
