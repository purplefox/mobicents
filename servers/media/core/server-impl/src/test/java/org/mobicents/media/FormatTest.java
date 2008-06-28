/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.format.AudioFormat;
import static org.junit.Assert.*;

/**
 *
 * @author Oleg Kulikov
 */
public class FormatTest {

    public FormatTest() {
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
     * Test of matches method, of class Format.
     */
    @Test
    public void testMatches() {
        System.out.println("matches");
        Format format1 = new AudioFormat("ALAW", 8000, 8,1);
        Format format2 = new AudioFormat("alaw", 8000, 8,1);
        
        boolean res = format1.matches(format2);
        assertEquals(res, true);
    }

}