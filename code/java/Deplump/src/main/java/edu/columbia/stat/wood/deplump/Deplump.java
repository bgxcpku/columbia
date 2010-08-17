/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.deplump;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Compresses a file using the sequence memoizer predictive model.
 *
 * @author nicholasbartlett
 */
public class Deplump {

    /**
     * Deplumps a single file.
     *
     * @param args single file with full path
     * @throws FileNotFoundException
     * @throws IOException
     */

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Deplump.Deplump(args[0]);
    }

    /**
     * Deplumps a single file.
     *
     * @param filename full path of file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void Deplump(String filename) throws FileNotFoundException, IOException {

        BufferedInputStream bis = null;
        DeplumpStream dps = null;
        File f = new File(filename);

        try {
            bis = new BufferedInputStream(new FileInputStream(filename));
            dps = new DeplumpStream(new BufferedOutputStream(new FileOutputStream(filename + ".deplump")));

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
        
    }
}
