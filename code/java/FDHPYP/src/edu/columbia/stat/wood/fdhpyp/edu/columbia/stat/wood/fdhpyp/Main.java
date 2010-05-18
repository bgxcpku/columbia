/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.fdhpyp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        File f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/AliceInWonderland.txt");
        BufferedInputStream bis = null;

        RestaurantFranchise rf = new RestaurantFranchise(10);
        double logLik = 0.0;
        try {
            bis = new BufferedInputStream(new FileInputStream(f));
            int b;
            int ind = 0;
            while ((b = bis.read()) > -1 ) {
                logLik += rf.continueSequence(b);
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
        }

        System.out.println(-logLik / Math.log(2) / f.length());
        System.out.println(Restaurant.restCount);
        System.out.println(rf.logLik());
        rf.sample();
        System.out.println("done");
    }
}
