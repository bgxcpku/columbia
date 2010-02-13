/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.ldadhpyplm;

import java.util.StringTokenizer;

/**
 *
 * @author davidpfau
 */
public class Parameters {
    
    public double[][] discount;
    public double[][] concentration;
    
    public double[] switchDiscount;
    public double[] switchConcentration;
    
    public Parameters(String input, int contextLength, int numDomains) {
        
        discount = new double[numDomains+1][contextLength+1];
        concentration = new double[numDomains+1][contextLength+1];
        
        switchDiscount = new double[contextLength+1];
        switchConcentration = new double[contextLength+1];
        
        String[] inputs = input.split("\n");
        String names  = inputs[0];
        String values = inputs[1];
        
        String[] name  = names.split(",");
        String[] value = values.split(",");
        
        int domain = 0; 
        int depth = 0;
        try {
            for(int i = 0; i < name.length-1; i++) {
                name[i] = name[i].trim();
                value[i] = value[i].trim();
                StringTokenizer strtok = new StringTokenizer(name[i]);
                String context = strtok.nextToken();
                if (context.equals("domain")) {
                    domain = Integer.parseInt(strtok.nextToken());
                }
                String discOrConc = strtok.nextToken();
                strtok.nextToken(); //next word is always "depth" which can be ignored
                depth = Integer.parseInt(strtok.nextToken());
                if (discOrConc.equals("discount")) {
                    if(context.equals("domain")) {
                        discount[domain][depth] = Double.parseDouble(value[i]);
                    } else if (context.equals("switch")) {
                        switchDiscount[depth] = Double.parseDouble(value[i]);
                    }
                } else if (discOrConc.equals("concentration")) {
                    if(context.equals("domain")) {
                        concentration[domain][depth] = Double.parseDouble(value[i]);
                    } else if (context.equals("switch")) {
                        switchConcentration[depth] = Double.parseDouble(value[i]);
                    }
                    
                }
            }
        } catch (Exception e) {
            System.err.println("Parameter string is not properly formatted!");
            System.exit(-1);
        }
    }
}
