/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

import java.io.File;
import java.net.URL;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author nicholasbartlett
 */
public class CommandLineOptions {

    public static Options getOptions(){
        Options options = new Options();

        options.addOption("file", true, "Fully specified path(s) to file or files to deplump");
        options.getOption("file").setArgs(Integer.MAX_VALUE);

        options.addOption("d","depth", true, "Maximum depth of the model");

        options.addOption("lr", "limitRestaurants", true, "Maximum number of nodes the Sequence Memoizer model is allowed to instantiate");

        options.addOption("lsl", "limitSeqeunceLength", true, "Maximum length (in bytes) of the sequence kept by the Sequence Memoizer for the "
                + "underlying tree structure to reference");

        options.addOption("i", "insert", true, "Should the model should be updated during compression?");

        options.addOption("url", "urlSerializedModel", true, "URL of a saved predictive model");

        options.addOption("saveModelFileName", "fileNameToSerializeLearnedModelTo", true, "File to which to save predictive model");


        return options;
    }

    public static ParseReturn parse(String[] args){

        Options options = getOptions();
        CommandLineParser clp = new BasicParser();
        CommandLine cl = null;
        
        try{
            cl = clp.parse(options, args);
        } catch (ParseException pe) {
            System.err.println( "Parsing failed.  Reason: " + pe.getMessage());
            System.out.println();

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "deplump", options );

            System.exit(1);
        }

        ParseReturn parseReturn = new ParseReturn();

        if (cl.getOptionValue("lr") != null) {
                parseReturn.maxNumberRestaurants = Long.parseLong(cl.getOptionValue("lr"));
        }

        if (cl.getOptionValue("lsl") != null) {
            parseReturn.maxSequenceLength = Long.parseLong(cl.getOptionValue("lsl"));
        }

        if (cl.getOptionValue("depth") != null) {
            parseReturn.depth = Integer.parseInt(cl.getOptionValue("d"));
        }

        if (cl.getOptionValue("i") != null) {
            parseReturn.insert = Boolean.parseBoolean(cl.getOptionValue("i"));
        }

        if (cl.getOptionValue("url") != null) {
            try {
                parseReturn.url = new URL(cl.getOptionValue("url"));
            } catch (java.net.MalformedURLException e){
                System.out.println("This is an invalid URL " + cl.getOptionValue("url"));
                System.out.println();
                System.exit(1);
            } 
        }

        String[] fileArray = cl.getOptionValues("file");
        if (fileArray != null) {
            parseReturn.files = new File[fileArray.length];
            int fileIndex = 0;
            for (String file : fileArray) {
                parseReturn.files[fileIndex++] = new File(file);
            }
        }

        String saveFileName = cl.getOptionValue("saveModelFileName");
        if (saveFileName != null) {
            parseReturn.saveModel = true;
            parseReturn.modelSaveFile = new File(saveFileName);
        }

        return parseReturn;
    }

    public static class ParseReturn{
        public long maxNumberRestaurants = -1;
        public long maxSequenceLength = -1;
        public int depth = -1;
        public boolean insert = true;
        public URL url = null;
        public boolean saveModel = false;
        public File modelSaveFile = null;
        public File[] files = null;
    }
}
