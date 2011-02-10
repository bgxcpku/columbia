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
     * @param1 = depth, if there is a max depth, else -1
     * @param2 = maxNumberRest, if there is a max number
     * @param3 = seed , random number generator seed
     * @param4 = maxRunLength (for byte seater before switching to run length encoder)
     * @param5 = maxFileLength (for byte reader)
     * @param6 = path to data (ex : Documents/NP Bayes/data/calgary_corpus/)
     * @param7 ... files, should be in folder indicated by path
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        if (args.length == 0) {
            String[] newArgs = {"SIMPLE", "-1", "-1", "0", "1000", "2000000",
                "/Users/fwood/Data/natural_language/","lmdata-nyt"};
            args = newArgs;
        }

        SeatingStyle seatingStyle = SeatingStyle.SIMPLE;
        Integer depth = new Integer(-1);
        Long seed = new Long(123);
        Integer maxNumberRestaurants = null;
        Integer maxRunLength;
        Integer maxFileLength;

        if (args[0].equals("SIMPLE")) {
            seatingStyle = SeatingStyle.SIMPLE;
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
        maxRunLength = Integer.valueOf(args[4]);
        maxFileLength = Integer.valueOf(args[5]);

        String[] filesToRead = {"bib", "book1", "book2", "geo", "news",
            "obj1", "obj2", "paper1", "paper2", "pic", "progc", "progl", "progp", "trans"};
        if (args.length > 7) {
            filesToRead = new String[args.length - 7];
            for (int fileIndex = 0; fileIndex < filesToRead.length; fileIndex++) {
                filesToRead[fileIndex] = args[7 + fileIndex];
            }
        }

        System.out.println("args used were: ");
        for (int j = 0; j < args.length; j++) {
            System.out.println(args[j]);
        }

        FileTranslatorByte ftb = new FileTranslatorByte();
        int[][] translation = ftb.translateFile(args[6], filesToRead, maxFileLength);

        System.out.println("Compressing the documents ");
        for (int j = 0; j < translation.length; j++) {
            System.out.println(filesToRead[j]);
        }

        ByteSeater seater = new ByteSeater(seed, maxRunLength.intValue());
        long startTime;
        long endTime;

        double secondsToComplete;
        double[] minutes = new double[translation.length];
        double[] bitsPerByte = new double[translation.length];
        double[] totalBytes = new double[translation.length];
        double[] numberRestAtFinish = new double[translation.length];

        double[] seatStats;
        SMTree sm = new SMTree(256, depth, maxNumberRestaurants, seatingStyle);
        for (int file = 0; file < translation.length; file++) {
            if (file > 0) {
                sm.seq.incrementSeq();
            }
            System.out.println("Working on file: " + filesToRead[file] + " (size: " + translation[file].length + " bytes)");

            startTime = System.nanoTime();

            //sm = new SMTree(256,depth, maxNumberRestaurants,seatingStyle);
            seatStats = seater.seatSequence(translation[file], sm); //, seatingStyle, depth, maxNumberRestaurants);

            /*
            for (int k = 0; k < 15; k++) {
                sm.sampleDiscounts();
                sm.sampleSeating();
                System.out.println(sm.getLogLik());
                sm.discounts.print();
            }*/

            int[] genData = sm.generateData(1000);
            for (int k = 0; k < 1000; k++) {
                System.out.print(new Character((char) genData[k]) + ",");
            }

            endTime = System.nanoTime();

            secondsToComplete = (endTime - startTime) / Math.pow(10, 9);
            minutes[file] = secondsToComplete / 60.0;
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
