/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stochasticmemoizer;

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
        // TODO code application logic here
       
        //TestRestaurant test = new TestRestaurant() ;
        //TestStochasticMemoizer tsm = new TestStochasticMemoizer() ;
        
        FileTranslator ft = new FileTranslator() ;
        Pair<int[],Integer> translation = ft.translateFile("/Users/nicholasbartlett/Desktop/", "AliceInWonderland.txt") ;

        int[] junk = translation.first() ;

        for(int i = 0; i<35;i++){
            System.out.print("," + junk[i]) ;
        }
        System.out.println() ;
        System.out.println(junk.length) ;
        System.out.println(translation.second()) ;

        int seatNumber = 30000 ;
        int[] toBeSeated = new int[seatNumber] ;
        for(int j = 0 ; j<seatNumber; j++){
            toBeSeated[j] = translation.first()[j] ;
        }
        StochasticMemoizer sm = new StochasticMemoizer(translation.second().intValue()) ;
        //double logLoss = sm.seatSequence(translation.first());
        double logLoss = sm.seatSequence(toBeSeated);
        System.out.println() ;
        System.out.println(logLoss/seatNumber) ;

  //      sm.printTree();
        //sm.printRestAndChildren(sm.contextFreeRestaurant, 0, 2);

        //Calculate marginal rate of compression over first 10,000 characters.
        /*double totLogLossOver10 = 10000.0*(-3.0040485624398676) ;
        double totLogLossOver20 = logLoss - totLogLossOver10 ;
        System.out.println("Marginal Rate for second 20,000 = " + totLogLossOver20/20000.0) ;*/
    }

}

