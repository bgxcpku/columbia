/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Tree node object used in the Chinese restaurant representation for HPYP models.
 *
 * @author nicholasbartlett
 *
 */
public class FiniteAlphabetRestaurant extends TreeMap<Integer, FiniteAlphabetRestaurant> {

    private TreeMap<Integer, int[]> tableConfig;
    private FiniteAlphabetRestaurant parent;
    private int customers, tables;
    private Discounts discounts;
    private int edgeStart, edgeLength;

    private static double MIN_SYMBOL_PROB = 5.01 / (double) (Integer.MAX_VALUE);

    /**
     * Count of instantiated restaurants.
     */
    public static int count = 0;


    /**
     * Initializes restaurant with given parameters.
     *
     * @param parent parent restaurant
     * @param edgeStart index of edge start
     * @param edgeLength edge length
     * @param discounts discounts for sequence memoizer
     */
    public FiniteAlphabetRestaurant(FiniteAlphabetRestaurant parent, int edgeStart, int edgeLength, Discounts discounts) {
        this.parent = parent;
        this.edgeStart = edgeStart;
        this.edgeLength = edgeLength;
        this.discounts = discounts;

        tableConfig = new TreeMap<Integer, int[]>();
        //tableConfig = new HashMap<Integer, int[]>();
        customers = 0;
        tables = 0;

        count++;
    }

    /**
     * Sets the edge start index.
     *
     * @param edgeStart new edge start
     */
    public void setEdgeStart(int edgeStart) {
        this.edgeStart = edgeStart;
    }

    /**
     * Gets the edge start index.
     *
     * @return edge start index
     */
    public int edgeStart() {
        return edgeStart;
    }

    /**
     * Gets the edge length.
     *
     * @return edge length
     */
    public int edgeLength() {
        return edgeLength;
    }

    /**
     * Decreases the edge length by a given amount.
     *
     * @param decrementAmount amount to decrease edge length
     */
    public void decrementEdgeLength(int decrementAmount) {
        if (decrementAmount >= edgeLength) {
            throw new IllegalArgumentException("resulting edge length must be positive");
        }

        edgeLength -= decrementAmount;
    }

    /**
     * Allows the table configurations to be set for each type seperately.  A table
     * configuation is an int[] which is equal in length to the number of tables
     * of the given type.  Each entry of the int[] specifies the number of customers
     * sitting at a unique table. This is required for the fragmentation step when
     * creating the sequence memozier tree.
     *
     * @param type integer value of type
     * @param config actual table configuration to be used
     */
    public void setTableConfig(int type, int[] config) {
        int[] currentConfig;

        currentConfig = tableConfig.get(type);

        //update customers and tables if will be deleting a row by replacement
        if (currentConfig != null) {
            for (int cust : currentConfig) {
                customers -= cust;
            }
            tables -= currentConfig.length;
        }

        //update customers and tables
        for (int cust : config) {
            customers += cust;
        }
        tables += config.length;

        tableConfig.put(type, config);
    }

    /**
     * Seats a customer of the given type in the restaurant.  Also, if a new table
     * is created a new customer of the same type is seated in the parent restaurant.
     *
     * @param type type to be seated
     * @return the predictive log likelihood of the type prior to insertion in the model
     */
    public double seat(int type) {
        double ll, pp, p, r, tw, cuSum, discount;
        int[] tsa, ntsa;
        int tc;

        discount = discount();
        pp = parent.predictiveProbability(type);

        tc = 0;
        tsa = tableConfig.get(type);
        
        if (tsa == null) {
            tableConfig.put(type, new int[]{1});

            if (tables == 0) {
                p = pp;
            } else {
                p = (double) tables * discount * pp / (double) customers;
            }

            tables++;
            parent.seat(type);

            ll = Math.log(p);
        } else {
            for (int cust : tsa) {
                tc += cust;
            }

            r = FiniteAlphabetSequenceMemoizer.RNG.nextDouble();
            tw = (double) tc - (double) tsa.length * discount + (double) tables * discount * pp;

            cuSum = 0.0;
            for (int table = 0; table < tsa.length; table++) {
                cuSum += ((double) tsa[table] - discount) / tw;
                if (cuSum > r) {
                    tsa[table]++;
                    break;
                }
            }

            if (cuSum <= r) {
                ntsa = new int[tsa.length + 1];
                System.arraycopy(tsa, 0, ntsa, 0, tsa.length);
                ntsa[tsa.length] = 1;
                tableConfig.put(type, ntsa);
                tables++;
                parent.seat(type);
            }

            p = tw / (double) customers;
            ll = Math.log(p);
        }

        customers++;

        return ll;
    }

