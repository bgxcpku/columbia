/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.finitedepthhpyp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        File f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/mouse/trans_mouse_dna.txt");
        System.out.println(f.length());
        System.out.println(Main.scoreData(Main.trainAndScore(f),f));
    }

    private static void updateContext(int obs, int depth){
        if(cntxt == null){
            cntxt = new int[]{obs};
        } else if(cntxt.length<depth){
            int[] newContext = new int[cntxt.length + 1];
            System.arraycopy(cntxt, 0, newContext, 0, cntxt.length);
            newContext[cntxt.length] = obs;
            cntxt = newContext;
        } else {
            for(int i = 0; i< depth -1; i++){
                cntxt[i] = cntxt[i + 1];
            }
            cntxt[depth-1] = obs;
        }
    }

    private static int[] cntxt;
    private static double scoreData(HPYTree pm, File f) throws IOException {
        double logLik = 0.0;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(f));

            double[] pd = new double[256];
            int depth = pm.getDepth();

            int b;
            while ((b = bis.read()) > -1) {
                pm.getPredDist(pd, cntxt);
                logLik += Math.log(pd[b]);
                Main.updateContext(b,depth);
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
        
        cntxt = null;
        return -logLik / Math.log(2) / f.length();
    }

    private static HPYTree trainAndScore(File f) throws FileNotFoundException, IOException {
        HPYTree pm = new HPYTree(256, 15, 0);
        double logLoss = 0.0;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(f));
            int b;
            while ((b = bis.read()) > -1) {
                logLoss += pm.continueSequence(b);
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
        }

        System.out.println("training entropy = " + -logLoss / Math.log(2) / f.length());
        return pm;
    }
}
