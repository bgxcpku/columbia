/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author nicholasbartlett
 */
public class OnlineRestaurant extends TreeMap<Integer, OnlineRestaurant>{

    private TreeMap<Integer, int[]> tableConfig;
    private OnlineRestaurant parent;
    private int customers, tables;
    private Discounts discounts;
    private int[] edge;

    private static double MIN_SYMBOL_PROB = 5.01 / (double) (Integer.MAX_VALUE);

    /**
     * Count of instantiated restaurants.
     */
    public static int count = 0;

    public OnlineRestaurant(OnlineRestaurant parent, int[] edge, Discounts discounts) {
        this.parent = parent;
        this.edge = edge;
        this.discounts = discounts;

        tableConfig = new TreeMap<Integer, int[]>();
        customers = 0;
        tables = 0;

        count++;
    }

    public int[] edge(){
        return edge;
    }

    public void decrementEdgeLength(int decrementAmount) {
        if (decrementAmount >= edge.length) {
            throw new IllegalArgumentException("resulting edge length must be positive");
        }

        int[] newEdge;

        newEdge = new int[edge.length - decrementAmount];
        System.arraycopy(edge, 0, newEdge, 0, newEdge.length);
        edge = newEdge;
    }

    public void setTableConfig(int type, int[] config) {
        int[] currentConfig;

        currentConfig = tableConfig.get(type);

        if (currentConfig != null) {
            for (int cust : currentConfig) {
                customers -= cust;
            }
            tables -= currentConfig.length;
        }

        for (int cust : config) {
            customers += cust;
        }
        tables += config.length;

        tableConfig.put(type, config);
    }

    public void insertInTree(){
        parent.put(edge[edge.length-1], this);
    }

    public boolean seat(double p, int type, int d, int depth, int[] context, int index, MutableDouble returnP, MutableDouble discountMultFactor){
        double discount, multFactor, pp;
        int[] tsa;
        int tt, tc;

        discount = discount(d);
        pp = p;

        tc = 0;
        tt = 0;
        tsa = tableConfig.get(type);
        if(tsa != null){
            tc = tsa[0];
            tt = tsa[1];
        }

        if(customers > 0){
            multFactor = (double) tables * discount / (double) customers;
            p *= multFactor;
            p += ((double) tc - (double) tt * discount) / (double) customers;

        }

        /*************do data structure stuff, this is recursive piece ********/
        OnlineRestaurant child, newChild;
        int overlap, el;
        boolean leafNode, seat;
        int[] childEdge;

        seat = false;
        leafNode = index == -1 || d == depth;

        if (leafNode) {
            seat = true;
            returnP.set(p);
        } else {

            child = get(context[index]);

            if (child == null) {

                el = (depth - d < index + 1) ? depth - d : index + 1;

                childEdge = new int[el];
                System.arraycopy(context, index - el + 1, childEdge, 0, el);

                child = new OnlineRestaurant(this, childEdge, discounts);
                
                put(context[index], child);
                seat = child.seat(p, type, d + el, depth, context, index - el, returnP, discountMultFactor);
                
            } else {

                childEdge = child.edge();
                overlap = 0;
                el = childEdge.length;
                while (overlap < el && childEdge[el - 1 - overlap] == context[index - overlap]) {
                    overlap++;
                }

                assert overlap > 0;

                if (overlap == el) {

                    seat = child.seat(p, type, d + el, depth, context, index - el, returnP, discountMultFactor);

                } else {

                    childEdge = new int[overlap];
                    System.arraycopy(context, index - overlap + 1, childEdge, 0, overlap);

                    newChild = child.fragment(this, childEdge, false, discount);
                    put(context[index], newChild);
                    child.insertInTree();

                    seat = newChild.seat(p, type, d + overlap, depth, context, index - overlap, returnP, discountMultFactor);
                    
                }
            }
        }

        /*********************now actually seat if needed**********************/
        boolean seatInParent;
        seatInParent = false;
        double num;

        discounts.updateGradient(d - edge.length, d, tt, customers, tables, pp, discount, discountMultFactor.doubleVal());
        if(customers > 0){
            discountMultFactor.times((double) tables * discount / (double) customers);
        }
        
        if (seat) {
            double r, tw;

            if (tsa == null) {
                tableConfig.put(type, new int[]{1, 1});
                customers++;
                tables++;
                seatInParent = true;
            } else {
                discount = discount();
                num = (double) tc - (double) tt * discount;
                tw =  num + (double) tables * discount * parent.predictiveProbability(type);
                r = OnlineSequenceMemoizer.RNG.nextDouble();
                if (num / tw > r) {
                    tsa[0]++;
                    customers++;
                } else {
                    tsa[0]++;
                    tsa[1]++;
                    customers++;
                    tables++;
                    seatInParent = true;
                }
            }
        }

        return seatInParent;
    }

