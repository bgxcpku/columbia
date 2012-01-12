/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

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
public class PlumpStreamTest {

    public PlumpStreamTest() {
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
     * Test of available method, of class PlumpStream.
     */
    @Test
    public void testAvailable() throws Exception {
        System.out.println("available");
        PlumpStream instance = null;
        int expResult = 0;
        int result = instance.available();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mark method, of class PlumpStream.
     */
    @Test
    public void testMark() {
        System.out.println("mark");
        int readlimit = 0;
        PlumpStream instance = null;
        instance.mark(readlimit);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of markSupported method, of class PlumpStream.
     */
    @Test
    public void testMarkSupported() {
        System.out.println("markSupported");
        PlumpStream instance = null;
        boolean expResult = false;
        boolean result = instance.markSupported();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of close method, of class PlumpStream.
     */
    @Test
    public void testClose() throws Exception {
        System.out.println("close");
        PlumpStream instance = null;
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of read method, of class PlumpStream.
     */
    @Test
    public void testRead_byteArr() throws Exception {
        System.out.println("read");
        byte[] bs = null;
        PlumpStream instance = null;
        int expResult = 0;
        int result = instance.read(bs);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of read method, of class PlumpStream.
     */
    @Test
    public void testRead_3args() throws Exception {
        System.out.println("read");
        byte[] bs = null;
        int off = 0;
        int len = 0;
        PlumpStream instance = null;
        int expResult = 0;
        int result = instance.read(bs, off, len);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of read method, of class PlumpStream.
     */
    @Test
    public void testRead_0args() throws Exception {
        System.out.println("read");
        PlumpStream instance = null;
        int expResult = 0;
        int result = instance.read();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of skip method, of class PlumpStream.
     */
    @Test
    public void testSkip() {
        System.out.println("skip");
        long n = 0L;
        PlumpStream instance = null;
        long expResult = 0L;
        long result = instance.skip(n);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class PlumpStream.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        PlumpStream instance = null;
        instance.reset();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}