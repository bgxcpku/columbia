/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.rangeencoder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */
public class Plump {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Plump.Plump(args[0]);
    }


    public static void Plump(String file) throws FileNotFoundException, IOException {

        PlumpStream ps = null;
        BufferedOutputStream bos = null;
        
        try {
            ps = new PlumpStream(new File(file));
            file = file.substring(0, file.lastIndexOf('.'));
            bos = new BufferedOutputStream(new FileOutputStream(file));

            int b;
            while((b = ps.read()) > -1){
                bos.write(b);
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