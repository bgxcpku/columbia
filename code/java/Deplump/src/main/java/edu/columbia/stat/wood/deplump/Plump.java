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
 * @author nicholasbartlett
 */
public class Plump {

    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        int numArgs;

        numArgs = args.length;
        
        if (numArgs == 0){
            Plump.PlumpSTDIN();
        } else {
            for(int i = 0; i < numArgs; i++){
                Plump.Plump(args[i]);
            }
        }
    }

    public static void Plump(String file) throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        PlumpStream ps = null;
        BufferedOutputStream bos = null;
        
        try {
            ps = new PlumpStream(new BufferedInputStream(new FileInputStream(file)));
            file = file.substring(0, file.lastIndexOf('.'));
            bos = new BufferedOutputStream(new FileOutputStream(file));

            int l;
            byte[] buffer = new byte[1024 * 8];
            while((l = ps.read(buffer)) > -1){
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

    public static void PlumpSTDIN() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{

        PlumpStream ps = null;
        BufferedOutputStream bos = null;

        try{
            ps = new PlumpStream(new BufferedInputStream(System.in));
            bos = new BufferedOutputStream(System.out);

            int l;
            byte[] buffer = new byte[1024 * 8];
            while((l = ps.read(buffer)) > -1){
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