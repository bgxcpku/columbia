/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.deplump;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author fwood
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({edu.columbia.stat.wood.deplump.DecoderTest.class,edu.columbia.stat.wood.deplump.PlumpTest.class,edu.columbia.stat.wood.deplump.DeplumpStreamTest.class,edu.columbia.stat.wood.deplump.DeplumpTest.class,edu.columbia.stat.wood.deplump.CommandLineOptionsTest.class,edu.columbia.stat.wood.deplump.v1.V1Suite.class,edu.columbia.stat.wood.deplump.MainTest.class,edu.columbia.stat.wood.deplump.PlumpStreamTest.class})
public class DeplumpSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}