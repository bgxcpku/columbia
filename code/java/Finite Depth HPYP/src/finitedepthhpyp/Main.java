/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package finitedepthhpyp;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        int numberOfFilesToSeat = 13 ; //14
        FileTranslatorByte ftb = new FileTranslatorByte() ;
        String[] filesToRead = {"bib", "book1", "book2", "geo", "news","obj1",
            "obj2","paper1","paper2",/*"pic",*/"progc","progl", "progp","trans"} ;
        int[][] translation = ftb.translateFile("/Users/nicholasbartlett/Documents/NP Bayes/data/calgary_corpus/", filesToRead) ;

        int totalBytesTranslated = 0;
        for(int j = 0; j<numberOfFilesToSeat; j++){
            totalBytesTranslated+=translation[j].length;
        }
        System.out.println("Total Bytes Translated = " + totalBytesTranslated) ;

        double avgLogLoss = 0.0 ;
        HPYTree hpy = null ;
        double iterationLogLoss ;
        for(int j = 0; j<numberOfFilesToSeat; j++){
            System.out.println("File " + filesToRead[j] + " is of size " + translation[j].length) ;
            hpy = new HPYTree(256,15) ;
            hpy.seatSeq(translation[j]);
            iterationLogLoss = hpy.logLoss/translation[j].length;
            System.out.println("LogLoss for " + filesToRead[j] +" is = " + iterationLogLoss) ;
            avgLogLoss += hpy.logLoss / totalBytesTranslated;
            System.out.println("Total Number Of Necessary Restaurants calculated is " + hpy.getNumberNecessaryRest());
            hpy = null ;
            translation[j] = null ;
        }

        System.out.println("Total bits per byte is = " + avgLogLoss) ;
    }
}
