/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package finitedepthhpyp;

/**
 *
 * @author nicholasbartlett
 */
public class HPYTree {

    public int alphabetSize;
    public Restaurant contextFreeRestaurant;
    public double[] discount = {0.05, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95};
    public double discountInfty = 0.95;
    public double logLoss = 0.0;
    public int depth;

    public HPYTree(int alphabetSize, int depth) {
        this.alphabetSize = alphabetSize;
        this.depth = depth;
        Restaurant.numberRest = 0;
        contextFreeRestaurant = new Restaurant(0.0, discount[0], null);
    }

    public void seatSeq(int[] seq) {
        int counter = 0;
        int index = 0;
        int[] context;

        for (int typeIndex = 0; typeIndex < depth; typeIndex++) {
            context = new int[typeIndex];
            System.arraycopy(seq, 0, context, 0, typeIndex);
            this.seatObs(contextFreeRestaurant, seq[typeIndex], context, typeIndex - 1, 1.0 / alphabetSize);
            index++;
        }

        context = new int[depth];
        for (int typeIndex = depth; typeIndex < seq.length; typeIndex++) {
            //have a print out to get an idea of how fast this thing is running
            if (counter++ >= 100000) {
                System.out.println("index = " + index);
                counter = 1;
            }
            System.arraycopy(seq, typeIndex - depth, context, 0, depth);
            this.seatObs(contextFreeRestaurant, seq[typeIndex], context, depth - 1, 1.0 / alphabetSize);
            index++;
        }
    }

    public boolean seatObs(Restaurant rest, int type, int[] context, int contextIndex, double probUpper) {
        int[] restCounts = rest.getRestCounts(type);

        double prob;
        double totalWeight = restCounts[2] + rest.concentration;
        if (restCounts[3] > 0) {
            prob = 1.0 * (restCounts[0] - restCounts[1] * rest.discount) / totalWeight +
                    (rest.discount * restCounts[3] + rest.concentration) * probUpper / totalWeight;
        } else {
            prob = probUpper;
        }

        //leafnode first
        if (contextIndex == -1) {
            logLoss += Math.log(prob) / Math.log(2);
            return rest.sitAtRest(type, probUpper);
        }

        Integer childKey = new Integer(context[contextIndex]);
        Restaurant childRest = rest.get(childKey);

        //if no children in the direction of this obs will need to create one
        if (childRest == null) {
            double childDiscount;
            if (context.length - contextIndex <= 10) {
                childDiscount = discount[context.length - contextIndex];
            } else {
                childDiscount = discountInfty;
            }
            childRest = new Restaurant(0, childDiscount, rest);
            rest.put(childKey, childRest);
        }

        contextIndex--;
        if (this.seatObs(childRest, type, context, contextIndex, prob)) {
            return rest.sitAtRest(type, probUpper);
        } else {
            return false;
        }
    }

    public int getNumberNecessaryRest(Restaurant rest){
        int numberProgeny = 0;
        if(rest.size() > 1 || rest.size() == 0){
            numberProgeny++;
        }
        for(Restaurant child : rest.values()){
            numberProgeny += this.getNumberNecessaryRest(child);
        }
        return numberProgeny;
    }

    public int getNumberNecessaryRest(){
        return this.getNumberNecessaryRest(contextFreeRestaurant);
    }
}
