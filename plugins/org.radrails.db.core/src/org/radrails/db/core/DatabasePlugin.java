/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.db.core;

import org.apache.derby.drda.NetworkServerControl;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author mbaumbach
 * @version 0.2.0
 */
public class DatabasePlugin extends Plugin
{

	// The shared instance.
	private static DatabasePlugin plugin;

	/**
	 * The constructor.
	 */
	public DatabasePlugin()
	{
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		new DerbyStarter().schedule();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception
	{
		try
		{
			DatabaseManager.getInstance().closeAll();
			DatabaseManager.getInstance().stopAll();

			try
			{
				NetworkServerControl server = new NetworkServerControl();
				server.shutdown();
			}
			catch (Exception e)
			{
				// ignore
			}
		}
		finally
		{
			super.stop(context);
		}
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static DatabasePlugin getDefault()
	{
		return plugin;
	}

	public static void startDerby()
	{
		DerbyStarter.start();
	}

} // DatabasePlugin
