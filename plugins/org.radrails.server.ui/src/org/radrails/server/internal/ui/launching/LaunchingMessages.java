package org.radrails.server.internal.ui.launching;

import org.eclipse.osgi.util.NLS;

public class LaunchingMessages extends NLS {
	private static String BUNDLE_NAME = LaunchingMessages.class.getName();
	
	public static String RailsAppLaunch_noServerExistsDialog_title;
	public static String RailsAppLaunch_noServerExistsDialog_msg;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, LaunchingMessages.class);
	}
}
