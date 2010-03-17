/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.finitedepthhpyp;

/**
 *
 * @author nicholasbartlett
 */
public class Concentrations {

    public double[] concentrations;

    public Concentrations() {
        concentrations = new double[]{0.0};
    }

    public Concentrations(double[] concentrations) {
        this.concentrations = concentrations;
    }

    public double get(int i) {
        if (i < concentrations.length) {
            return concentrations[i];
        } else {
            return concentrations[concentrations.length - 1];
        }
    }

    public double getLog(int i) {
        if (i < concentrations.length) {
            return Math.log(concentrations[i]);
        } else {
            return Math.log(concentrations[concentrations.length - 1]);
        }
    }

    public void set(double newVal, int i) {
        concentrations[i] = newVal;
    }

    public void clear() {
        for (int i = 0; i < concentrations.length; i++) {
            concentrations[i] = 0.0;
        }
    }

    public boolean isConcentrationInRange() {
        for (int i = 0; i < concentrations.length; i++) {
            if (concentrations[i] < 0.0) {
                return false;
            }
        }
        return true;
    }

    public void print() {
        System.out.print("Concentrations = [");
        for (int i = 0; i < concentrations.length; i++) {
            if (i == 0) {
                System.out.print(concentrations[i]);
            } else {
                System.out.print(", " + concentrations[i]);
            }
        }
        System.out.println("]");
    }
}
