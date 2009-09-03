/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.server.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.debug.core.DebugPlugin;
import org.osgi.framework.BundleContext;
import org.radrails.server.core.launching.ServerLaunchListener;

/**
 * Plugin class for the server.core plugin.
 * 
 * @author mkent
 * 
 */
public class ServerPlugin extends Plugin {

	public static final String PLUGIN_ID = "org.radrails.server.core";
	
	private static ServerPlugin instance;
	private ServerLaunchListener fLaunchListener;

	/**
	 * Default constructor.
	 */
	public ServerPlugin() {
		super();
		instance = this;
	}

	/**
	 * @return the singleton instance of <code>ServerPlugin</code>
	 */
	public static ServerPlugin getInstance() {
		return instance;
	}

	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		fLaunchListener = new ServerLaunchListener();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(fLaunchListener);
		
		// Restore the state of the ServerManager
		ServerManager.getInstance().loadServers();
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(fLaunchListener);
			ServerManager.getInstance().stopAll();
		} finally {
			super.stop(context);
		}
	}
}
