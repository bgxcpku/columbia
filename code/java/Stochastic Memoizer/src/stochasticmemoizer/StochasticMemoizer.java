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

    public int alphabetSize;
    public Restaurant contextFreeRestaurant;
    public double[] discount = {0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95};
    public double discountInfty = 0.95;
    public double[] predictiveDist;

    public StochasticMemoizer(int alphabetSize) {
        this.alphabetSize = alphabetSize;
        contextFreeRestaurant = new Restaurant(null, null, discount[0]);
        predictiveDist = new double[alphabetSize];
        for (int j = 0; j < alphabetSize; j++) {
            predictiveDist[j] = 1.0 / alphabetSize;
        }
    }

    public double seatSequence(int[] obs) {
        double predictiveLogLoss = 0;
        ArrayList<Restaurant> restPath = new ArrayList<Restaurant>();
        restPath.add(contextFreeRestaurant);
        sitObs(obs[0], restPath);

        int[] context;
        //FIX to pass in context with integer locating current position
        int printIndex = 0 ;
        int ind500 = 1;
        for (int j = 0; j < obs.length - 1; j++) {
            context = new int[j + 1];
            for (int i = 0; i <= j; i++) {
                context[i] = obs[i];
            }

            restPath = this.insertContextAndReturnPath(context);
            this.updatePredictiveDistribution(restPath);
            sitObs(obs[j + 1], restPath);
            predictiveLogLoss += Math.log(this.predictiveDist[obs[j + 1]]) / Math.log(2);
            if(printIndex++ == 500) {
                System.out.println(500*(ind500++)) ;
                printIndex = 0 ;
            }

        }
        return predictiveLogLoss;
    }

    /*this method traverses the tree from top to bottom given the context and
     * creates a restaurant at the appropriate location and returns the path
     * to get there.
     * */
    public ArrayList<Restaurant> insertContextAndReturnPath(int[] context) {
        ArrayList<Restaurant> restPath = new ArrayList<Restaurant>();

        int fromIndex = context.length - 1;
        restPath.add(contextFreeRestaurant);

        if (context.length == 0) {
            return restPath;
        } else {
            Restaurant currentRest = contextFreeRestaurant;
            while (true) {
                Restaurant childRest = currentRest.get(new Integer(context[fromIndex]));
                if (childRest != null) {
                    int indexPathsDiffer = compareContexts(context, fromIndex, childRest.parentPath);
                    if (indexPathsDiffer == childRest.parentPath.length) {
                        fromIndex -= childRest.parentPath.length;
                        currentRest = childRest;
                        restPath.add(currentRest);
                        if (fromIndex == -1) {
                            return restPath;
                        }
                        continue;
                    } else {
                        fromIndex -= indexPathsDiffer;
                        double discountNewRestaurant = 1.0;

                        int[] pathToParent = new int[indexPathsDiffer];
                        int index = 0;

                        //FIX by using arrayCopy and math.pow
                        for (int j = fromIndex + 1; j < fromIndex + indexPathsDiffer + 1; j++) {
                            if (context.length - j <= 10) { //FIX make discount array length
                                discountNewRestaurant *= discount[context.length - j];
                            } else {
                                discountNewRestaurant *= discountInfty;
                            }
                            pathToParent[index++] = context[j];
                        }

                        currentRest = childRest.reconfigureRestaurantReturnIntermediateRestaurant(pathToParent, discountNewRestaurant);
                        restPath.add(currentRest);
                        continue;
                    }
                } else {
                    //if no children in given direction of context, just insert
                    //a restaurant as the child and exit.
                    int[] pathToParent = new int[fromIndex + 1];

                    //FIX discount and math.pow calculation
                    double discountNewRest = 1.0;
                    for (int j = 0; j <= fromIndex; ++j) {
                        pathToParent[j] = context[j];
                        if (context.length - j > 10) {
                            discountNewRest *= discountInfty;
                        } else {
                            discountNewRest *= discount[context.length - j];
                        }
                    }

                    //create new child rest
                    Restaurant restToAdd = new Restaurant(currentRest, pathToParent, discountNewRest);

                    //add rest to list of children rest
                    currentRest.put(new Integer(context[fromIndex]), restToAdd);

                    //add rest to rest path and return
                    restPath.add(restToAdd);
                    return restPath;
                }
            }
        }
    }

    /* returns the first place on the path that the two contexts do not agree, counting
     * from the right to the left*/
    private int compareContexts(int[] context, int fromIndex, int[] path) {
        for (int j = 0; j < path.length; ++j) {
            if (context[fromIndex - j] != path[path.length - 1 - j]) {
                return j;
            }
        }
        return path.length;
    }

    /* method to return a predictive array over the possible outcomes given that
     * the next observation is in the last restaurant of the provided restaurant
     * path. It is assumed that the character list/alphabet is made up of integers
     * running from 0 through the total number -1, thus the predictive distribution
     * is just an array with the characters indexing the probabilitis of observing
     * that character */
    public void updatePredictiveDistribution(ArrayList<Restaurant> restPath) {

        for (int j = 0; j < alphabetSize; j++) {
            predictiveDist[j] = 0;
        }

        //run up the path and update the probabilities on the way
        double cumulativeDiscountUpperLevelRest = 1.0;
        for (int j = 0; j < restPath.size(); j++) {
            Restaurant thisRest = restPath.get(restPath.size() - j - 1);
            if (thisRest.getNumberTablesAtRest() == 0) {
                continue;
            }

            int thisRestWeight = thisRest.getNumberCustAtRest();

            //loop through the types in the rest to add to probability vector
            for (Integer type : thisRest.state.keySet()) {
                predictiveDist[type] += cumulativeDiscountUpperLevelRest * (thisRest.getNumberCustAtType(type) - thisRest.discount * thisRest.state.get(type).length) / thisRestWeight;
            }

            cumulativeDiscountUpperLevelRest *= thisRest.discount * thisRest.getNumberTablesAtRest() / thisRestWeight;
        }

        //add in the probability of drawing from the uniform base measure ;
        for (int j = 0; j < this.alphabetSize; j++) {
            predictiveDist[j] += cumulativeDiscountUpperLevelRest * (1.0 / this.alphabetSize);
        }
    }

    public void sitObs(int obs, ArrayList<Restaurant> restPath) {
        double totalWeight = 0.0;
        double cumulativeDiscountUpperLevelRest = 1.0;

        for (int j = 0; j < restPath.size(); j++) {
            Restaurant thisRest = restPath.get(restPath.size() - j - 1);
            if (thisRest.getNumberCustAtType(obs) == 0) {
                continue;
            }

            int thisRestWeight = thisRest.getNumberCustAtRest();
            totalWeight += cumulativeDiscountUpperLevelRest * (thisRest.getNumberCustAtType(obs) - thisRest.discount * thisRest.state.get(obs).length) / thisRestWeight;

            cumulativeDiscountUpperLevelRest *= thisRest.discount * thisRest.getNumberTablesAtRest() / thisRestWeight;
        }

        //now actually go up the tree until the observation is seated at an existing restaurant
        cumulativeDiscountUpperLevelRest = 1.0;
        double rawRandomSample = Math.random();
        double cumSum = 0.0;
        for (int j = 0; j < restPath.size(); j++) {
            Restaurant thisRest = restPath.get(restPath.size() - j - 1);
            if (thisRest.getNumberCustAtType(obs) == 0) {
                thisRest.addNewTable(obs);
                continue;
            }

            int thisRestWeight = thisRest.getNumberCustAtRest();
            cumSum += cumulativeDiscountUpperLevelRest * (thisRest.getNumberCustAtType(obs) - thisRest.discount * thisRest.state.get(obs).length) / thisRestWeight / totalWeight;

            if (cumSum > rawRandomSample) {
                thisRest.addCustToExistingTable(obs);
                return;
            }

            thisRest.addNewTable(obs);
            cumulativeDiscountUpperLevelRest *= thisRest.discount * thisRest.getNumberTablesAtRest() / thisRestWeight;
        }
    }

    public void printRestAndChildren(Restaurant rest, int indexDownPath, int stringLengthOfWord) {
        int stringLength;

        if (rest.parentPath != null) {
            if (indexDownPath == 0) {
                stringLength = new Integer(rest.parentPath[rest.parentPath.length - 1]).toString().length();
                for (int j = 0; j < stringLengthOfWord - stringLength; j++) {
                    System.out.print(" ");
                }
                System.out.print(rest.parentPath[rest.parentPath.length - 1]);
                indexDownPath++ ;

            } else {
                System.out.print(" - ") ;
                stringLength = new Integer(rest.parentPath[rest.parentPath.length - 1]).toString().length();
                for (int j = 0; j < stringLengthOfWord - stringLength; j++) {
                    System.out.print(" ");
                }
                System.out.print(rest.parentPath[rest.parentPath.length - 1]);
                indexDownPath++ ;
            }
            for (int i = 1; i < rest.parentPath.length; i++) {
                System.out.print(" - ") ;
                stringLength = new Integer(rest.parentPath[rest.parentPath.length - 1 - i]).toString().length();
                for (int j = 0; j < stringLengthOfWord - stringLength; j++) {
                    System.out.print(" ");
                }
                System.out.print(rest.parentPath[rest.parentPath.length - 1 - i]);
                indexDownPath++ ;
            }
        }

        if(rest.keySet().size() == 0) return ;

        int index = 0 ;
        for(Restaurant child : rest.values()){
            if(index++ == 0) printRestAndChildren(child, indexDownPath, stringLengthOfWord) ;
            else {
                System.out.println() ;
                //add in spaces for index down path
                for(int j = 0; j<indexDownPath; j++){
                    for(int i = 0; i<stringLengthOfWord; i++){
                        System.out.print(" ") ;
                    }
                }
                //add in spaces for missed ( - )
                for(int j = 0; j<indexDownPath -1 ;j++){
                    System.out.print("   ") ;
                }
                printRestAndChildren(child, indexDownPath, stringLengthOfWord) ;
            }

        }
        if (rest == this.contextFreeRestaurant) {
            System.out.println();
        }
    }

    public void printTree(){
        printRestAndChildren(this.contextFreeRestaurant, 0, new Integer(this.alphabetSize).toString().length()) ;
    }
}
