package org.radrails.rails.core;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.radrails.rails.core");
		// $JUnit-BEGIN$
		suite.addTestSuite(InflectorTest.class);
		// $JUnit-END$
		return suite;
	}

}