    /**
     * Method to insert types in one pass down the tree.  This method creates necessary
     * nodes and then does the insertion in a single pass down the tree.
     *
     * @param p probability of type in parent node
     * @param type type to be inserted
     * @param d current depth
     * @param depth max depth of tree
     * @param context context array
     * @param index index of current place in context
     * @param returnP container for predictive probability of type prior to insertion
     * @param discountMultFactor container used for calculating the discount hessian
     * @return indicator that a customer must be seated in the parent restaurant
     */
    public boolean seat(double p, int type, int d, int depth, int[] context, int index, MutableDouble returnP, MutableDouble discountMultFactor){
        double discount, multFactor, pp;
        int[] tsa;
        int tc, tt;
        
        discount = discount(d);
        pp = p;
        
        if(customers > 0){
            multFactor = (double) tables * discount / (double) customers;
            p *= multFactor;
        }

        tsa = tableConfig.get(type);
        tc = 0;
        tt = 0;
        
        if(tsa != null){
            for(int c : tsa){
                tc += c;
            }
            tt = tsa.length;

            p += ((double) tc - (double) tt * discount) / (double) customers;
        }

        /*************do data structure stuff, this is recursive piece ********/
        FiniteAlphabetRestaurant child, newChild;
        int overlap, el, es;
        boolean leafNode, seat;

        seat = false;

        if (depth == -1) {
            leafNode = index == -1;
        } else {
            leafNode = index == -1 || d == depth;
        }

        if (leafNode) {
            seat = true;
            returnP.set(p);
        } else {

            child = get(context[index]);

            if (child == null) {
                if (depth == -1) {
                    el = index + 1;
                    child = new FiniteAlphabetRestaurant(this, 0, index + 1, discounts);
                } else {
                    el = (depth - d < index + 1) ? depth - d : index + 1;
                    child = new FiniteAlphabetRestaurant(this, index - el + 1, el, discounts);
                }
                put(context[index], child);
                seat = child.seat(p, type, d + el, depth, context, index - el, returnP, discountMultFactor);
            } else {

                es = child.edgeStart();
                el = child.edgeLength();
                overlap = 0;
                while (overlap < el && context[es + el - 1 - overlap] == context[index - overlap]) {
                    overlap++;
                }

                assert overlap > 0;

                if (overlap == el) {

                    seat = child.seat(p, type, d + el, depth, context, index - el, returnP, discountMultFactor);

                } else {

                    newChild = child.fragment(this, index - overlap + 1, overlap, false);
                    put(context[index], newChild);
                    newChild.put(context[es + el - overlap - 1], child);

                    seat = newChild.seat(p, type, d + overlap, depth, context, index - overlap, returnP, discountMultFactor);

                }
            }
        }

        /*********************now actually seat if needed**********************/
        boolean seatInParent;

        seatInParent = false;
        
        discounts.updateGradient(d - edgeLength, d, tt, customers, tables, pp, discount, discountMultFactor.doubleVal());
        if(customers > 0){
            discountMultFactor.times((double) tables * discount / (double) customers);
        }

        if (seat) {
            double r, tw, cuSum;
            int[] ntsa;

            if (tsa == null) {
                tableConfig.put(type, new int[]{1});
                tables++;
                seatInParent = true;
            } else {

                r = FiniteAlphabetSequenceMemoizer.RNG.nextDouble();
                tw = (double) tc - (double) tt * discount + (double) tables * discount * pp;

                cuSum = 0.0;
                for (int table = 0; table < tt; table++) {
                    cuSum += ((double) tsa[table] - discount) / tw;
                    if (cuSum > r) {
                        tsa[table]++;
                        break;
                    }
                }

                if (cuSum <= r) {
                    ntsa = new int[tt + 1];
                    System.arraycopy(tsa, 0, ntsa, 0, tt);
                    ntsa[tt] = 1;
                    tableConfig.put(type, ntsa);
                    tables++;
                    seatInParent = true;
                }
            }

            customers++;
        }

        return seatInParent;
    }

