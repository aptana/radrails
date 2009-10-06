package com.aptana.radrails.cloud;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.aptana.radrails.cloud.shell.AptanaCloudCommandProviderTest;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for com.aptana.radrails.cloud");
		// $JUnit-BEGIN$
		suite.addTestSuite(AptanaCloudCommandProviderTest.class);
		// $JUnit-END$
		return suite;
	}

}
