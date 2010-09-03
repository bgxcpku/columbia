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
import java.lang.reflect.InvocationTargetException;

/**
 * @author nicholasbartlett
 */
public class Plump {

    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        ParseReturn parseReturn = CommandLineOptions.parse(args);

        PlumpStream ps = null;
        BufferedOutputStream bos = null;

        if (parseReturn.files != null) {
            for (File f : parseReturn.files) {
                try {
                    ps = new PlumpStream(new BufferedInputStream(new FileInputStream(f)), parseReturn.depth, parseReturn.maxNumberRestaurants, parseReturn.maxSequenceLength, parseReturn.insert, parseReturn.url);
                    File outFile = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf('.')));
                    bos = new BufferedOutputStream(new FileOutputStream(outFile));

                    int l;
                    byte[] buffer = new byte[1024 * 8];
                    while ((l = ps.read(buffer)) > -1) {
                        bos.write(buffer, 0, l);
                    }
                } finally {
                    if (ps != null) {
                        ps.close();
                    }
                    if (bos != null) {
                        bos.close();
                    }
                }
            }
        } else {
            try {
                ps = new PlumpStream(new BufferedInputStream(System.in), parseReturn.depth, parseReturn.maxNumberRestaurants, parseReturn.maxSequenceLength, parseReturn.insert, parseReturn.url);
                bos = new BufferedOutputStream(System.out);

                int l;
                byte[] buffer = new byte[1024 * 8];
                while ((l = ps.read(buffer)) > -1) {
                    bos.write(buffer, 0, l);
                }
            } finally {
                if (ps != null) {
                    ps.close();
                }
                if (bos != null) {
                    bos.close();
                }
            }
        }
    }
}