    /**
     * Like recursive seat, only calculates the full predictive CDF prior to insertion of the type.
     *
     * @param pArray predictive CDF in parent node
     * @param type type to be inserted
     * @param d depth of current node
     * @param depth max depth of tree
     * @param context context array
     * @param index index of current place in context
     * @param discountMultFactor container used for calculating the discount hessian
     * @return indicator that a customer must be seated in the parent restaurant
     */

    public boolean seatCDF(double[] pArray, int type, int d, int depth, int[] context, int index, MutableDouble discountMultFactor) {
        /***********update ppArray to reflect counts in this restaurant************/
        double pp, discount, multFactor;
        int[] tsa;
        int tc;

        pp = pArray[type];
        discount = discount(d);

        if (customers > 0) {
            multFactor = (double) tables * discount / (double) customers;
            for (int i = 0; i < pArray.length; i++) {
                pArray[i] *= multFactor;
            }
        }

        for (Integer t : tableConfig.keySet()) {
            tsa = tableConfig.get(t);

            tc = 0;
            for (int c : tsa) {
                tc += c;
            }

            pArray[t] += ((double) tc - (double) tsa.length * discount) / (double) customers;
        }

        /*************do data structure stuff, this is recursive piece ********/
        FiniteAlphabetRestaurant child, newChild;
        int overlap, el, es;
        boolean leafNode, seat;

        seat = false;

        if (depth == -1) {
            leafNode = index == -1;
        } else {
            leafNode = index == -1 || d == depth;
        }

        if (leafNode) {
            seat = true;
        } else {

            child = get(context[index]);

            if (child == null) {
                if (depth == -1) {
                    el = index + 1;
                    child = new FiniteAlphabetRestaurant(this, 0, index + 1, discounts);
                } else {
                    el = (depth - d < index + 1) ? depth - d : index + 1;
                    child = new FiniteAlphabetRestaurant(this, index - el + 1, el, discounts);
                }
                put(context[index], child);
                seat = child.seatCDF(pArray, type, d + el, depth, context, index - el, discountMultFactor);
            } else {

                es = child.edgeStart();
                el = child.edgeLength();
                overlap = 0;
                while (overlap < el && context[es + el - 1 - overlap] == context[index - overlap]) {
                    overlap++;
                }

                assert overlap > 0;

                if (overlap == el) {

                    seat = child.seatCDF(pArray, type, d + el, depth, context, index - el, discountMultFactor);

                } else {

                    newChild = child.fragment(this, index - overlap + 1, overlap, false);
                    put(context[index], newChild);
                    newChild.put(context[es + el - overlap - 1], child);

                    seat = newChild.seatCDF(pArray, type, d + overlap, depth, context, index - overlap, discountMultFactor);

                }
            }
        }

        /*********************now actually seat if needed**********************/
        boolean seatInParent;
        int tt;
        
        seatInParent = false;
        tc = 0;
        tt = 0;
        tsa = tableConfig.get(type);

        if(tsa != null){
            for (int c : tsa) {
                tc += c;
            }
            tt = tsa.length;
        }

        discounts.updateGradient(d - edgeLength, d, tt, customers, tables, pp, discount, discountMultFactor.doubleVal());
        if(customers > 0){
            discountMultFactor.times((double) tables * discount / (double) customers);
        }

        if (seat) {
            double r, tw, cuSum;
            int[] ntsa;

            if (tsa == null) {
                tableConfig.put(type, new int[]{1});
                tables++;
                seatInParent = true;
            } else {

                r = FiniteAlphabetSequenceMemoizer.RNG.nextDouble();
                tw = (double) tc - (double) tt * discount + (double) tables * discount * pp;

                cuSum = 0.0;
                for (int table = 0; table < tt; table++) {
                    cuSum += ((double) tsa[table] - discount) / tw;
                    if (cuSum > r) {
                        tsa[table]++;
                        break;
                    }
                }

                if (cuSum <= r) {
                    ntsa = new int[tt + 1];
                    System.arraycopy(tsa, 0, ntsa, 0, tt);
                    ntsa[tt] = 1;
                    tableConfig.put(type, ntsa);
                    tables++;
                    seatInParent = true;
                }
            }

            customers++;
        }
        
        return seatInParent;
    }

