/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.gatsby.nlp.lm.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.ServerSocket;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author fwood
 */
public class Server implements Serializable {

    private static final long serialVersionUID = 1;

    public LanguageModel getLanguageModel() {
        return languageModel;
    }

    public void setLanguageModel(LanguageModel lm) {
        languageModel = lm;
    }
    protected LanguageModel languageModel;
    int context_length;
    String arch;
    int numWordsInVocab;
    transient String serialization_directoryname;
    transient String log_filename;
    transient String bd;
    transient int port;
    transient PrintStream log_ps;
    transient FileOutputStream log_fos;
    transient ServerSocket serverSocket;

    public void saveToFile(String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);

    }

    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
//            Class languageModelClass = (Class)ois.readObject();
//            languageModelClass.
        Server savedServer = (Server) ois.readObject();

        PrintStream lm_log_stream = this.languageModel.log_ps;

        this.languageModel = savedServer.languageModel;
        this.languageModel.log_ps = lm_log_stream;
        this.context_length = savedServer.context_length;
        this.arch = savedServer.arch;
        this.numWordsInVocab = savedServer.numWordsInVocab;
    }

    public void parseArgs(String[] args) {
        // create Options object
        Options options = new Options();

        //options.addOption("onehpyp", false, "use a single hpyp instead of an hhpyp");

        Option port_opt = OptionBuilder.withArgName("num").hasArg().withDescription("port").create("port");
        Option con_len = OptionBuilder.withArgName("num").hasArg().withDescription("context length").create("cl");
        Option nw_opt = OptionBuilder.withArgName("num").hasArg().withDescription("num words in dictionary").create("nw");
        Option log_file = OptionBuilder.withArgName("file").hasArg().withDescription("log file (optional - default resides in base directory)").create("L");
        Option base_directory = OptionBuilder.withArgName("directory").hasArg().withDescription("base directory").create("B");
        Option serialization_directory = OptionBuilder.withArgName("directory").hasArg().withDescription("serialization directory (defaults to base directory)").create("S");
        Option model_architecture_option = OptionBuilder.withArgName("single_switch, per_level_switch_sharing, hpyp_switch").hasArg().withDescription("model \"switch\" arch.").create("arch");

        options.addOption(model_architecture_option);
        options.addOption(log_file);
        options.addOption(base_directory);
        options.addOption(serialization_directory);
        options.addOption(con_len);
        options.addOption(nw_opt);
        options.addOption(port_opt);

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        HelpFormatter formatter = new HelpFormatter();
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException pe) {

            if (pe instanceof MissingArgumentException) {
                System.err.println(((MissingArgumentException) pe).getLocalizedMessage());
                formatter.printHelp("app name", options);
                System.exit(-1);
            }

            formatter.printHelp("app name", options);
            System.exit(-1);
        }

        if (cmd.hasOption("arch")) {
            arch = cmd.getOptionValue("arch");
            if (!(arch.equals("single_switch") || arch.equals("per_level_switch_sharing") || arch.equals("hpyp_switch"))) {
                formatter.printHelp("Unknown switch architecture.", options);
                System.exit(-1);
            }
        }

