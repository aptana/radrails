package org.radrails.rails.internal.ui.wizards;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.radrails.rails.internal.ui.wizards.pages.WizardNewRailsProjectPage;

public class RailsProjectCreatorTest extends TestCase
{

	public void testOldRailsDoesntUseDSwitch()
	{
		WizardNewRailsProjectPage page = new WizardNewRailsProjectPage("pageName")
		{
			@Override
			public String getRailsVersion()
			{
				return "1.0.0";
			}

			@Override
			public IProject getProjectHandle()
			{
				return null;
			}

			@Override
			public IPath getLocationPath()
			{
				return null;
			}
		};

		RailsProjectCreator creator = new RailsProjectCreator(page);
		assertEquals("_1.0.0_ appname", creator.getArgs("appname"));
	}
}
