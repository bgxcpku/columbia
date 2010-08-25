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
public class Deplump {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        int numArgs;

        numArgs = args.length;

        if (numArgs == 0) {
            Deplump.DeplumpSTDIN();
        } else {
            for (int i = 0; i < numArgs; i++) {
                Deplump.Deplump(args[i]);
            }
        }
    }

    public static void Deplump(String filename) throws FileNotFoundException, IOException {

        BufferedInputStream bis = null;
        DeplumpStream dps = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(filename));
            dps = new DeplumpStream(new BufferedOutputStream(new FileOutputStream(filename + ".dpl")));

            int streamLength = 0;
            int l;
            byte[] buffer;
            
            buffer = new byte[1024 * 8];
            while((l = bis.read(buffer)) > -1){
                dps.write(buffer,0,l);
                streamLength += l;
                System.out.println(streamLength);
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

    public static void DeplumpSTDIN() throws IOException{

        BufferedInputStream bis = null;
        DeplumpStream dps = null;

        try{
            bis = new BufferedInputStream(System.in);
            dps = new DeplumpStream(new BufferedOutputStream(System.out));

            int l;
            byte[] buffer;

            buffer = new byte[1024 * 8];
            while((l = bis.read(buffer)) > -1){
                dps.write(buffer,0,l);
            }
        } finally {
            if(bis != null){
                bis.close();
            }
            if(dps != null){
                dps.close();
            }
        }
    }
}
