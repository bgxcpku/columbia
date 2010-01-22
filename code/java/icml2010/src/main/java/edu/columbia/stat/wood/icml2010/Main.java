/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.icml2010;

import java.io.FileNotFoundException;
import java.io.IOException;
import edu.columbia.stat.wood.sequencememoizer.*;

/**
 *
 * @author nicholasbartlett
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        int numberOfFilesToSeat = 4 ; //14
        FileTranslatorByte ftb = new FileTranslatorByte() ;
        String[] filesToRead = {/*"bib", "book1", "book2", "geo", "news","obj1",
            "obj2","paper1","paper2","pic",*/ "progc","progl", "progp","trans"} ;
        int[][] translation = ftb.translateFile("/Users/nicholasbartlett/Documents/NP Bayes/data/calgary_corpus/", filesToRead, numberOfFilesToSeat) ;

        int totalBytesTranslated = 0;
        for(int j = 0; j<numberOfFilesToSeat; j++){
            totalBytesTranslated+=translation[j].length;
        }
        System.out.println("Total Bytes Translated = " + totalBytesTranslated) ;

        double avgLogLoss = 0.0 ;
        StochasticMemoizer sm = null ;
        double iterationLogLoss ;
        for(int j = 0; j<numberOfFilesToSeat; j++){
            System.out.println("File " + filesToRead[j] + " is of size " + translation[j].length) ;
            sm = new StochasticMemoizer(256, 5) ;

            sm.seatSequnce(translation[j]);
            //sm.seatSequnceWithRandomDeletionOfRestaurants(translation[j],100000) ;
            //sm.seatSequnceWithRandomEntireDeletionOfRestaurants(translation[j], 120000);
            //sm.seatSequenceWithDeletionOfUnusedRestaurants(translation[j],100000) ;
            //sm.seatSequenceWithDeletionOfUnhelpfulRestaurants(translation[j],10000) ;

            System.out.print("Discounts = ");
            sm.discounts.printArray(sm.discounts.discounts);

            System.out.println(Restaurant.numberRest);

            iterationLogLoss = sm.logLoss/translation[j].length;
            System.out.println("LogLoss for " + filesToRead[j] +" is = " + iterationLogLoss) ;
            avgLogLoss += sm.logLoss / totalBytesTranslated;
            sm = null ;
            translation[j] = null ;
        }

        System.out.println(Restaurant.numberRest);
        System.out.println("Total bits per byte is = " + avgLogLoss) ;
    }

}
