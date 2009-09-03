/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.rails.core.railsplugins;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.radrails.rails.core.RailsLog;
import org.radrails.rails.internal.core.RailsPlugin;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.RubyRuntime;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Singleton class that manages access to a list of available Rails plugins. The list is cached in the rails.ui plug-in
 * state location, and can be refreshed on-demand from the Rails Plugins web service.
 * 
 * @author mkent
 */
public class RailsPluginsManager
{

	public class RailsPluginException extends Exception
	{

		private static final long serialVersionUID = -7961625830842504591L;

		public RailsPluginException(Exception e)
		{
			super(e);
		}

	}

	private static final int DAY = 1000 * 60 * 60 * 24;
	private long lastUpdated = -1;

	private static final String SERVICE_URL = "http://agilewebdevelopment.com/plugins/top_rated.xml";
	private static final String PLUGINS_FILE = "rails_plugins.xml";
	private static RailsPluginsManager fInstance;

	private List<RailsPluginDescriptor> fPlugins;
	private static Set<IRailsPluginListener> listeners = new HashSet<IRailsPluginListener>();

	private RailsPluginsManager()
	{
	}

	public static RailsPluginsManager getInstance()
	{
		if (fInstance == null)
		{
			fInstance = new RailsPluginsManager();
		}
		return fInstance;
	}

	public static void addRailsPluginListener(IRailsPluginListener listener)
	{
		listeners.add(listener);
	}

	public static void removeRailsPluginListener(IRailsPluginListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Gets a list of currently available Rails plugins. The list contains Maps, each of which holds information about a
	 * plugin in String to String mappings.
	 * 
	 * @return a list of plugins
	 * @throws Exception
	 */
	public List<RailsPluginDescriptor> getPlugins() throws RailsPluginException
	{
		if (fPlugins == null)
		{
			// Try to load from state file
			fPlugins = loadPlugins();

			// No results, try to update from web service sync
			if (fPlugins.isEmpty())
			{
				fPlugins = updatePlugins(new NullProgressMonitor());
			}
			else if (haventUpdatedInADay()) // current results are stale, async update
			{
				scheduleLoadOfRemotePluginListing();
			}
		}
		return fPlugins;
	}

	private boolean haventUpdatedInADay()
	{
		return lastUpdated < (System.currentTimeMillis() - DAY);
	}

	private void scheduleLoadOfRemotePluginListing()
	{
		Job job = new Job("Grabbing Rails Plugin listing from remote source")
		{

			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					updatePlugins(monitor);
				}
				catch (Exception e)
				{
					RailsLog.log(e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}

		};
		job.setSystem(true);
		job.schedule(10000); // schedules a 10-second delay
	}

	/**
	 * Updates the current list of Rails plugins from the web service.
	 * 
	 * @throws RailsPluginException
	 * @throws Exception
	 *             if an error occurs
	 */
	public List<RailsPluginDescriptor> updatePlugins(IProgressMonitor monitor) throws RailsPluginException
	{

		monitor.beginTask("Updating plugin list", 3);

		// Get latest list from web service
		monitor.subTask("Accessing plugin directory");
		HttpURLConnection conn;
		try
		{
			conn = getConnection();
		}
		catch (MalformedURLException e1)
		{
			throw new RailsPluginException(e1);
		}
		catch (ProtocolException e1)
		{
			throw new RailsPluginException(e1);
		}
		catch (IOException e1)
		{
			throw new RailsPluginException(e1);
		}
		monitor.worked(1);

		monitor.subTask("Downloading plugin list");
		String pxml = null;
		try
		{
			pxml = getPluginXMLFeed(monitor, conn);
		}
		catch (IOException e)
		{
			throw new RailsPluginException(e);
		}
		monitor.worked(1);

		monitor.subTask("Processing plugin information");
		// Update the object list from the XML data
		List<RailsPluginDescriptor> plugins = new ArrayList<RailsPluginDescriptor>();
		StringReader strIn = new StringReader(pxml);
		try
		{
			plugins = parsePluginsXML(strIn);
		}
		catch (SAXException e)
		{
			throw new RailsPluginException(e);
		}
		catch (ParserConfigurationException e)
		{
			throw new RailsPluginException(e);
		}
		catch (IOException e)
		{
			throw new RailsPluginException(e);
		}

		// Cache XML data to file
		writeOutPluginsXML(pxml);
		monitor.worked(1);
		lastUpdated = System.currentTimeMillis();

		for (IRailsPluginListener listener : listeners)
		{
			listener.remotePluginsRefreshed();
		}
		monitor.done();

		return plugins;
	}

	private HttpURLConnection getConnection() throws MalformedURLException, IOException, ProtocolException
	{
		URL url = new URL(SERVICE_URL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		return conn;
	}

	private String getPluginXMLFeed(IProgressMonitor monitor, HttpURLConnection conn) throws IOException
	{
		BufferedReader bufIn = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuffer pxml = new StringBuffer();
		String l = null;
		while ((l = bufIn.readLine()) != null)
		{
			pxml.append(l);
		}
		return pxml.toString();
	}

	private void writeOutPluginsXML(String pxml)
	{
		PrintWriter out = null;
		try
		{
			File f = new File(getLocalPluginXMLCache().toOSString());
			out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
			out.write(pxml);
			out.flush();
		}
		catch (IllegalStateException e)
		{
			RailsLog.log(e);
		}
		catch (IOException e)
		{
			RailsLog.log(e);
		}
		finally
		{
			if (out != null)
				out.close();
		}
	}

	/**
	 * Loads the list of plugins from the state file.
	 */
	private List<RailsPluginDescriptor> loadPlugins()
	{
		File f = new File(getLocalPluginXMLCache().toOSString());
		if (f.exists())
		{
			try
			{
				FileReader fis = new FileReader(f);
				return parsePluginsXML(fis);
			}
			catch (SAXException e)
			{
				RailsLog.logError("Error parsing Rails plugins XML", e);
			}
			catch (ParserConfigurationException e)
			{
				RailsLog.logError("Error parsing Rails plugins XML: parser misconfigured.", e);
			}
			catch (IOException e)
			{
				RailsLog.logError("Error parsing Rails plugins XML: I/O problems.", e);
			}
		}
		return Collections.emptyList();
	}

	private IPath getLocalPluginXMLCache()
	{
		return RailsPlugin.getInstance().getStateLocation().append(PLUGINS_FILE);
	}

	/**
	 * Parses an XML file describing a list of Rails plugins and returns a List of Map objects, containing the plugin
	 * attributes mapped to their corresponding values.
	 * 
	 * @param rdr
	 *            the XML source
	 * @return a list of plugin Maps
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private List<RailsPluginDescriptor> parsePluginsXML(Reader rdr) throws SAXException, ParserConfigurationException,
			IOException
	{
		XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		RailsPluginsContentHandler handler = new RailsPluginsContentHandler();
		reader.setContentHandler(handler);
		reader.parse(new InputSource(rdr));
		return handler.getRailsPlugins();
	}

	/**
	 * Install a Rails plugin from an SVN repository. Information about the plugin is provided in a
	 * RailsPluginDescriptor.
	 * 
	 * @param project
	 *            the project to install the plugin into
	 * @param plugin
	 *            the plugin to install
	 * @param externals
	 *            true if an svn:externals entry is to be added, false otherwise
	 */
	public static void installPlugin(IProject project, RailsPluginDescriptor plugin, boolean externals, boolean checkout)
	{
		// Prepare plugin info
		String repos = plugin.getRepository();
		if (repos == null || repos.trim().length() == 0)
		{
			RailsLog.log("Plugin has no repository: " + plugin.toString());
			repos = plugin.getName();
		}
		// Prepare the command
		String command = "install ";
		if (externals || checkout)
		{
			String ext_str = externals ? "x" : "";
			String check_str = checkout ? "o" : "";
			command += "-" + ext_str + check_str + " " + repos;
		}
		else
		{
			command += repos;
		}

		run(project, command);

		for (IRailsPluginListener listener : listeners)
		{
			listener.pluginInstalled(project, plugin);
		}
	}

	public static ILaunch run(IProject project, String args)
	{
		try
		{
			ILaunchConfigurationWorkingCopy wc = makeWorkingCopy(getPluginScript(), args, project);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_USE_TERMINAL, "org.radrails.rails.shell");
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_TERMINAL_COMMAND, "script/plugin " + args);
			ILaunchConfiguration config = wc.doSave();
			return config.launch(ILaunchManager.RUN_MODE, null);
		}
		catch (CoreException e)
		{
			RailsLog.logError("Error running rake task", e);
		}
		return null;
	}

