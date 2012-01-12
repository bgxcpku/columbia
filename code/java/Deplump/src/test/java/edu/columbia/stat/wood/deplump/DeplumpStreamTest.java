/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

import edu.columbia.stat.wood.sequencememoizer.BytePredictiveModel;
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
public class DeplumpStreamTest {

    public DeplumpStreamTest() {
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
     * Test of getModel method, of class DeplumpStream.
     */
    @Test
    public void testGetModel() {
        System.out.println("getModel");
        DeplumpStream instance = null;
        BytePredictiveModel expResult = null;
        BytePredictiveModel result = instance.getModel();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class DeplumpStream.
     */
    @Test
    public void testSetModel() {
        System.out.println("setModel");
        BytePredictiveModel model = null;
        DeplumpStream instance = null;
        instance.setModel(model);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of close method, of class DeplumpStream.
     */
    @Test
    public void testClose() throws Exception {
        System.out.println("close");
        DeplumpStream instance = null;
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of flush method, of class DeplumpStream.
     */
    @Test
    public void testFlush() throws Exception {
        System.out.println("flush");
        DeplumpStream instance = null;
        instance.flush();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of write method, of class DeplumpStream.
     */
    @Test
    public void testWrite_byteArr() throws Exception {
        System.out.println("write");
        byte[] byteSequence = null;
        DeplumpStream instance = null;
        instance.write(byteSequence);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of write method, of class DeplumpStream.
     */
    @Test
    public void testWrite_3args() throws Exception {
        System.out.println("write");
        byte[] byteSequence = null;
        int off = 0;
        int len = 0;
        DeplumpStream instance = null;
        instance.write(byteSequence, off, len);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of write method, of class DeplumpStream.
     */
    @Test
    public void testWrite_int() throws Exception {
        System.out.println("write");
        int i = 0;
        DeplumpStream instance = null;
        instance.write(i);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}