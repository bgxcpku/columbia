/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.dcc2010;

import edu.columbia.stat.wood.sequencememoizer.v1.ByteSequenceMemoizer;
import edu.columbia.stat.wood.sequencememoizer.v1.ByteSequenceMemoizerParameters;
import edu.columbia.stat.wood.sequencememoizer.v1.util.ByteRestaurant;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */
public class Debug {
/*
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String corpus = "/home/bartlett/enwik.xml.gz";
        int sizeOfTree = 10000000;
        int depth = 1048576;
        long streamLength = 100000000;
        int radix = 1;
        int maxCustomersInRestaurant = 10000;

        File f = new File(corpus);

        FileRadixInputStream is = new FileRadixInputStream(f, radix);

        int n = 0;

        ByteSequenceMemoizer sm = new ByteSequenceMemoizer(new ByteSequenceMemoizerParameters(depth, sizeOfTree, (long) 100 * (long) sizeOfTree));
        sm.maxCustomersInRestaurant = maxCustomersInRestaurant;

        int bytesLogLik = 0;
        double logLik = 0.0;
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
    }*/
}
