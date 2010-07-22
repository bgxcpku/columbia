/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.deplump;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Actually decompresses and deplumped file.
 *
 * @author nicholasbartlett
 */
public class Plump {

    /**
     * Decompresses a deplumped file.
     *
     * @param args single deplumped file with full path
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        Plump.Plump(args[0]);
    }


    /**
     * Decompresses a deplumped file.
     * @param file deplumped file with full path
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void Plump(String file) throws FileNotFoundException, IOException {

        PlumpStream ps = null;
        BufferedOutputStream bos = null;
        
        try {
            ps = new PlumpStream(new BufferedInputStream(new FileInputStream(file)));
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