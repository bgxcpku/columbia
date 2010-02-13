/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package name.pfau.david;

import edu.columbia.nlp.lm.client.Add;
import edu.columbia.nlp.lm.client.Client;
import edu.columbia.nlp.lm.client.Sample;
import java.util.ArrayList;

/**
 *
 * @author fwood
 */
public class LDADHPYPLMSampler extends Client {
    // add 4 observations
    // sample 6 times
    // disconnect
    public static void main( String[] args ) {
        String hostname = "localhost";

        int port = 4041;
        int domain = 1;
        int count = 1;

        int[][] observations = { {0, 1, 0}, {0, 1, 1}, {0, 2, 0}, {1, 1, 4}};

        Add addClient = new Add();
        Sample sampleClient = new Sample();

        for(int i = 0; i< observations.length; i++) {
            ArrayList<Integer> obs = new ArrayList<Integer>(observations[i].length);
            for(int j=0;j<observations[i].length;j++)
                obs.add(observations[i][j]);

            addClient.add(domain, count, obs);
            System.out.println("Anybody out there?");
        }

        sampleClient.sample(6);


    }
}
