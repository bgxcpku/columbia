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
public class StochasticMemoizer {

    public int alphabetSize ;
    public Restaurant contextFreeRestaurant;
    double[] discount = {0.05,0.7,0.8,0.82,0.84,0.88,0.91,0.92,0.93,0.94,0.95} ;
    double discountInfty = 0.95 ;

    public void StochasticMemoizer(int alphabetSize) {
        this.alphabetSize = alphabetSize;
        contextFreeRestaurant = new Restaurant(null,null,discount[0]);
    }

    /*this method traverses the tree from top to bottom given the context and
     * creates a restaurant at the appropriate location and returns the path
     * to get there.
     * */
    public ArrayList<Restaurant> insertContextAndReturnPath(int[] context) {
        ArrayList<Restaurant> restPath = new ArrayList<Restaurant>();

        int fromIndex = context.length-1;
        restPath.add(contextFreeRestaurant);

        if (context.length == 0) {    
            return restPath;
        } else {    
            Restaurant currentRest = contextFreeRestaurant;
            do {
                Restaurant childRest = currentRest.get(new Integer(context[fromIndex]));
                if (childRest != null) {
                    
                    int indexPathsDiffer = compareContexts(context, fromIndex,childRest.parentPath) ;
                    if(indexPathsDiffer == childRest.parentPath.length){
                        fromIndex -= childRest.parentPath.length ;
                        currentRest = childRest ;
                        restPath.add(currentRest) ;
                        continue ;
                    } else {
                        fromIndex -=indexPathsDiffer ;

                        double discountNewRestaurant = 1.0 ;
                        int[] pathToParent = new int[indexPathsDiffer] ;
                        int index = 0 ;

                        for(int j = fromIndex + 1; j<=fromIndex + indexPathsDiffer; ++j){
                            if(context.length-j <= 10){
                                discountNewRestaurant *= discount[context.length-j] ;
                            } else discountNewRestaurant *= discountInfty ;
                            pathToParent[++index] = context[j] ;
                        }

                        childRest.reconfigureRest(childRest.discount/discountNewRestaurant);

                        //need to now instantiate the new, intermediate restaurant
                        //based on the reconfiguredRestaurant ;

/*

                        if(context.length - 1 - fromIndex <= 10){
                            for(int j = context.length - 1 - fromIndex; j<10;++j){

                            }
                        }


                        //probably need to figure out first how many people are at each of the currently
                        //represented tables and then break those to get number of new tables ;
                        for(int j = 0 ; j < childRest.state.length){
                            
                        }





*/










                    }
                } else {
                    int[] pathToParent = new int[fromIndex + 1] ;

                    double discountNewRest = 1.0 ;
                    for(int j = 0; j<=fromIndex; ++j){
                        pathToParent[j] = context[j] ;
                        if(context.length - j >10){
                            discountNewRest *= discountInfty ;
                        } else {
                            discountNewRest *= discount[context.length - j] ;
                        }
                    }

                    Restaurant restToAdd = new Restaurant(currentRest, pathToParent, discountNewRest) ;
                    
                    currentRest.put(new Integer(context[fromIndex]),restToAdd) ;
                    currentRest = restToAdd ;
                    restPath.add(currentRest) ;
                    return restPath ;
                }
            } while (currentRest != null);
        }
        return null;

    }

    /*returns the first place on the path that the two contexts do not agree*/
    private int compareContexts(int[] context, int fromIndex, int[] path) {
        for (int j = 0; j < path.length; ++j) {
            if (context[fromIndex - j] != path[path.length - 1 - j]) {
                return j;
            }
        }
        return path.length;
    }

    private Restaurant instantiateRestaurant(Restaurant childRest){
        return null ;
    }

}
