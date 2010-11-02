/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.dcc2010;

import edu.columbia.stat.wood.sequencememoizer.v1.ByteSequenceMemoizer;
import edu.columbia.stat.wood.sequencememoizer.v1.ByteSequenceMemoizerParameters;
import edu.columbia.stat.wood.sequencememoizer.v1.IntSequenceMemoizer;
import edu.columbia.stat.wood.sequencememoizer.v1.IntSequenceMemoizerParameters;
import edu.columbia.stat.wood.sequencememoizer.v1.util.ByteRestaurant;
import edu.columbia.stat.wood.sequencememoizer.v1.util.IntRestaurant;
import edu.columbia.stat.wood.util.IntDiscreteDistribution;
import edu.columbia.stat.wood.util.IntUniformDiscreteDistribution;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author nicholasbartlett
 * args are
 *
 * Corpus
 * size of tree
 * depth
 * stream length
 * radix (bytes)
 */
public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException, java.lang.InterruptedException {
        int nargs = args.length;

        File f, g;
        String corpus = "/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/pap.gz";
        int sizeOfTree = 1000000;
        int depth = 16;
        long streamLength = 700000;
        int radix = 1;
        int maxCustomersInRestaurant = 10000;
        int nSample = 10;

        if (nargs > 0) {
            corpus = args[0];
        }
        if (nargs > 1) {
            sizeOfTree = Integer.parseInt(args[1]);
        }
        if (nargs > 2) {
            depth = Integer.parseInt(args[2]);
        }
        if (nargs > 3) {
            streamLength = Long.parseLong(args[3]);
        }
        if (nargs > 4) {
            radix = Integer.parseInt(args[4]);
        }
        if (nargs > 5) {
            maxCustomersInRestaurant = Integer.parseInt(args[5]);
        }
        if (nargs > 6){
            nSample = Integer.parseInt(args[6]);
        }

        g = new File(corpus);
        cp(g);
        f = new File("/spare/hpc/tmp/stats/projects/" + g.getName());
        //f = new File(corpus);

        System.out.println(corpus + "|" + sizeOfTree + "|" + depth + "|" + streamLength + "|" + radix + "|" + maxCustomersInRestaurant);
        
        ByteSequenceMemoizer sm = null;
        FileRadixInputStream is = null;

        try{
            for(int n = 0; n < nSample ; n++){

                is = new FileRadixInputStream(f, radix, streamLength);
                sm = new ByteSequenceMemoizer(new ByteSequenceMemoizerParameters(depth, sizeOfTree, (long) 100 * (long) sizeOfTree));
                sm.maxCustomersInRestaurant = maxCustomersInRestaurant;
                ByteRestaurant.count = 1;

                double l;
                double logLik = 0.0;
                
                while(is.bytesRead < streamLength && (l = is.readLong()) > -1){

                    logLik += sm.continueSequence((byte) l);

                    if(is.bytesRead % 1000000 == 0){
                        System.out.println(is.bytesRead + "|" + 1000000 + "|" + logLik + "|" + (-logLik / Math.log(2) / 1000000.0));
                        logLik = 0.0;
                    }
                }

                if(is.bytesRead % 1000000 != 0){
                    System.out.println(is.bytesRead + "|" + (is.bytesRead % 1000000) + "|" + logLik + "|" + (-logLik / Math.log(2) / (double)(is.bytesRead % 1000000)));
                }
                
                is.close();
            }
        } finally {
            is.close();
        }

        
        /*
        int n = 0;
        double logLik = 0.0;
        if (radix == 1) {
            ByteSequenceMemoizer sm = new ByteSequenceMemoizer(new ByteSequenceMemoizerParameters(depth, sizeOfTree, (long) 100 * (long) sizeOfTree));
            sm.maxCustomersInRestaurant = maxCustomersInRestaurant;

            int bytesLogLik = 0;
            long l;
            while ((l = is.readLong()) > -1 && n < 5) {

                if (is.bytesRead > streamLength) {
                    System.out.println((is.bytesRead - radix) + "|" + bytesLogLik + "| " + logLik + "|" + (-logLik / Math.log(2) / (double) bytesLogLik));
                    is.bytesRead = radix;
                    bytesLogLik = 0;
                    logLik = 0.0;
                    ByteRestaurant.count = 0;
                    n++;
                    sm = new ByteSequenceMemoizer(new ByteSequenceMemoizerParameters(depth, sizeOfTree, (long) 100 * (long) sizeOfTree));
                    sm.maxCustomersInRestaurant = maxCustomersInRestaurant;
                }

                logLik += sm.continueSequence((byte) l);
                bytesLogLik += radix;

                if (bytesLogLik == 1000000) {
                    System.out.println(is.bytesRead + "|" + bytesLogLik + "| " + logLik + "|" + (-logLik / Math.log(2) / (double) bytesLogLik));
                    bytesLogLik = 0;
                    logLik = 0.0;
                }
            }

            System.out.println(is.bytesRead + "|" + bytesLogLik + "| " + logLik + "|" + (-logLik / Math.log(2) / (double) bytesLogLik));

        }
        /*else {
            IntSequenceMemoizer sm;
            if (radix == 2) {
                sm = new IntSequenceMemoizer(new IntSequenceMemoizerParameters(depth, sizeOfTree, (long) 100 * (long) sizeOfTree, 1 << 16));
                sm.maxCustomersInRestaurant = maxCustomersInRestaurant;
            } else if (radix == 4) {
                IntDiscreteDistribution baseDistribution = new IntUniformDiscreteDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE);
                double[] discounts = new double[]{0.5, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95};
                IntSequenceMemoizerParameters smp = new IntSequenceMemoizerParameters(baseDistribution, discounts, 0.5, depth, 3, sizeOfTree, sizeOfTree * 100);

                sm = new IntSequenceMemoizer(smp);
                sm.maxCustomersInRestaurant = maxCustomersInRestaurant;
            } else {
                throw new RuntimeException("Must use radix 1,2,4");
            }

            int bytesLogLik = 0;
            long l;
            while ((l = is.readLong()) > -1 && n < 5) {

                if (is.bytesRead > streamLength) {
                    System.out.println((is.bytesRead - radix) + "|" + bytesLogLik + "| " + logLik + "|" + (-logLik / Math.log(2) / bytesLogLik));
                    is.bytesRead = radix;
                    bytesLogLik = 0;
                    logLik = 0.0;

                    IntRestaurant.count = 0;
                    if (radix == 2) {
                        sm = new IntSequenceMemoizer(new IntSequenceMemoizerParameters(depth, sizeOfTree, (long) 100 * (long) sizeOfTree, 1 << 16));
                        sm.maxCustomersInRestaurant = maxCustomersInRestaurant;
                    } else if (radix == 4) {
                        IntDiscreteDistribution baseDistribution = new IntUniformDiscreteDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE);
                        double[] discounts = new double[]{0.5, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95};
                        IntSequenceMemoizerParameters smp = new IntSequenceMemoizerParameters(baseDistribution, discounts, 0.5, depth, 3, sizeOfTree, (long) sizeOfTree * (long) 100);

                        sm = new IntSequenceMemoizer(smp);
                        sm.maxCustomersInRestaurant = maxCustomersInRestaurant;
                    }
                    n++;
                }

                logLik += sm.continueSequence((int) l);
                bytesLogLik += radix;

                if (bytesLogLik == 1000000) {
                    System.out.println(is.bytesRead + "|" + bytesLogLik + "| " + logLik + "|" + (-logLik / Math.log(2) / (double) bytesLogLik));
                    bytesLogLik = 0;
                    logLik = 0.0;
                }
            }
            System.out.println(is.bytesRead + "|" + bytesLogLik + "| " + logLik + "|" + (-logLik / Math.log(2) / (double) bytesLogLik));
        }*/
    }

    public static void cp(File f) throws java.io.IOException, java.lang.InterruptedException {
        File g = new File("/spare/hpc/tmp/stats/projects/" + f.getName());
        if (g.exists()) {
            return;
        } else {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("cp " + f.getPath() + " /spare/hpc/tmp/stats/projects/" + f.getName());
            proc.waitFor();
            InputStream is = proc.getErrorStream();
            int b;
            while ((b = is.read()) > -1) {
                System.err.print((char) b);
            }
            System.out.println();
            is.close();
        }
    }
}
