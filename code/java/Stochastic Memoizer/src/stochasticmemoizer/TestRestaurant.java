/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stochasticmemoizer;

/**
 *
 * @author nicholasbartlett
 */
public class TestRestaurant {
    public TestRestaurant(){
        /* methods to test are:
         * getNumberCustAtType
         * getNumberCustAtRest
         * getNumberTablesAtRest
         * addNewTable
         * addCustToExistingTable
         * deleteCustFromRest
         * reconfigureRestaurantReturnIntermediateRestaurant */

        System.out.println("Initialize rest1 and rest3, rest3 being rest1's child") ;

        Restaurant rest1 = new Restaurant(null, null,.05) ;
        int[] parentPath = {1,2,3} ;
        Restaurant rest3 = new Restaurant(rest1,parentPath,.7) ;
        rest1.put(new Integer(3), rest3) ;

        System.out.println("seat some customers in each restaurant in an acceptable seating arrangement") ;
        
        rest3.addNewTable(new Integer(1)) ;
        rest1.addNewTable(new Integer(1)) ;

        rest3.addNewTable(new Integer(2)) ;
        rest1.addNewTable(new Integer(2)) ;

        rest3.addNewTable(new Integer(3)) ;
        rest1.addNewTable(new Integer(3)) ;

        rest3.addCustToExistingTable(new Integer(2)) ;
        rest3.addCustToExistingTable(new Integer(3)) ;
        rest1.addCustToExistingTable(new Integer(3)) ;
        
        System.out.println("show children of rest1") ;
        rest1.printRestaurantChildren() ;

        System.out.println("show state of rest3") ;
        rest3.printRestaurantState();

        int[] newParentPath = {3} ;

        System.out.println("insert a rest2 between rest 1 and rest 3 and then print the children of both rest 1 and 2") ;
        Restaurant rest2 = rest3.reconfigureRestaurantReturnIntermediateRestaurant(newParentPath, .75) ;
        rest1.printRestaurantChildren() ;
        rest2.printRestaurantChildren() ;

        System.out.println("show new state of rest 3") ;
        rest3.printRestaurantState();

        System.out.println("show state of rest 2") ;
        rest2.printRestaurantState();

        System.out.println("show new discount parameter of rest 3") ;
        System.out.println(rest3.discount) ;

        System.out.println("check that rest 3 has a new parent, rest 2, by looking at children of rest3's parent") ;
        rest3.parentRestaurant.printRestaurantChildren();

    }
}