	private static String getPluginScript()
	{
		return new Path("script").append("plugin").toPortableString();
	}

	/**
	 * Removes a plugin from the given project.
	 * 
	 * @param project
	 *            the project to remove the plugin from
	 * @param plugin
	 *            the plugin to remove
	 */
	public static void removePlugin(IProject project, RailsPluginDescriptor plugin)
	{
		String name = (String) plugin.getProperty(RailsPluginDescriptor.NAME);
		if (name.indexOf(' ') > -1)
		{
			System.out.println("WTF!");
		}
		run(project, "remove " + name + "");
		for (IRailsPluginListener listener : listeners)
		{
			listener.pluginRemoved(project, plugin);
		}
	}

	private static ILaunchConfigurationWorkingCopy makeWorkingCopy(String file, String args, IProject project)
			throws CoreException
	{
		String workingDirectory = "";
		IPath path = RailsPlugin.findRailsRoot(project);
		if (path.toPortableString().length() != 0)
		{
			workingDirectory = project.getFolder(path).getLocation().toOSString();
		}
		else
		{
			workingDirectory = project.getLocation().toOSString();
		}
		return RubyRuntime.createBasicLaunch(file, args, project, workingDirectory);
	}

	public static List<RailsPluginDescriptor> getInstalledPlugins(IProject project)
	{
		List<RailsPluginDescriptor> plugins = new ArrayList<RailsPluginDescriptor>();
		if (project == null)
			return plugins;
		IPath railsRoot = RailsPlugin.findRailsRoot(project);
		IPath pluginsDir = railsRoot.append("vendor").append("plugins");
		IFolder pluginsFolder = project.getFolder(pluginsDir);
		try
		{
			IResource[] members = pluginsFolder.members();
			for (int i = 0; i < members.length; i++)
			{
				IResource member = members[i];
				if (member.getType() != IResource.FOLDER)
					continue;
				RailsPluginDescriptor desc = new RailsPluginDescriptor();
				desc.setProperty(RailsPluginDescriptor.NAME, member.getName());
				plugins.add(desc);
			}
		}
		catch (CoreException e)
		{
			RailsLog.log(e);
		}
		return plugins;
	}

	public static boolean pluginInstalled(RailsPluginDescriptor plugin, IProject project)
	{
		List<RailsPluginDescriptor> plugins = getInstalledPlugins(project);
		for (RailsPluginDescriptor descriptor : plugins)
		{
			if (normalize(descriptor.getName()).equalsIgnoreCase(plugin.getName()))
				return true;
		}
		return false;
	}

	private static String normalize(String string)
	{
		return string.replace('_', ' ');
	}
}
