package org.radrails.rails.ui;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.radrails.rails.internal.ui.RailsLightweightDecoratorTest;
import org.radrails.rails.internal.ui.generators.GeneratorsConsoleLineTest;
import org.radrails.rails.internal.ui.wizards.RailsProjectCreatorTest;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.radrails.rails.ui");
		// $JUnit-BEGIN$
		suite.addTestSuite(RailsLightweightDecoratorTest.class);
		suite.addTestSuite(GeneratorsConsoleLineTest.class);
		suite.addTestSuite(RailsProjectCreatorTest.class);
		// $JUnit-END$
		return suite;
	}

}
