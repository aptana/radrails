/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.server.internal.ui;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.radrails.server.core.ServerManager;
import org.radrails.server.internal.ui.launching.LaunchCleaner;

/**
 * Plugin class for the server UI plugin.
 * 
 * @author mkent
 * 
 */
public class ServerUIPlugin extends AbstractUIPlugin {

	private static final String PLUGIN_ID = "org.radrails.server.ui";
	
	private static ServerUIPlugin instance;

	private LaunchCleaner fLaunchCleaner;

	/**
	 * Constructor.
	 */
	public ServerUIPlugin() {
		super();
		instance = this;
	}

	/**
	 * @return the singleton instance of this class
	 */
	public static ServerUIPlugin getInstance() {
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			ServerManager.getInstance().stopAll();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(fLaunchCleaner);
		} finally {
			super.stop(context);
		}
	}

	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		fLaunchCleaner = new LaunchCleaner();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fLaunchCleaner);
	}
	
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}

}