    /**
     * Like recursive seatCDF, but now only way to identify the type to seat is
     * a point on the predictive CDF.  The predictive CDF is calculated prior to insertion,
     * the correct type is identified, and the type is then inserted into the model.
     *
     * @param pointOnCdf point on CDF
     * @param pArray predictive CDF in parent node
     * @param type container object for type to be seated
     * @param d depth of current node
     * @param depth max depth of tree
     * @param context context array
     * @param index index of current place in context
     * @param discountMultFactor container used for calculating the discount hessian
     * @return indicator that a customer must be seated in the parent restaurant
     *
     */
    public boolean seatPointOnCDF(double pointOnCdf, double[] pArray, MutableInteger type, int d, int depth, int[] context, int index, MutableDouble discountMultFactor) {
        /***********update ppArray to reflect counts in this restaurant************/
        double discount, multFactor;
        int[] tsa;
        int tc;
        double[] pp;

        tc = 0;
        discount = discount(d);
        pp = new double[pArray.length];
        System.arraycopy(pArray, 0, pp, 0, pArray.length);

        if (customers > 0) {
            multFactor = (double) tables * discount / (double) customers;
            for (int i = 0; i < pArray.length; i++) {
                pArray[i] *= multFactor;
            }
        }

        for (Integer t : tableConfig.keySet()) {
            tsa = tableConfig.get(t);

            tc = 0;
            for (int c : tsa) {
                tc += c;
            }

            pArray[t] += ((double) tc - (double) tsa.length * discount) / (double) customers;
        }

        /*************do data structure stuff, this is recursive piece ********/
        FiniteAlphabetRestaurant child, newChild;
        int overlap, el, es;
        boolean leafNode, seat;
        double cuSum, eofAdjustment;

        seat = false;

        if (depth == -1) {
            leafNode = index == -1;
        } else {
            leafNode = index == -1 || d == depth;
        }

        if (leafNode) {
            seat = true;

            eofAdjustment = 1.0 + MIN_SYMBOL_PROB * (double) pArray.length ;
            cuSum = 0.0;
            for(int t = 0; t < pArray.length; t++){

                cuSum += (pArray[t] + MIN_SYMBOL_PROB) / eofAdjustment;
                if(cuSum > pointOnCdf){
                    type.set(t);
                    break;
                }
            }
        } else {

            child = get(context[index]);

            if (child == null) {
                if (depth == -1) {
                    el = index + 1;
                    child = new FiniteAlphabetRestaurant(this, 0, index + 1, discounts);
                } else {
                    el = (depth - d < index + 1) ? depth - d : index + 1;
                    child = new FiniteAlphabetRestaurant(this, index - el + 1, el, discounts);
                }
                put(context[index], child);
                seat = child.seatPointOnCDF(pointOnCdf, pArray, type, d + el, depth, context, index - el, discountMultFactor);
            } else {

                es = child.edgeStart();
                el = child.edgeLength();
                overlap = 0;
                while (overlap < el && context[es + el - 1 - overlap] == context[index - overlap]) {
                    overlap++;
                }

                assert overlap > 0;

                if (overlap == el) {

                    seat = child.seatPointOnCDF(pointOnCdf, pArray, type, d + el, depth, context, index - el, discountMultFactor);

                } else {

                    newChild = child.fragment(this, index - overlap + 1, overlap, false);
                    put(context[index], newChild);
                    newChild.put(context[es + el - overlap - 1], child);

                    seat = newChild.seatPointOnCDF(pointOnCdf, pArray, type, d + overlap, depth, context, index - overlap, discountMultFactor);
                }
            }
        }

        /*********************now actually seat if needed**********************/
        boolean seatInParent;
        int tt;

        seatInParent = false;
        tc = 0;
        tt = 0;
        tsa = tableConfig.get(type.intVal());

        if(tsa != null){
            for (int c : tsa) {
                tc += c;
            }
            tt = tsa.length;
        }

        discounts.updateGradient(d - edgeLength, d, tt, customers, tables, pp[type.intVal()], discount, discountMultFactor.doubleVal());
        if(customers > 0){
            discountMultFactor.times((double) tables * discount / (double) customers);
        }

        if (seat) {
            double r, tw;
            int[] ntsa;

            seatInParent = false;
            tsa = tableConfig.get(type.intVal());

            if (tsa == null) {
                
                tableConfig.put(type.intVal(), new int[]{1});
                tables++;
                seatInParent = true;
                
            } else {
                
                r = FiniteAlphabetSequenceMemoizer.RNG.nextDouble();
                tw = (double) tc - (double) tt * discount + (double) tables * discount * pp[type.intVal()];

                cuSum = 0.0;
                for (int table = 0; table < tt; table++) {
                    cuSum += ((double) tsa[table] - discount) / tw;
                    if (cuSum > r) {
                        tsa[table]++;
                        break;
                    }
                }

                if (cuSum <= r) {
                    ntsa = new int[tt + 1];
                    System.arraycopy(tsa, 0, ntsa, 0, tt);
                    ntsa[tt] = 1;
                    tableConfig.put(type.intVal(), ntsa);
                    tables++;
                    seatInParent = true;
                }
            }

            customers++;
        }

        return seatInParent;
    }

