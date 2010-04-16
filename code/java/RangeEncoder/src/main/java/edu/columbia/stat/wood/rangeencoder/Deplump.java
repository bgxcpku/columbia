/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.rangeencoder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */
public class Deplump {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Deplump.Deplump(args[0]);
    }

    public static void Deplump(String filename) throws FileNotFoundException, IOException {

        BufferedInputStream bis = null;
        DeplumpStream dps = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(filename), 1024 * 512);
            dps = new DeplumpStream(new BufferedOutputStream(new FileOutputStream(filename + ".deplump"), 1024 * 512));

            int b;
            while ((b = bis.read()) > -1) {
                dps.write(b);
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (dps != null) {
                dps.close();
            }
        }

        System.out.println(Long.toBinaryString(dps.enc.minRange).length());
        System.out.println((double) dps.enc.bitsEmitted / 8);
        System.out.println(dps.enc.logLoss / 8);
    }
}
