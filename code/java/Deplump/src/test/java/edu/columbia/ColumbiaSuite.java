/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia;

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
@Suite.SuiteClasses({edu.columbia.stat.StatSuite.class})
public class ColumbiaSuite {

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