    /**
     * Fragments a restaurant and returns the intermediate restaurant.  Each
     * restaurant is internally coherent, but the graph needs to be updated
     * externally to reflect that a restaurant has been inserted in the middle
     * of an edge.
     *
     * @param irParent parent of new restaurant
     * @param irEdgeStart edge start of new restaurant
     * @param irEdgeLength edge length of new restaurant
     * @param forPrediction indicator that fragmentation is for prediction and thus
     * will not ultimately be inserted in the tree
     * @return restaurant either for insertion in the tree or for prediction
     */
    public FiniteAlphabetRestaurant fragment(FiniteAlphabetRestaurant irParent, int irEdgeStart, int irEdgeLength, boolean forPrediction) {
        double discount, irDiscount, fragDiscount, fragConcentration, r, cuSum, totalWeight;
        FiniteAlphabetRestaurant intermediateRestaurant;
        int[] tsa, irtsa, newtsa;
        int table;
        ArrayList<MutableInteger> fragmentedTable;
        ArrayList<MutableInteger> allTables;

        intermediateRestaurant = new FiniteAlphabetRestaurant(irParent, irEdgeStart, irEdgeLength, discounts);
        irDiscount = intermediateRestaurant.discount();
        discount = discount();
        fragDiscount = discount / irDiscount;
        fragConcentration = -1 * discount;

        for (Integer type : tableConfig.keySet()) {
            allTables = new ArrayList<MutableInteger>();

            tsa = tableConfig.get(type);
            if(!forPrediction){
                tables -= tsa.length;
            }

            irtsa = new int[tsa.length];
            table = 0;
            for (int tableSize : tsa) {

                fragmentedTable = new ArrayList<MutableInteger>();
                fragmentedTable.add(new MutableInteger(1));
                totalWeight = fragConcentration;

                topFor:
                for (int customer = 1; customer < tableSize; customer++) {
                    totalWeight++;
                    r = FiniteAlphabetSequenceMemoizer.RNG.nextDouble();
                    cuSum = 0.0;

                    for (MutableInteger t : fragmentedTable) {
                        cuSum += ((double) t.intVal() - fragDiscount) / totalWeight;
                        if (cuSum > r) {
                            t.increment();
                            continue topFor;
                        }
                    }
                    fragmentedTable.add(new MutableInteger(1));
                }
                irtsa[table++] = fragmentedTable.size();
                allTables.addAll(fragmentedTable);
            }
            intermediateRestaurant.setTableConfig(type, irtsa);
            newtsa = new int[allTables.size()];
            table = 0;
            for (MutableInteger tableSize : allTables) {
                newtsa[table++] = tableSize.intVal();
            }

            if(!forPrediction){
                tableConfig.put(type, newtsa);
                tables += newtsa.length;
            }
        }

        if(!forPrediction){
            parent = intermediateRestaurant;
            decrementEdgeLength(irEdgeLength);
        } else {
            count--;
        }

        return intermediateRestaurant;
    }

