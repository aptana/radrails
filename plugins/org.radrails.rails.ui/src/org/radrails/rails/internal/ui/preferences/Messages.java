package org.radrails.rails.internal.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	
	private static final String BUNDLE_NAME = Messages.class.getName();
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	public static String RailsPreferencePage_DetectedPathLabel;
	public static String RailsPreferencePage_InterpretersLinkText;
	public static String RailsPreferencePage_Description;
	public static String RailsPreferencePage_HeaderText;
	public static String RailsPreferencePage_MongrelRailsPathLabel;
	public static String RailsPreferencePage_RailsPathLabel;
	
}
