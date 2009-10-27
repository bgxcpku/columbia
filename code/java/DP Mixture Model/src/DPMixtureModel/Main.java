/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DPMixtureModel;

/**
 *
 * @author nicholasbartlett
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        Restaurant rest = new Restaurant() ;
        System.out.println(rest.numTables) ;
        System.out.println(rest.numAtTables) ;

        rest.addTable() ;
        
        System.out.println(rest.numTables) ;
        System.out.println(rest.numAtTables) ;

        rest.seatAtTable(1, 15.2, 0) ;
        rest.seatAtTable(3, -1.5, 0) ;
        rest.addTable() ;
        rest.seatAtTable(12,12.21,1) ;
        
        System.out.println(rest.obsAtTables()) ;
        System.out.println(rest.indexTable(3)) ;
    }

}
