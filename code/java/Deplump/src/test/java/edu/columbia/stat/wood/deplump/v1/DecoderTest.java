/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump.v1;

import edu.columbia.stat.wood.sequencememoizer.BytePredictiveModel;
import java.io.InputStream;
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
public class DecoderTest {

    public DecoderTest() {
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
     * Test of set method, of class Decoder.
     */
    @Test
    public void testSet() throws Exception {
        System.out.println("set");
        BytePredictiveModel pm = null;
        InputStream is = null;
        boolean insert = false;
        Decoder instance = new Decoder();
        instance.set(pm, is, insert);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of read method, of class Decoder.
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");
        Decoder instance = new Decoder();
        int expResult = 0;
        int result = instance.read();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}