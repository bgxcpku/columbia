/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stochasticmemoizer3;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author nicholasbartlett
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        /*FileTranslator ft = new FileTranslator() ;
        Pair<int[],Integer> translation = ft.translateFile("/Users/nicholasbartlett/Documents/NP Bayes/data/pride_and_prejudice/", "pride_and_prejudice.txt") ;

        for(int i = 0; i<35;i++){
            System.out.print("," + translation.first()[i]) ;
        }
        System.out.println() ;
        System.out.println(translation.first().length) ;
        System.out.println(translation.second()) ;

        int seatNumber = 717581 ; //717581 ;
        int[] toBeSeated = new int[seatNumber] ;
        for(int j = 0 ; j<seatNumber; j++){
            toBeSeated[j] = translation.first()[j] ;
        }
        */

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
        StochasticMemoizer sm = null ;
        double iterationLogLoss ;
        for(int j = 0; j<numberOfFilesToSeat; j++){
            System.out.println("File " + filesToRead[j] + " is of size " + translation[j].length) ;
            sm = new StochasticMemoizer(256) ;
            //sm.seatSequnce(translation[j]);
            //sm.seatSequnceWithRandomDeletionOfRestaurants(translation[j],100000) ;
            //sm.seatSequenceWithDeletionOfUnusedRestaurants(translation[j],100000) ;
            iterationLogLoss = sm.logLoss/translation[j].length;
            System.out.println("LogLoss for " + filesToRead[j] +" is = " + iterationLogLoss) ;
            avgLogLoss += sm.logLoss / totalBytesTranslated;
            sm = null ;
            translation[j] = null ;
        }

        System.out.println("Total bits per byte is = " + avgLogLoss) ;
    }
}