    /**
     * Gets the discount for the restaurant from the discounts object.
     *
     * @return double discount value
     */
    public double discount() {
        int depth;

        depth = depth();

        return discounts.get(depth - edgeLength, depth);
    }

    /**
     * Gets the discount for the restaurant from the discounts object.
     *
     * @param depth depth of restaurant
     * @return double discount value
     */
    public double discount(int depth){
        return discounts.get(depth - edgeLength, depth);
    }

    /**
     * Gets the predictive probability of a given type in this restaurant /
     * conditional distribution.
     *
     * @param type type for requested predictive probability
     * @return predictive probability of type
     */
    public double predictiveProbability(int type) {
        double p, discount;
        int tc, tt;
        int[] tsa;

        if (customers > 0) {
            discount = discount();
            tc = 0;
            tt = 0;
            tsa = tableConfig.get(type);
            if (tsa != null) {
                for (int cust : tsa) {
                    tc += cust;
                }
                tt = tsa.length;
            }

            p = ((double) tc - (double) tt * discount + (double) tables * discount * parent.predictiveProbability(type)) / (double) customers;
        } else {
            p = parent.predictiveProbability(type);
        }

        return p;
    }

    /**
     * Gets the predictive PDF at this node.  Predictive probabilities are for
     * types [0,alphabetSize), in order.
     *
     * @return double[] of predictive probabilities
     */
    public double[] predictivePDF() {
        double[] pp;
        int[] tsa;
        double multFactor, discount;
        int tc;

        pp = parent.predictivePDF();
        if (customers > 0) {

            discount = discount();
            multFactor = discount * (double) tables / (double) customers;

            for (int t = 0; t < pp.length; t++) {
                pp[t] *= multFactor;
            }

            for (Integer type : tableConfig.keySet()) {
                tsa = tableConfig.get(type);

                tc = 0;
                for (int c : tsa) {
                    tc += c;
                }

                pp[type] += ((double) tc - (double) tsa.length * discount) / (double) customers;
            }
        }

        return pp;
    }

