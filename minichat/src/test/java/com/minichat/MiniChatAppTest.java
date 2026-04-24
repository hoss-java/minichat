package com.minichat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class MiniChatAppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MiniChatAppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MiniChatAppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testMiniChatApp()
    {
        assertTrue( true );
    }
}
