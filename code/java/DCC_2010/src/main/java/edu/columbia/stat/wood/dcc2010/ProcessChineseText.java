/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.dcc2010;

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
public class ProcessChineseText {

    public static void main(String[] args) throws IOException{
        String path = "/Users/nicholasbartlett/Documents/np_bayes/data/lancaster_corpus/2474/Lcmc/data/character/";
        String[] files = new String[]{"LCMC_A.XML", "LCMC_B.XML","LCMC_C.XML","LCMC_D.XML","LCMC_E.XML","LCMC_F.XML","LCMC_G.XML","LCMC_H.XML","LCMC_J.XML","LCMC_K.XML","LCMC_L.XML", "LCMC_M.XML", "LCMC_N.XML", "LCMC_P.XML","LCMC_R.XML"};

        File out = new File("/Users/nicholasbartlett/Documents/np_bayes/data/lancaster_corpus/concatenated_characters.txt");
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(out));
            for(String file : files){
                System.out.println(file);
                boolean inTag = false;
                bis = new BufferedInputStream(new FileInputStream(new File(path + file)));
                int b;
                int i = 0;
                while((b = bis.read()) > -1){
                    i++;
                    if((char) b == '<'){
                        inTag = true;
                    } else if (inTag && (char) b == '>'){
                        inTag = false;
                    } else if(!inTag){
                        bos.write(b);
                    }
                }
                bis.close();
            }
        } finally {
            bos.close();
            bis.close();
        }
    }

}
