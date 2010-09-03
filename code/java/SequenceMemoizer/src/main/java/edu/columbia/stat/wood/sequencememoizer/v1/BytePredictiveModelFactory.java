/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer.v1;

import edu.columbia.stat.wood.sequencememoizer.BytePredictiveModel;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author nicholasbartlett
 */
public class BytePredictiveModelFactory extends edu.columbia.stat.wood.sequencememoizer.BytePredictiveModelFactory {

    @Override
    public BytePredictiveModel get(int depth, long maxNumberRestaurants, long maxSequenceLength, URL url) {
        if (url == null) {
            if(depth <= -1){
                depth = 1023;
            }
            if(maxNumberRestaurants <= 0){
                maxNumberRestaurants = Long.MAX_VALUE;
            }
            if(maxSequenceLength <= 1024){
                maxSequenceLength = Long.MAX_VALUE;
            }
            return new ByteSequenceMemoizer(new ByteSequenceMemoizerParameters(depth, maxNumberRestaurants, maxSequenceLength));
        } else {
            try {
                ObjectInputStream ois = null;
                ByteSequenceMemoizer sm = null;
                ois = new ObjectInputStream(new BufferedInputStream(url.openStream()));
                sm = (ByteSequenceMemoizer) ois.readObject();
                sm.newSequence();

                if(depth > -1){
                    sm.setDepth(depth);
                }
                if(maxNumberRestaurants > 0){
                    sm.setMaxNumberRestaurants(maxNumberRestaurants);
                }
                if(maxSequenceLength > 1024){
                    sm.setMaxSequenceLength(maxSequenceLength);
                }

                return sm;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(BytePredictiveModelFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BytePredictiveModelFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
}
