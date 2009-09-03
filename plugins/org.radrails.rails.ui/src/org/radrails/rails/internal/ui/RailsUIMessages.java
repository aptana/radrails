package org.radrails.rails.internal.ui;

import org.eclipse.osgi.util.NLS;

public class RailsUIMessages extends NLS {
	
	private static final String BUNDLE_NAME = RailsUIMessages.class.getName();
	
	public static String SelectRailsProject_message;	
	public static String OpenRubyEditor_message;	
	public static String SpecifyRakePath_message;

	public static String Browser_goButton_tooltip;
	public static String Browser_refreshButton_tooltip;
	public static String Browser_forwardButton_tooltip;
	public static String Browser_backButton_tooltip;	
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, RailsUIMessages.class);
	}

}
