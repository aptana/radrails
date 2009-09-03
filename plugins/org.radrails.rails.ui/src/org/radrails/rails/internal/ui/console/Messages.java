package org.radrails.rails.internal.ui.console;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{

	private static final String BUNDLE_NAME = Messages.class.getName();

	public static String RailsShell_HelpText;
	public static String RailsShell_ProjectNeedsToBeSelected;

	static
	{
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
