/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stats.dataprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author piero
 */
public class Corpus {

    private Dictionary dictionary;

    public Corpus() {
        dictionary = new Dictionary();
    }

    public Dictionary getDictionary() {
        return this.dictionary;
    }

    protected ArrayList<Integer> readInFile(String fn) {
        File f = new File(fn);
        FileInputStream fis = null;
        BufferedReader in = null;
        try {
            fis = new FileInputStream(f);
            in = new BufferedReader(new InputStreamReader(fis));
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File "+fn+" not found");
            //e.printStackTrace();
            System.exit(-1);
        }

        ArrayList<Integer> tc = new ArrayList<Integer>();

        try {
            String line = in.readLine();
            while (line != null) {
                line = line.trim();
                String[] words_on_line = line.split(" ");
                if (words_on_line.length != 0 && !(words_on_line.length == 1 && words_on_line[0].equals(""))) {

                    for (String word : words_on_line) {
                        String[] ws = word.split("/");
                        if (ws.length == 0) continue;
                        String w = ws[0];
                        w = w.toLowerCase();
                        w = w.trim();
                        int c = dictionary.lookupOrAddIfNotFound(w);
                        tc.add(c);
                    //max_tokens++;
                    //System.out.print(w + " ");
                    }
                    //System.out.println("EOL");
                    int c = dictionary.lookupOrAddIfNotFound("EOL");
                    tc.add(c);
                //max_tokens++;
                }
                line = in.readLine();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        tc.add(dictionary.lookupOrAddIfNotFound("EOF"));
        return tc;
    }
}
