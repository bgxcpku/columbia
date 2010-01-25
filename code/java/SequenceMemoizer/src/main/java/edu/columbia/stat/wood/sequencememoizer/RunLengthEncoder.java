/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import java.util.ArrayList;

/**
 *
 * @author nicholasbartlett
 */
public class RunLengthEncoder {

    public RunLengthEncoder() {
    }

    ;

    //a section of a seq and spits back the number in the run in the appropriately
    //encoded way
    public int[] encode(int obsIndex, int[] seq) {
        ArrayList<Integer> encoding = new ArrayList<Integer>();
        int currentRunLength = Integer.MIN_VALUE;
        do {
            if (seq[obsIndex] == seq[obsIndex - 1]) {
                currentRunLength++;
                obsIndex++;
                if (currentRunLength >= Integer.MAX_VALUE) {
                    encoding.add(new Integer(currentRunLength));
                    currentRunLength = Integer.MIN_VALUE;
                }
            } else {
                encoding.add(new Integer(currentRunLength));
                break;
            }
            if (obsIndex == seq.length) {
                encoding.add(new Integer(currentRunLength));
            }
        } while (obsIndex < seq.length);

        int[] returnVal = new int[encoding.size()];
        int index = 0;
        for (Integer value : encoding) {
            returnVal[index++] = value.intValue();
        }
        return returnVal;
    }
}
