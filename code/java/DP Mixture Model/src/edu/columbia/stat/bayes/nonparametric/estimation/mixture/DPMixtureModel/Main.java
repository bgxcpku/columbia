/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.bayes.nonparametric.estimation.mixture.DPMixtureModel ;
import java.util.ArrayList ;
/**
 *
 * @author nicholasbartlett
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //there will be some 8 actual clusters, vocabulary here is 1000 ;
        DiscreteDistrib[] clusterParams = new DiscreteDistrib[8] ;
        DirichletDistrib baseMeasure = new DirichletDistrib(1,10000) ;
        for(int j=0; j<8; ++j){
            clusterParams[j] = baseMeasure.sample() ;
        }

        //print the cluster parameters ;
        /*for(int j=0; j<8; ++j){
            StringBuilder paramLine = new StringBuilder() ;
            for(int k=0;k<100;++k){
                paramLine.append(clusterParams[j].parameter[k] + ", ") ;
            }
            System.out.println(paramLine) ;
        }*/

        //create some data
        int n = 100 ;
        ArrayList<BagOfWordsObservation> data = new ArrayList(n) ;
        for(int j=0; j<n; ++j){
            data.add(clusterParams[j%8].sample(5000)) ;
            //System.out.println(j%8) ;
        }

        //print the data
        /*for(int j=0; j<n; ++j){
            StringBuilder line = new StringBuilder() ;
            for(int k=0; k<100; ++k){
                line.append(data.get(j).value[k] + ", ") ;
            }
            System.out.println(j) ;
            System.out.println(line) ;
        }*/

        Restaurant rest = new  Restaurant(data,baseMeasure) ;
        System.out.println(rest.size()) ;


        double meanPWA = 0 ;
        int iters = 100;
        for(int iter=0; iter<iters;++iter) {
            rest.reSeatRestaurant() ;
            rest.reSampleTables() ;
            rest.reSampleAlpha(.1, 100) ;
            
            for(Population p:rest.values()){
                System.out.print(p.size() + ", ") ;
            }

            System.out.println() ;
            System.out.print(rest.size() + ", ") ;

            double pwa = pairWiseAccuracey(rest) ;
            meanPWA += pwa ;
            System.out.print(rest.alpha + ", ") ;
            System.out.print(pwa) ;
            System.out.println() ;
        }
        System.out.println(meanPWA/iters) ; 
    }

    private static double pairWiseAccuracey(Restaurant r){
        //invert the hashmap like in reSeat tables
        DiscreteDistrib[] tableList = new DiscreteDistrib[r.dataset.size()];
        for (DiscreteDistrib t : r.keySet()) {
            for (Integer obs : (Integer[]) r.get(t).toArray(new Integer[0])) {
                tableList[obs] = t;
            }
        }

        int total = 0 ;
        int correctClassify = 0 ;
        for(int i=0; i<100; i++){
            for(int j=(i+1); j<100; j++){
                if(i%8 == j%8 && tableList[i] == tableList[j]) ++correctClassify ;
                else if(i%8 != j%8 && tableList[i] != tableList[j]) ++correctClassify ;
                ++total ;
            }
        }

        return 1.0*correctClassify/total ;
    }
}