    public double discount() {
        return discount(depth());
    }

    public double discount(int depth){
        return discounts.get(depth - edge.length, depth);
    }

    public double predictiveProbability(int type) {
        double p, discount;
        int[] tsa;

        if (customers > 0) {
            discount = discount();
            tsa = tableConfig.get(type);
            if(tsa != null){
                p = ((double) tsa[0] - (double) tsa[1] * discount + (double) tables * discount * parent.predictiveProbability(type)) / (double) customers;
            } else {
                p = (double) tables * discount * parent.predictiveProbability(type) / (double) customers;
            }
        } else {
            p = parent.predictiveProbability(type);
        }

        return p;
    }

    public double[] predictiveProbability() {
        double[] pp;
        int[] tsa;
        double multFactor, discount;

        pp = parent.predictiveProbability();
        if (customers > 0) {

            discount = discount();
            multFactor = discount * (double) tables / (double) customers;

            for (int t = 0; t < pp.length; t++) {
                pp[t] *= multFactor;
            }

            for (Integer type : tableConfig.keySet()) {
                tsa = tableConfig.get(type);
                pp[type] += ((double) tsa[0] - (double) tsa[1] * discount) / (double) customers;
            }
        }

        return pp;
    }

    public int depth() {
        return parent.depth() + edge.length;
    }

    public OnlineRestaurant fragment(OnlineRestaurant irParent, int[] irEdge, boolean forPrediction, double discount) {
        double irDiscount, fragDiscount, fragConcentration, r, cuSum, totalWeight;
        OnlineRestaurant intermediateRestaurant;
        int[] tsa, irtsa, newtsa;
        int table;
        ArrayList<MutableInteger> fragmentedTable;
        ArrayList<MutableInteger> allTables;

        instantiateTables(discount);

        intermediateRestaurant = new OnlineRestaurant(irParent, irEdge, discounts);
        irDiscount = intermediateRestaurant.discount();
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
                    r = OnlineSequenceMemoizer.RNG.nextDouble();
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

        compressTables();
        intermediateRestaurant.compressTables();

        if(!forPrediction){
            parent = intermediateRestaurant;
            decrementEdgeLength(irEdge.length);
        } else {
            count--;
        }

        return intermediateRestaurant;
    }

    public void instantiateTables(double discount){
        int[] tsa, ntsa;

        logSterlingNumberMemoizer = new HashMap<IntegerPair, Double>(200);

        for(Integer type : tableConfig.keySet()){
            tsa = tableConfig.get(type);
            ntsa = instantiateTSA(discount, tsa[0], tsa[1]);
            tableConfig.put(type, ntsa);
        }

        logSterlingNumberMemoizer = null;
    }

    private void compressTables(){
        int[] tsa;
        int tc, tt;

        for(Integer type : tableConfig.keySet()){
            tsa = tableConfig.get(type);

            tc = 0;
            for(int c : tsa){
                tc += c;
            }
            tt = tsa.length;

            tableConfig.put(type, new int[]{tc,tt});
        }
    }

