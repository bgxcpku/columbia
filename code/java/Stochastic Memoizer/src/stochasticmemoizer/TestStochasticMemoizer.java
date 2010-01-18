/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stochasticmemoizer;

import java.util.ArrayList;

/**
 *
 * @author nicholasbartlett
 */
public class TestStochasticMemoizer {
    public TestStochasticMemoizer(){
        StochasticMemoizer sm = new StochasticMemoizer(256) ;
        System.out.println("Print current list of children, should be empty") ;
        sm.contextFreeRestaurant.printRestaurantChildren();

        int[] path1 = {1} ;
        int[] path2 = {2} ;
        int[] path3 = {5,4,3,2} ;

        System.out.println("add restaurants to the tree according to path 1,2,3") ;
        System.out.println("path1 and 2 are children of the base, path3 is a child of path 2") ;
        sm.insertContextAndReturnPath(path1) ;
        sm.insertContextAndReturnPath(path2) ;
        sm.insertContextAndReturnPath(path3) ;

        System.out.println("Attempt to re-insert a path which already exists just to make sure it doesn't fail") ;
        ArrayList<Restaurant> ret = sm.insertContextAndReturnPath(path3) ;

        System.out.println("Print children of restaurants in rest path down to rest inserted at path3") ;
        for(int j = 0; j<ret.size() ; ++j){
            ret.get(j).printRestaurantChildren();
            System.out.println() ;
        }

        System.out.println("make sure that the discount parameters in the restaurants are correct") ;
        System.out.println(sm.contextFreeRestaurant.discount) ;
        System.out.println(sm.contextFreeRestaurant.get(2).discount) ;
        System.out.println(sm.contextFreeRestaurant.get(2).get(3).discount) ;
        System.out.println("this last one should be " + sm.discount[4]*sm.discount[3]*sm.discount[2]) ;

        System.out.println() ;
        System.out.println("insert a path which requires you to re-instantiate a restaurant halfway down a path") ;
        int[] path4 = {1,1,3,2} ;
        sm.insertContextAndReturnPath(path4) ;

        System.out.println() ;
        System.out.println("print children of previous parent rest") ;
        sm.contextFreeRestaurant.get(2).printRestaurantChildren();
        System.out.println("print children of newly instated rest") ;
        sm.contextFreeRestaurant.get(2).get(3).printRestaurantChildren();

        System.out.println("print discount parameters") ;
        double d ;
        d = sm.contextFreeRestaurant.get(2).discount ;
        System.out.println(d) ;
        d = sm.contextFreeRestaurant.get(2).get(3).discount ;
        System.out.println(d) ;
        d = sm.contextFreeRestaurant.get(2).get(3).get(4).discount ;
        System.out.println(d) ;
        d = sm.contextFreeRestaurant.get(2).get(3).get(1).discount ;
        System.out.println(d) ;
        System.out.println("this discount ought to be " + sm.discount[4]*sm.discount[3]) ;

        System.out.println() ;
        System.out.println("just to test that the discount updates work correctly over 10, insert some long paths") ;

        int[] path5 = {1,1,1,1,1,1,1,1,1,1,1,1,3,2} ;
        sm.insertContextAndReturnPath(path5) ;

        System.out.println(path5.length) ;
        d = Math.pow(sm.discountInfty, 4) ;
        for(int j = 0; j<6; j++){
            d *= sm.discount[10-j] ;
        }
        System.out.println("discount should be " + d) ;
        System.out.println(sm.contextFreeRestaurant.get(2).get(3).get(1).get(1).discount) ;


        System.out.println() ;
        int[] path6 = {2,2,2,2,2,2,1,1,1,1,1,1,3,2} ;
        ArrayList<Restaurant> restPath = sm.insertContextAndReturnPath(path6) ;
        System.out.println("this discount now ought to be " + sm.discount[5]*sm.discount[6]*sm.discount[7]*sm.discount[8]) ;
        System.out.println(sm.contextFreeRestaurant.get(2).get(3).get(1).get(1).discount) ;

        System.out.println("and both lowest level rest should be now " + d/(sm.contextFreeRestaurant.get(2).get(3).get(1).get(1).discount)) ;
        System.out.println(sm.contextFreeRestaurant.get(2).get(3).get(1).get(1).get(2).discount) ;
        System.out.println(sm.contextFreeRestaurant.get(2).get(3).get(1).get(1).get(1).discount) ;
        System.out.println() ;

        System.out.println("finally, check that the returned path is correct down to the low level rest") ;
        for(int j = 0 ; j<restPath.size() ; j++){
            restPath.get(j).printRestaurantParentPath();
        }

        System.out.println() ;

        sm = new StochasticMemoizer(256) ;
        int[] obs = {1} ;
        sm.seatSequence(obs);
        sm.contextFreeRestaurant.printRestaurantState();
        sm.contextFreeRestaurant.printRestaurantChildren();
        sm.contextFreeRestaurant.printRestaurantParentPath();

        System.out.println() ;

        sm = new StochasticMemoizer(256) ;
        int[] obs2 = {1,1,1} ;
        sm.seatSequence(obs2);
        sm.contextFreeRestaurant.printRestaurantState();
        sm.contextFreeRestaurant.get(1).printRestaurantState();
        sm.contextFreeRestaurant.get(1).get(1).printRestaurantState();

        System.out.println() ;
        sm = new StochasticMemoizer(256) ;
        int[] obs3 = {3,1,2,1,2} ;
        sm.seatSequence(obs3);
        sm.contextFreeRestaurant.printRestaurantState();
        sm.contextFreeRestaurant.get(3).printRestaurantState();
        sm.contextFreeRestaurant.get(3).printRestaurantChildren();  // no children
        sm.contextFreeRestaurant.get(1).printRestaurantState();
        sm.contextFreeRestaurant.get(1).get(3).printRestaurantState();
        sm.contextFreeRestaurant.get(1).get(2).printRestaurantState();
        sm.contextFreeRestaurant.get(2).printRestaurantState();

        System.out.println() ;
        sm = new StochasticMemoizer(10) ;
        int[] obs4 = {0,0,1,0,1,9,3,2,3,1,7,5,8,3,0} ;
        sm.seatSequence(obs4);
        double tot = 0.0 ;
        for(int j = 0 ; j<10; j++){
            System.out.print(sm.predictiveDist[j] + ",") ;
            tot += sm.predictiveDist[j] ;
        }
        System.out.println() ;
        System.out.println(tot) ;
    }
}
