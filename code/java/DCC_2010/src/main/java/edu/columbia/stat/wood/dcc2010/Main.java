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

        //defaults
        File f, g;
        String corpus = "/Users/nicholasbartlett/Documents/np_bayes/data/pride_and_prejudice/pap.gz";
        int sizeOfTree = 10000;
        int depth = 1024;
        long streamLength = 100000;
        int radix = 1;

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

        g = new File(corpus);
        cp(g);
        f = new File("/tmp/" + g.getName());

        System.out.println(corpus + "|" + sizeOfTree + "|" + depth + "|" + streamLength + "|" + radix);

        FileRadixInputStream is = new FileRadixInputStream(f, radix);

        int n = 0;
        double logLik = 0.0;
        if (radix == 1) {
            ByteSequenceMemoizer sm = new ByteSequenceMemoizer(new ByteSequenceMemoizerParameters(depth, sizeOfTree, (long) 100 * (long) sizeOfTree));

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

        } else {
            IntSequenceMemoizer sm;
            if (radix == 2) {
                sm = new IntSequenceMemoizer(new IntSequenceMemoizerParameters(depth, sizeOfTree, (long) 100 * (long) sizeOfTree, 1 << 16));
            } else if (radix == 4) {
                IntDiscreteDistribution baseDistribution = new IntUniformDiscreteDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE);
                double[] discounts = new double[]{0.5, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95};
                IntSequenceMemoizerParameters smp = new IntSequenceMemoizerParameters(baseDistribution, discounts, 0.5, depth, 3, sizeOfTree, sizeOfTree * 100);

                sm = new IntSequenceMemoizer(smp);
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
                    } else if (radix == 4) {
                        IntDiscreteDistribution baseDistribution = new IntUniformDiscreteDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE);
                        double[] discounts = new double[]{0.5, 0.7, 0.8, 0.82, 0.84, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95};
                        IntSequenceMemoizerParameters smp = new IntSequenceMemoizerParameters(baseDistribution, discounts, 0.5, depth, 3, sizeOfTree, (long) sizeOfTree * (long) 100);

                        sm = new IntSequenceMemoizer(smp);
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
        }
    }

    public static void cp(File f) throws java.io.IOException, java.lang.InterruptedException {
        File g = new File("/tmp/" + f.getName());
        if (g.exists()) {
            return;
        } else {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("cp " + f.getPath() + " /tmp/" + f.getName());
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