    private HashMap<IntegerPair, Double> logSterlingNumberMemoizer;// = new HashMap<IntegerPair, Double>(200);;
    private double logSterlingNumber(double d, int c, int t){
        double logSterlingNumber;
        Double lsn;
        IntegerPair key;

        if(c < 0 || t < 0 || d <= 0.0 || d >= 1.0){
            throw new IllegalArgumentException();
        }

        if(c == t) {
            return 0.0;
        }

        if(c == 0 || t== 0 || t > c){
            return Double.NEGATIVE_INFINITY;
        }

        key = new IntegerPair(c, t);

        lsn = logSterlingNumberMemoizer.get(key);

        if(lsn == null){
            logSterlingNumber = logAdd(logSterlingNumber(d, c-1, t-1), Math.log((double) c - 1.0 - d * (double) t) +  logSterlingNumber(d, c-1, t));
            logSterlingNumberMemoizer.put(new IntegerPair(c,t), logSterlingNumber);
            return logSterlingNumber;
        } else {
            return lsn.doubleValue();
        }
    }

    private double logBracket(double a, double b, int c){
        double logBracket;

        logBracket = 0.0;

        for(int i = 0; i < c; i++){
            logBracket += Math.log(a + (double) i * b);
        }

        return logBracket;
    }

    private int breakOffTable(double d, int c, int t){
        double[] p;
        double tw, cuSum, r, maxp;
        int tableSize;

        if(t == 1){
            return c;
        }

        if(c == t){
            return 1;
        }

        p = new double[c - t + 1];
        maxp = Double.NEGATIVE_INFINITY;

        for(int i = 1; i <= p.length; i++){
            p[i-1] = logSterlingNumber(d,c-i,t-1) + logBracket(1.0 - d, 1.0, i - 1) + logChoose(c - t + 1,i);
            maxp = (p[i-1] > maxp) ? p[i-1] : maxp;
        }

        tw = 0.0;
        for(int i = 0 ; i < p.length; i++){
            p[i] = Math.exp(p[i] - maxp);
            tw += p[i];
        }

        cuSum = 0.0;
        r = OnlineSequenceMemoizer.RNG.nextDouble();
        tableSize = -1;
        for(int i = 0; i < p.length; i++){
            p[i] /= tw;
            cuSum += p[i];
            if(cuSum > r){
                tableSize = i + 1;
                break;
            }
        }
        
        assert tableSize > 0;

        return tableSize;
    }

    private int[] instantiateTSA(double d, int c, int t){
        int[] tsa;

        tsa = new int[t];

        for(int i = 0; i < tsa.length; i++){
            tsa[i] = breakOffTable(d, c, t);
            c -= tsa[i];
            t--;
        }

        assert t == 0;
        assert c == 0;

        return tsa;
    }

    private double logAdd(double a, double b){
        double max, logAdd;

        if(a == Double.NEGATIVE_INFINITY && b == Double.NEGATIVE_INFINITY){
            return Double.NEGATIVE_INFINITY;
        }

        max = (a > b) ? a : b;

        a -= max;
        b -= max;

        logAdd = max + Math.log(Math.exp(a) + Math.exp(b));

        return logAdd;
    }

    private static HashMap<Integer, Double> logFactorialMemoizer = new HashMap<Integer, Double>(1000);
    private double logFactorial(int n){
        double logFactorial;
        Double lf;

        if(n == 1 || n == 0){
            return 0.0;
        }

        lf = logFactorialMemoizer.get(n);

        if(lf == null){
            logFactorial = Math.log(n) + logFactorial(n-1);
            logFactorialMemoizer.put(n,logFactorial);
            return logFactorial;
        } else {
            return lf.doubleValue();
        }
    }

    private double logChoose(int n, int k){
        assert k <= n;
        return logFactorial(n) - logFactorial(k) - logFactorial(n-k);
    }

    public void printState(){
        int[] tsa;
        for(Integer type : tableConfig.keySet()){
            tsa = tableConfig.get(type);
            System.out.print(type + " : [" + tsa[0]);
            for(int i = 1; i < tsa.length; i++){
                System.out.print(", " + tsa[i]);
            }
            System.out.println("]");
        }
    }

    public static void main(String[] args){
        OnlineRestaurant r = new OnlineRestaurant(null, null,null);

        int[] tsa = r.instantiateTSA(.8, 30, 7);
        for(int i = 0; i < tsa.length; i++){
            System.out.println(tsa[i]);
        }        
    }
}
