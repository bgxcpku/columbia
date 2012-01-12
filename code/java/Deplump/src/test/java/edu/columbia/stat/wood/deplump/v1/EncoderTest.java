/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump.v1;

import edu.columbia.stat.wood.sequencememoizer.BytePredictiveModel;
import java.io.OutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fwood
 */
public class EncoderTest {

    public EncoderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of set method, of class Encoder.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        BytePredictiveModel pm = null;
        OutputStream out = null;
        boolean insert = false;
        Encoder instance = new Encoder();
        instance.set(pm, out, insert);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encode method, of class Encoder.
     */
    @Test
    public void testEncode() throws Exception {
        System.out.println("encode");
        byte observation = 0;
        Encoder instance = new Encoder();
        instance.encode(observation);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of close method, of class Encoder.
     */
    @Test
    public void testClose() throws Exception {
        System.out.println("close");
        Encoder instance = new Encoder();
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}