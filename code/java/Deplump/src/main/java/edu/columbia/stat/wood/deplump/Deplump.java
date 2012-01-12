/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.deplump;

import edu.columbia.stat.wood.deplump.CommandLineOptions.ParseReturn;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author nicholasbartlett
 */
public class Deplump {

    public static void main(String[] args) throws Exception {
        ParseReturn parseReturn = CommandLineOptions.parse(args);

        BufferedInputStream bis = null;
        DeplumpStream dps = null;

        if (parseReturn.files != null) {
            for (File f : parseReturn.files) {
                try {
                    if(!f.exists()) {
                        System.err.println("Input file "+f+" does not exist.  Terminating early");
                        throw new FileNotFoundException();
                    }
                    bis = new BufferedInputStream(new FileInputStream(f));
                    File outFile = new File(f.getAbsolutePath() + ".dpl");
                    dps = new DeplumpStream(new BufferedOutputStream(new FileOutputStream(outFile)), parseReturn.depth, parseReturn.maxNumberRestaurants, parseReturn.maxSequenceLength, parseReturn.insert, parseReturn.url);

                    int l;
                    byte[] buffer;

                    buffer = new byte[1024 * 16];
                    while ((l = bis.read(buffer)) > -1) {
                        dps.write(buffer, 0, l);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    bis.close();
                    dps.close();
                    throw e;
                }
                if (parseReturn.saveModel) {
                    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(parseReturn.modelSaveFile)));
                    try {
                        oos.writeObject(dps.getModel());
                        oos.flush();
                        oos.close();
                    } catch (IOException ioe) {
                        System.err.println("Error writing model file -- probably corrupt -- try again");
                        throw ioe;
                    }
                }
            }
        } else {
            try {
                bis = new BufferedInputStream(System.in);
                dps = new DeplumpStream(new BufferedOutputStream(System.out), parseReturn.depth, parseReturn.maxNumberRestaurants, parseReturn.maxSequenceLength, parseReturn.insert, parseReturn.url);

                int l;
                byte[] buffer;

                buffer = new byte[1024 * 16];
                while ((l = bis.read(buffer)) > -1) {
                    dps.write(buffer, 0, l);
                }
            } catch (Exception e) {

                if (bis != null) {
                    bis.close();
                }
                if (dps != null) {
                    dps.close();
                }
                throw e;
            }
        }
    }
}
