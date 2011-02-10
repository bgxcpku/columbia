/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stats.hdplda;
import java.util.ArrayList;
import java.util.HashMap ;
import java.util.HashSet;

/**
 *
 * @author nicholasbartlett
 */
public class LowLevelRestaurant extends HashMap<TableLabel,HashSet<Token>> {
    public TopLevelRestaurant topLevelRestaurant ;
    public SetableDouble alpha;

    public LowLevelRestaurant(TopLevelRestaurant topLevelRestaurant, SetableDouble alpha){
        super() ;
        this.topLevelRestaurant = topLevelRestaurant ;
        this.alpha = alpha;
    }

    public void addDocument(Document document) {
        for(Type type : document.keySet()) {
            for(int count = 0; count < document.get(type).getValue();count++) {
                addWord(type.getType());
            }
        }
    }

    public double reseatCustomers() {
        HashMap<Token, TableLabel> inverseMap = new HashMap<Token,TableLabel>(30);
        for(TableLabel label : keySet()) {
            for(Token t : this.get(label)) {
                inverseMap.put(t, label);
            }
        }

        for(Token t : inverseMap.keySet()) {
            this.removeToken(t,inverseMap.get(t));
            this.addToken(t);
        }

        return 0.0;
    }

    //method to remove word
    public void removeToken(Token token, TableLabel label) {
        get(label).remove(token);
        // need to do bookkeeping on global populations (and the low level as well)
    }

    //method to add word
    public void addToken(Token token){
          double[] logWeights = new double[keySet().size() + 1];

            //set weight values for all active tables
            int i = 0;
            ArrayList<TableLabel> tableOrder = new ArrayList<TableLabel>(size());
            for (TableLabel label : keySet()) {
                logWeights[i++] = Math.log(get(label).size()) +
                        label.get().getLogLikelihood(token);
                tableOrder.add(label);
            }

            //find weight of last element which is integrated distn against
            //the base measure

            Pair<Double,DiscreteDistrib> retval = topLevelRestaurant.getMeasure(token);

            logWeights[keySet().size()] = Math.log(alpha.getValue()) + retval.first();

            //re-scale weights to make numerically possible
            double weightScale = logWeights[0];
            for (int w = 1; w < logWeights.length; w++) {
                if (logWeights[w] > weightScale) {
                    weightScale = logWeights[w];
                }
            }

            double totWeight = 0;
            for (int w = 0; w < logWeights.length; ++w) {
                if (logWeights[w] != Double.NEGATIVE_INFINITY) {
                    logWeights[w] -= weightScale;
                    totWeight += Math.exp(logWeights[w]);
                }
            }

            //Sample new cluster indicator variable
            int z = 0;
            double cumSum = 0;
            double rawSample = Math.random();
            for (int w = 0; w < logWeights.length; ++w) {
                cumSum += Math.exp(logWeights[w]) / totWeight;
                if (rawSample < cumSum) {
                    z = w;
                    break;
                }
            }

            //add obs to correct population
            if (z < size()) {
                get(tableOrder.get(z)).add(token);
            } else {
                TableLabel newTableLabel = new TableLabel(retval.second());

                HashSet<Token> hs = new HashSet<Token>();
                hs.add(token);
                put(newTableLabel,hs);
                topLevelRestaurant.add(newTableLabel.get(),token);
            }
        }
    }

}
