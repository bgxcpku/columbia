/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

import edu.columbia.stat.wood.deplump.CommandLineOptions.ParseReturn;
import org.apache.commons.cli.Options;
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
public class CommandLineOptionsTest {

    public CommandLineOptionsTest() {
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
     * Test of getOptions method, of class CommandLineOptions.
     */
    @Test
    public void testGetOptions() {
        System.out.println("getOptions");
        Options expResult = null;
        Options result = CommandLineOptions.getOptions();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class CommandLineOptions.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        String[] args = null;
        ParseReturn expResult = null;
        ParseReturn result = CommandLineOptions.parse(args);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}