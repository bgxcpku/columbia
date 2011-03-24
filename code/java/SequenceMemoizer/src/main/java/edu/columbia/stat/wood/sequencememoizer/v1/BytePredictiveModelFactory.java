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

/**
 * Implements the predictive byte model factory.
 * @author nicholasbartlett
 */
public class BytePredictiveModelFactory extends edu.columbia.stat.wood.sequencememoizer.BytePredictiveModelFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public BytePredictiveModel get(int depth, long maxNumberRestaurants, long maxSequenceLength) {
       
            if(depth <= -1){
                depth = 32;
            }
            if(maxNumberRestaurants <= 0){
                maxNumberRestaurants = 10000000;
            }
            if(maxSequenceLength <= 1024){
                maxSequenceLength = 100000000;
            }
            ByteSequenceMemoizerParameters smp = new ByteSequenceMemoizerParameters(depth, maxNumberRestaurants, maxSequenceLength);
            smp.maxCustomersInRestaurant = 8192;
            return new ByteSequenceMemoizer(smp);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public BytePredictiveModel get(URL url) {
            try {
                ObjectInputStream ois = null;
                BytePredictiveModel sm = null;
                ois = new ObjectInputStream(new BufferedInputStream(url.openStream()));
                sm = (BytePredictiveModel) ois.readObject();
                if(sm instanceof ByteSequenceMemoizer)
                    ((ByteSequenceMemoizer)sm).newSequence();
                return sm;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(BytePredictiveModelFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BytePredictiveModelFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        return null;
    }
}