    /**
     * Unseats a given type from the restaurant.  This method is used in sampling.
     *
     * @param type type to useat
     */
    public void unseat(int type) {
        double r, cuSum;
        int[] tsa, ntsa;
        int tc;

        tc = 0;
        tsa = tableConfig.get(type);
        for (int cust : tsa) {
            tc += cust;
        }

        if (tsa.length == 1 && tsa[0] == 1) {
            tableConfig.remove(type);
            tables--;
            parent.unseat(type);
        } else {
            r = FiniteAlphabetSequenceMemoizer.RNG.nextDouble();
            cuSum = 0.0;
            for (int table = 0; table < tsa.length; table++) {
                cuSum += (double) tsa[table] / (double) tc;
                if (cuSum > r) {
                    tsa[table]--;
                    if (tsa[table] == 0) {
                        ntsa = new int[tsa.length - 1];
                        System.arraycopy(tsa, 0, ntsa, 0, table);
                        System.arraycopy(tsa, table + 1, ntsa, table, tsa.length - table - 1);
                        tableConfig.put(type, ntsa);
                        tables--;
                        parent.unseat(type);
                    }
                    break;
                }
            }
            assert cuSum > r;
        }
        customers--;
    }

    /**
     * Gets the depth of the restaurant.  The depth is equal to the context
     * length and is zero at the root restaurant.
     *
     * @return depth of restaurant
     */
    public int depth() {
        return parent.depth() + edgeLength;
    }

    /**
     * Prints the state of the restaurant.  Each line starts with the type folowed
     * by a colon and then a vector showing the size of each table associated with
     * the printed type.  The length of the vector is the number of tables associated
     * with the given type.
     */
    public void printState() {
        int[] tsa;
        for (Integer type : tableConfig.keySet()) {
            tsa = tableConfig.get(type);
            System.out.print(type + "  :  [" + tsa[0]);
            for (int i = 1; i < tsa.length; i++) {
                System.out.print(", " + tsa[i]);
            }
            System.out.println("]");
        }
    }

    /**
     * Gets the log likelihood of the seating arrangement in the restaurant.
     *
     * @return log likelihood of seating arrangement;
     */
    public double logLik() {
        int c, t;
        double logLik, discount;

        c = 0;
        t = 0;
        logLik = 0.0;
        discount = discount();

        for (int[] tsa : tableConfig.values()) {
            for (int custs : tsa) {
                logLik += logLikTable(custs, c, t, discount);
                t++;
                c += custs;
            }
        }

        return logLik;
    }

    private double logLikTable(int tableSize, int existingCust, int existingTables, double discount) {
        double logLik, p;

        logLik = 0.0;
        
        if(existingCust > 0){
            p = ((double) existingTables * discount) / (double) existingCust;
            logLik += Math.log(p);
        }

        existingCust++;

        for (int i = 1; i < tableSize; i++) {
            p = ((double) i - discount) / (double) existingCust;
            logLik += Math.log(p);
            existingCust++;
        }

        return logLik;
    }

    private void sampleCustomer(int type, int table, double discount) {
        double r, cuSum, totalWeight, pp;
        int tt, tc, zeroInd;
        int[] tsa, ntsa;

        tsa = tableConfig.get(type);

        tsa[table]--; assert tsa[table] >= 0;

        if (tsa[table] == 0) {
            parent.unseat(type);
            tables--;
        }

        tt = 0;
        tc = 0;

        zeroInd = -1;
        for (int i = 0; i < tsa.length; i++) {
            tc += tsa[i];
            if (tsa[i] > 0) {
                tt++;
            } else {
                zeroInd = i;
            }
        }

        r = FiniteAlphabetSequenceMemoizer.RNG.nextDouble();
        cuSum = 0.0;
        pp = parent.predictiveProbability(type);
        totalWeight = (double) tc - (double) tt * discount + (double) tables * discount * pp;

        for (int i = 0; i < tsa.length; i++) {
            if (tsa[i] > 0) {
                cuSum += ((double) tsa[i] - discount) / totalWeight;
            }
            if (cuSum > r) {
                tsa[i]++;
                break;
            }
        }

        if (cuSum <= r) {
            tables++;
            if (zeroInd > -1) {
                tsa[zeroInd]++;
            } else {
                ntsa = new int[tsa.length + 1];
                System.arraycopy(tsa, 0, ntsa, 0, tsa.length);
                ntsa[tsa.length] = 1;
                tableConfig.put(type, ntsa);
            }
            parent.seat(type);
        }
    }

