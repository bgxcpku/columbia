/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.util;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */
public class DLMWriter {

    private BufferedWriter bw;
    private String dlm;
    private int cols;
    private int col = 0;
    
    public DLMWriter(File f, int cols, String dlm) throws FileNotFoundException, IOException{
        bw = new BufferedWriter(new FileWriter((f)));
        this.cols = cols;
        this.dlm = dlm;
    }

    public void write(String s) throws IOException{
        if(col == 0){
            bw.write(s, 0, s.length());
            col++;
        } else{
            bw.write(dlm + s, 0, s.length() + dlm.length());
            col++;
            if(col == cols){
                bw.write("\n", 0,1);
                col = 0;
            }
        }
    }

    public void close() throws IOException{
        bw.close();
    }
}
