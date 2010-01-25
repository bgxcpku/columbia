/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.icml2010;

import java.io.FileNotFoundException;
import java.io.IOException;
import edu.columbia.stat.wood.sequencememoizer.*;

/**
 *
 * @author nicholasbartlett
 */
public class Main {

    /**
     * @param args the command line arguments     
     * @param0 = seatingstyle, must be one of the enums
     * @param1 = depth, if there is a a max depth, -1
     * @param2 = maxNumberRest, if there is a max number
     * @param3 = seed , random number generator seed
     * @param4 = path to data (ex : Documents/NP Bayes/data/calgary_corpus/)
     * @param5 ... files, should be in folder indicated by path
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        if (args.length == 0) {
            String[] newArgs = {"SIMPLE", "-1", "-1", "0",
            "/Users/nicholasbartlett/Documents/NP Bayes/data/calgary_corpus/", "bib", "book1", "book2"};
            args = newArgs;
        }

        SeatingStyle seatingStyle = SeatingStyle.SIMPLE;
        Integer depth = new Integer(-1);
        Long seed = new Long(123);
        Integer maxNumberRestaurants = null;

        if (args[0].equals("SIMPLE")) {
            seatingStyle = SeatingStyle.SIMPLE;
        } else if (args[0].equals("SIMPLE_BOUNDED_MEMORY")) {
            seatingStyle = SeatingStyle.SIMPLE_BOUNDED_MEMORY;
        } else if (args[0].equals("RANDOM_DELETION")) {
            seatingStyle = SeatingStyle.RANDOM_DELETION;
        } else if (args[0].equals("DISANTLY_USED_DELETION")) {
            seatingStyle = SeatingStyle.DISANTLY_USED_DELETION;
        } else if (args[0].equals("BAYES_FACTOR_DELETION")) {
            seatingStyle = SeatingStyle.BAYES_FACTOR_DELETION;
        }

        depth = Integer.valueOf(args[1]);
        maxNumberRestaurants = Integer.valueOf(args[2]);
        seed = Long.valueOf(args[3]);


        String[] filesToRead = {"bib", "book1", "book2", "geo", "news",
            "obj1", "obj2", "paper1", "paper2", "pic", "progc", "progl", "progp", "trans"};
        if (args.length > 5) {
            filesToRead = new String[args.length - 5];
            for (int fileIndex = 0; fileIndex < filesToRead.length; fileIndex++) {
                filesToRead[fileIndex] = args[5 + fileIndex];
            }
        }

        System.out.println("args used were: ");
        for (int j = 0; j < args.length; j++) {
            System.out.println(args[j]);
        }

        FileTranslatorByte ftb = new FileTranslatorByte();
        int[][] translation = ftb.translateFile(args[4], filesToRead);

        System.out.println("Compressing the documents ");
        for (int j = 0; j < translation.length; j++) {
            System.out.println(filesToRead[j]);
        }

        ByteSeater seater = new ByteSeater(seed);
        long startTime;
        long endTime;

        double secondsToComplete;
        double[] minutes = new double[translation.length];
        double[] bitsPerByte = new double[translation.length];
        double[] totalBytes = new double[translation.length];
        double[] numberRestAtFinish = new double[translation.length];

        double[] seatStats;
        for (int file = 0; file < translation.length; file++) {
            System.out.println("Working on file: " + filesToRead[file] + " (size: " + translation[file].length + " bytes)");
            startTime = System.nanoTime();
            seatStats = seater.seatByteSequence(translation[file], seatingStyle, depth, maxNumberRestaurants);
            endTime = System.nanoTime();

            secondsToComplete = (endTime - startTime) / Math.pow(10, 9);
            minutes[file] = secondsToComplete/60.0;
            bitsPerByte[file] = seatStats[0];
            totalBytes[file] = seatStats[1];
            numberRestAtFinish[file] = seatStats[2];
        }

        System.out.println();
        System.out.println("Bits per Byte for each file:");
        for (int file = 0; file < translation.length; file++) {
            System.out.println(bitsPerByte[file]);
        }
        System.out.println();

        System.out.println("Total Bytes for each file:");
        for (int file = 0; file < translation.length; file++) {
            System.out.println(totalBytes[file]);
        }
        System.out.println();

        System.out.println("number of rest at finish:");
        for (int file = 0; file < translation.length; file++) {
            System.out.println(numberRestAtFinish[file]);
        }
        System.out.println();

        System.out.println("Minutes to complete each file:");
        for (int file = 0; file < translation.length; file++) {
            System.out.println(minutes[file]);
        }
    }
}