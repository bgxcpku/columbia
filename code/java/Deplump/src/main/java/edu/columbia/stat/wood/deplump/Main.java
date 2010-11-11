/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */
public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        long startTime = System.nanoTime();
        String in_string = args[0];
        String out_string = args[1];

        File in = new File(in_string);
        File out = new File(out_string);

        BufferedInputStream bis = null;
        DeplumpStream ds = null;

        PlumpStream ps = null;
        BufferedOutputStream bos = null;
        
        try{
            int i = 1;
            bis = new BufferedInputStream(new FileInputStream(in));
            ds = new DeplumpStream(new BufferedOutputStream(new FileOutputStream(out)));
            byte[] buffer = new byte[1024 * 1024];
            int l;
            long count = 0;
            while((l = bis.read(buffer)) > -1){
                ds.write(buffer, 0, l);
                count += l;

                int chunkSize = 10000000;
                if(count / chunkSize > i){
                    System.out.println("count = " + count + ", time = " + (System.nanoTime() - startTime));
                    i = (int) (count / chunkSize);
                    i++;
                }
                
            }
            ds.close();
            bis.close();

            /*ps = new PlumpStream(new BufferedInputStream(new FileInputStream(out)));
            bos = new BufferedOutputStream(new FileOutputStream(new File(out_string + ".pl")));
            while((l = ps.read(buffer)) > -1){
                bos.write(buffer,0,l);
            }*/

        } finally {
            bis.close();
            ds.close();
            //bos.close();
            //ps.close();
        }
    }
}
