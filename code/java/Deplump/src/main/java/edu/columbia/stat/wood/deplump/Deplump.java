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

/**
 * @author nicholasbartlett
 */
public class Deplump {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        ParseReturn parseReturn = CommandLineOptions.parse(args);

        BufferedInputStream bis = null;
        DeplumpStream dps = null;

        if (parseReturn.files != null) {
            for (File f : parseReturn.files) {
                try {
                    bis = new BufferedInputStream(new FileInputStream(f));
                    File outFile = new File(f.getAbsolutePath() + ".dpl");
                    dps = new DeplumpStream(new BufferedOutputStream(new FileOutputStream(outFile)), parseReturn.depth, parseReturn.maxNumberRestaurants, parseReturn.maxSequenceLength, parseReturn.insert, parseReturn.url);

                    int l;
                    byte[] buffer;

                    buffer = new byte[1024 * 16];
                    while ((l = bis.read(buffer)) > -1) {
                        dps.write(buffer, 0, l);
                    }
                } finally {
                    bis.close();
                    dps.close();
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
}