//        if (cmd.hasOption("onehpyp")) {
//            use_single_hpyp = true;
//        } else {
//        }

        if (!cmd.hasOption("nw")) {
            formatter.printHelp("Dictionary size must be specified", options);
            System.exit(-1);
        } else {
            try {
                numWordsInVocab = Integer.parseInt(cmd.getOptionValue("nw"));
            } catch (NumberFormatException nfe) {
                formatter.printHelp("Num words in dictionary must be a number", options);
                System.exit(-1);
            }
        }

        if (!cmd.hasOption("cl")) {
            formatter.printHelp("Context length must be supplied", options);
            System.exit(-1);
        } else {
            try {
                context_length = Integer.parseInt(cmd.getOptionValue("cl"));
            } catch (NumberFormatException nfe) {
                formatter.printHelp("Context length must be a number", options);
                System.exit(-1);
            }
        }

        if (!cmd.hasOption("port")) {
            formatter.printHelp("Port must be supplied", options);
            System.exit(-1);
        } else {
            try {
                port = Integer.parseInt(cmd.getOptionValue("port"));
            } catch (NumberFormatException nfe) {
                formatter.printHelp("Port must be a number", options);
                System.exit(-1);
            }
        }


        if (cmd.hasOption("B")) {
            bd = cmd.getOptionValue("B");
            if (!bd.endsWith(File.separator)) {
                bd = bd + File.separator;
            }

            File ddir = new File(bd);

            if (!ddir.exists()) {
                ddir.mkdir();
            }

            if (!ddir.isDirectory()) {
                System.err.println("Base directory given is not a directory");
                System.exit(-1);
            }

        } else {
            formatter.printHelp("must supply base directory", options);
            System.exit(-1);
        }

        String default_filename = "lm";
        default_filename = default_filename + "." + numWordsInVocab + "." + context_length + "." + port;

        if (cmd.hasOption("S")) {
            serialization_directoryname = cmd.getOptionValue("S");

            if (serialization_directoryname.equals("default")) {
                serialization_directoryname = bd;
            } else {
            }
            File ser_dir = new File(serialization_directoryname);

            if (!ser_dir.exists()) {
                ser_dir.mkdir();
            }

        } else {
            serialization_directoryname = bd;
        }



        if (cmd.hasOption("L")) {
            log_filename = cmd.getOptionValue("L");
        } else {
            log_filename = bd + default_filename + ".log";
        }

        if (!log_filename.equals("stderr")) {

            try {
                log_fos = new FileOutputStream(log_filename);
            } catch (FileNotFoundException e) {
                System.err.println("Could not write to log file " + log_filename);
            }
            log_ps = new PrintStream(log_fos);
        } else {
            log_ps = System.err;
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.parseArgs(args);
        server.initializeLanguageModel();
        server.run();
        server.finish();
    }

    public void finish() {
        log_ps.flush();
        log_ps.close();
        try {

            if (!log_filename.equals("stdio")) {
                log_fos.flush();
                log_fos.close();
            }
        } catch (Exception e) {
            System.err.println("Error flushing log file.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void initializeLanguageModel() {

        //languageModel.setVocabularySize(numWordsInVocab);

        //if (arch.equals("hpyp_switch")) {
        //run_hhpyp_experiment(c1, c2, tc, the_dictionary, context_length, burnin_sweeps, num_sweeps, results_ps, log_ps, serialization_directoryname, default_filename, do_serialization);
        //} else if (arch.equals("single_switch")) {
        //run_single_switch_pyp_hhpyp_experiment(c1, c2, tc, the_dictionary, context_length, burnin_sweeps, num_sweeps, results_ps, log_ps, serialization_directoryname, default_filename, do_serialization);
        //} else
        if (arch.equals("per_level_switch_sharing")) {
            languageModel = new PerLevelSwitchSharingDHPYPLanguageModel(context_length, numWordsInVocab, log_ps);
        //run_multiple_independent_switch_hhpyp_experiment(c1, c2, tc, the_dictionary, context_length, burnin_sweeps, num_sweeps, results_ps, log_ps, serialization_directoryname, default_filename, do_serialization);
        } else {
            System.err.println("Other switch architectures not implemented yet.");
            System.exit(-1);
        }

    }

    public void run() {

        boolean listening = true;

        int listenSocket = port;

        try {
            serverSocket = new ServerSocket(listenSocket);
            //serverSocket.getChannel().configureBlocking(false);
            serverSocket.setReceiveBufferSize(1000000);
            serverSocket.setSoTimeout(0);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + listenSocket + ".");
            e.printStackTrace();
            System.exit(-1);
        }

        while (listening) {
            try {
                new RPCStreamHandler(this, serverSocket.accept()).start();
            } catch (IOException e) {
                System.err.println("Could not listen on port: " + listenSocket + ".");
                e.printStackTrace();
                System.exit(-1);
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + listenSocket + ".");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
