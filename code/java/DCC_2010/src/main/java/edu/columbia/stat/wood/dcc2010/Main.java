/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.dcc2010;

import edu.columbia.stat.wood.sequencememoizer.v1.ByteSequenceMemoizer;
import edu.columbia.stat.wood.sequencememoizer.v1.ByteSequenceMemoizerParameters;
import edu.columbia.stat.wood.sequencememoizer.v1.IntSequenceMemoizer;
import edu.columbia.stat.wood.sequencememoizer.v1.IntSequenceMemoizerParameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
    
    public static void main(String[] args) throws FileNotFoundException, IOException{
        int nargs = args.length;

        //defaults
        File f;
        String corpus = "/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/alice_in_wonderland.txt.gz";
        int sizeOfTree = 1000000;
        int depth = 1024;
        long streamLength = Long.MAX_VALUE;
        int radix = 1;

        if(nargs > 0){
            corpus = args[0];
        }
        if(nargs > 1){
            sizeOfTree = Integer.parseInt(args[1]);
        }
        if(nargs > 2){
            depth = Integer.parseInt(args[2]);
        }
        if(nargs > 3){
            streamLength = Long.parseLong(args[3]);
        }
        if(nargs > 4){
            radix = Integer.parseInt(args[4]);
        }

        f = new File(corpus);
        
        System.out.println("corpus" + "|" + sizeOfTree + "|" + depth + "|" + streamLength + "|" + radix);

        FileRadixInputStream is = new FileRadixInputStream(f,radix,streamLength);

        double logLik = 0.0;
        if(radix == 1){
            ByteSequenceMemoizer sm = new ByteSequenceMemoizer(new ByteSequenceMemoizerParameters(depth,sizeOfTree, 100 * sizeOfTree));

            long l;
            while((l = is.readLong()) > -1){

                if(is.bytesRead > streamLength){
                    System.out.println((is.bytesRead - radix) + "|"  + logLik + "|" + (-logLik / Math.log(2) / ((double) is.bytesRead - radix)));
                    is.bytesRead = radix;
                    logLik = 0.0;
                    sm = new ByteSequenceMemoizer(new ByteSequenceMemoizerParameters(depth,sizeOfTree, 100 * sizeOfTree));
                }

                logLik += sm.continueSequence((byte) l);

                if(is.bytesRead == 1000000){
                    System.out.println(is.bytesRead + "|"  + logLik + "|" + (-logLik / Math.log(2) / (double) is.bytesRead));
                    logLik = 0.0;
                }
            }
            System.out.println(is.bytesRead + "|"  + logLik + "|" + (-logLik / Math.log(2) / (double) is.bytesRead));

        } else {
            IntSequenceMemoizer sm;
            if(radix == 2){
                sm = new IntSequenceMemoizer(new IntSequenceMemoizerParameters(depth,sizeOfTree, 100 * sizeOfTree, 1 << 16));
            } else {
                throw new RuntimeException("Must use radix 1,2");
            }

            long l;
            while((l = is.readLong()) > -1){

                if(is.bytesRead > streamLength){
                    System.out.println((is.bytesRead - radix) + "|"  + logLik + "|" + (-logLik / Math.log(2) / ((double) is.bytesRead - radix)));
                    is.bytesRead = radix;
                    logLik = 0.0;
                    sm = new IntSequenceMemoizer(new IntSequenceMemoizerParameters(depth,sizeOfTree, 100 * sizeOfTree, 1 << 16));
                }


                logLik += sm.continueSequence((int) l);

                if(is.bytesRead == 1000000){
                    System.out.println(is.bytesRead + "|"  + logLik + "|" + (-logLik / Math.log(2) / (double) is.bytesRead));
                    logLik = 0.0;
                }
            }
            System.out.println(is.bytesRead + "|"  + logLik + "|" + (-logLik / Math.log(2) / (double) is.bytesRead));
        }
    }
}
