/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.fdhpyp;


/**
 *
 * @author nicholasbartlett
 */
public class RestaurantFranchise {
    private  Restaurant root;
    private Discounts discounts;
    private Concentrations concentrations;
    private int depth;

    public RestaurantFranchise(int depth){
        discounts = new Discounts(new double[]{0.05, .7 , 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95});
        concentrations = new Concentrations();
        this.depth = depth;
        root = new Restaurant(null,discounts.get(0),concentrations.get(0));
    }

    private int[] currentContext;
    public double continueSequence(int type){
        double p;
        p = seat(type, currentContext);
        updateContext(type);
        return Math.log(p);
    }

    public double seat(int type, int[] context){
        Restaurant r;
        double p;

        r = get(context);
        p = r.predictiveProbability(type);

        r.seat(type);
        
        return p;
    }

    public void sample(){
        sampleSeating(root);
        sampleDiscounts(0.05);
    }

    public void sampleSeating(){
        sampleSeating(root);
    }

    private void sampleSeating(Restaurant r){
        r.fillSummaryStats();
        for(Restaurant child:r.values()){
            sampleSeating(child);
        }
        r.sampleSeatingArrangements();
        r.clearSummaryStats();
    }

    public void sampleDiscounts(double proposalWidth){
        double[] currentDiscounts;
        double[] currentLogLik;
        double[] r;
        MutableDouble d;
        double proposal;

        r = new double[discounts.length()];
        currentDiscounts = new double[discounts.length()];
        logLik();
        currentLogLik = new double[discounts.length()];
        System.arraycopy(dLogLik, 0, currentLogLik, 0, discounts.length());

        for(int i = 0; i<discounts.length(); i++){
            d = discounts.get(i);
            currentDiscounts[i] = d.doubleVal();
            proposal = currentDiscounts[i] + Restaurant.RNG.nextDouble() * proposalWidth - proposalWidth / 2;
            proposal = (proposal >= 1.0 || proposal <= 0.0)?currentDiscounts[i]:proposal;
            d.set(proposal);
        }

        logLik();

        for(int i = 0; i<discounts.length(); i++){
            r[i] = Math.exp(dLogLik[i] - currentLogLik[i]);
        }

        for(int i = 0; i<discounts.length(); i++){
            if(r[i] <= Restaurant.RNG.nextDouble()){
                discounts.get(i).set(currentDiscounts[i]);
            }
        }
    }

    public double logLik(){
        double logLik;

        logLik = 0.0;
        dLogLik = new double[discounts.length()];
        cLogLik = new double[concentrations.length()];
        
        logLik(root,0);
        for(double d:dLogLik){
            logLik += d;
        }
        
        return logLik;
    }

    private double[] dLogLik;
    public double[] cLogLik;
    public void logLik(Restaurant r, int d){
        double logLik;
        int cIndex;
        int dIndex;

        dIndex = (d<discounts.length())?d:(discounts.length()-1);
        cIndex = (d<concentrations.length())?d:(concentrations.length()-1);

        logLik = r.logLik();

        dLogLik[dIndex] += logLik;
        cLogLik[cIndex] += logLik;
        
        for(Restaurant child:r.values()){
            logLik(child, d+1);
        }
    }

    private Restaurant get(int[] context){
        Restaurant current;
        Restaurant child;
        int d;
        int ci;

        if(context == null){
            return root;
        }

        ci = context.length -1;
        d = 0;
        current = root;
        child = null;

        while(d<depth && ci>-1){
            child = current.get(context[ci]);
            if(child == null){
                child = new Restaurant(current,discounts.get(d+1), concentrations.get(d+1));
                current.put(context[ci], child);
            }
            current = child;
            ci--;
            d++;
        }
        
        return child;
    }
    
    private void updateContext(int obs){
        if(currentContext == null){
            currentContext = new int[]{obs};
        } else if(currentContext.length<depth){
            int[] newContext = new int[currentContext.length + 1];
            System.arraycopy(currentContext, 0, newContext, 0, currentContext.length);
            newContext[currentContext.length] = obs;
            currentContext = newContext;
        } else {
            for(int i = 0; i< depth -1; i++){
                currentContext[i] = currentContext[i + 1];
            }
            currentContext[depth-1] = obs;
        }
    }
}
