package org.radrails.server.internal.ui;

import org.eclipse.osgi.util.NLS;

public class ServerMessages extends NLS {
	
	private static final String BUNDLE_NAME = ServerMessages.class.getName();
	
	public static String RemoveServerActionDelegate_msg;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, ServerMessages.class);
	}

}