    /**
     * Samples seating arrangment for entire restaurant.
     */
    public void sampleSeatingArrangements(){
        int b;
        int[] typeTable;
        RandomCustomer randomCustomer;
        double discount;

        if(customers > 1){
            typeTable = new int[2];
            randomCustomer = new RandomCustomer();
            discount = discount();

            while ((b = randomCustomer.nextCustomer(typeTable)) > -1) {
                sampleCustomer(typeTable[0], typeTable[1], discount);
            }

            fixZeros();
        }
    }

    private void fixZeros(){
        int[] tsa,ntsa;
        int numZeros,ind;

        for(Integer type : tableConfig.keySet()){
            tsa = tableConfig.get(type);

            numZeros = 0;
            for(int tableSize : tsa){
                assert tableSize >= 0 ;
                if(tableSize == 0){
                    numZeros++;
                }
            }

            if(numZeros > 0){
                ntsa = new int[tsa.length - numZeros];
                ind = 0;
                for(int tableSize:tsa){
                   if(tableSize > 0){
                       ntsa[ind++] = tableSize;
                   }
                }
                tableConfig.put(type,ntsa);
            }
        }
    }

    private class RandomCustomer{
        
        private int[] randomType;
        private int[] randomTable;
        private int index;
        private int customersToSample;

        /**
         * Class to generate and then iterate over a random customer ordering
         * within the restaurant.  Object acts essentially like an iterator object.
         */
        public RandomCustomer() {
            int ind;
            int[] tsa, randomOrder;

            index = 0;
            randomType = new int[customers];
            randomTable = new int[customers];

            customersToSample = customers;
            for (int[] ts : tableConfig.values()) {
                if (ts.length == 1 && ts[0] == 1) {
                    customersToSample--;
                }
            }
            
            randomOrder = sampleWOReplacement(customersToSample);

            ind = 0;
            for (Integer type : tableConfig.keySet()) {
                tsa = tableConfig.get(type);
                if (!(tsa.length == 1 && tsa[0] == 1)) {
                    for (int table = 0; table < tsa.length; table++) {
                        for (int customer = 0; customer < tsa[table]; customer++) {
                            randomType[randomOrder[ind]] = type;
                            randomTable[randomOrder[ind++]] = table;
                        }
                    }
                }
            }
        }

        /**
         * Gets the next customer to be sampled.
         * @param typeAndTable array to place return type and table
         * @return 0 if there are return values, -1 if not
         */
        public int nextCustomer(int[] typeAndTable){
            if(index == customersToSample){
                return -1;
            } else{
                typeAndTable[0] = randomType[index];
                typeAndTable[1] = randomTable[index++];
                return 0;
            }
        }

        /**
         * Helper method to create a random ordering of n objects.
         *
         * @param n number of objects
         * @return int[] of numbers 0 through n-1 in random order
         */
        private int[] sampleWOReplacement(int n){
            HashSet<Integer> set;
            int[] randomOrder;
            int s;
            double rand,cuSum;

            set = new HashSet<Integer>(n);

            for(int i = 0; i<n;i++){
                set.add(i);
            }

            randomOrder = new int[n];
            s = set.size();
            while(s > 0){
                rand = FiniteAlphabetSequenceMemoizer.RNG.nextDouble();
                cuSum = 0.0;
                for(Integer i:set){
                    cuSum += 1.0 / (double) s;
                    if(cuSum > rand){
                        randomOrder[n-s] = i;
                        set.remove(i);
                        break;
                    }
                }
                s--;
                assert s == set.size();
                s = set.size();
            }
            
            return randomOrder ;
        }
    }
}
