/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stats.dataprocessing;

import edu.columbia.stats.hdplda.BagOfWordsObservation;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author fwood
 */
public class Main {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Insufficient number of arguments. Usage:\n" +
                    "THIS path word");
        }

        String path = args[0];
        String word = args[1];

        //read dictionary
        Corpus corpus = new Corpus();
        corpus.readInFile(path + File.separator + "wordsOnly.txt");

        //read document for specified word
        try {
            FileInputStream fstream = new FileInputStream(path + File.separator + "target_" + word);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                ArrayList<Integer> bag = new ArrayList<Integer>();
//                System.out.println(strLine);

                strLine = strLine.trim();
                String[] words_on_line = strLine.split(" ");
                if (words_on_line.length != 0 && !(words_on_line.length == 1 && words_on_line[0].equals(""))) {
                    for (String token : words_on_line) {
                        String[] ws = token.split("/");
                        if (ws.length == 0) {
                            continue;
                        }
                        String w = ws[0];
                        w = w.toLowerCase();
                        w = w.trim();
                        int c = corpus.getDictionary().lookup(w);
                        bag.add(c);
                    }
                }
                int[] intBag = new int[bag.size()];
                for (int i = 0; i < bag.size(); i++) {
                    intBag[i] = bag.get(i);
                }
                BagOfWordsObservation obs = new BagOfWordsObservation(intBag);
            }
            in.close();
        } catch (Exception e) {
            System.out.println("Problem reading " + path + File.separator + "target_" + word);
            e.printStackTrace();
            System.exit(0);
        }
    }
}
