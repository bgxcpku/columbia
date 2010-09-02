/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

import edu.columbia.stat.wood.sequencememoizer.BytePredictiveModel;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author nicholasbartlett
 */
public abstract class Decoder {

    public abstract int read() throws IOException;

    public abstract void set(BytePredictiveModel pm, InputStream is, boolean insert) throws IOException;
}
