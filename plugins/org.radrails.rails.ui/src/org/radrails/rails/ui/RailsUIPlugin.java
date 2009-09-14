/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.radrails.rails.core.IRailsConstants;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.RailsProjectListener;
import org.radrails.rails.internal.ui.RailsShellOpenJob;
import org.radrails.rails.internal.ui.autotest.AutotestManager;
import org.radrails.rails.internal.ui.tail.TailConsoleManager;
import org.rubypeople.rdt.core.ILoadpathEntry;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.ISourceFolder;
import org.rubypeople.rdt.core.ISourceFolderRoot;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.ui.RubyExplorerTracker;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.ui.PreferenceConstants;

import com.aptana.ide.core.db.EventInfo;
import com.aptana.ide.core.db.EventLogger;
import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.IGemManager;

/**
 * Plugin class for the rails.ui plugin.
 * 
 * @author mkent
 */
public class RailsUIPlugin extends AbstractUIPlugin
{

	/**
	 * Whether or not to show jobs in progress view/bar. If false, jobs will be set to "System" so they won't get shown.
	 */
	private static final boolean DISPLAY_JOBS = true;

	private static RailsUIPlugin instance;

	private static final String PLUGIN_ID = "org.radrails.rails.ui";

	private static Hashtable<String, Image> images = new Hashtable<String, Image>();

	private AutotestManager fAutotestManager;
	private RailsProjectListener fRailsListener;
	// TODO When/if the RailsDBConnector is working properly then uncomment this.
	// private RailsDBConnector connector;
	private ServiceTracker gemManagerTracker;

	/**
	 * Default constructor.
	 */
	public RailsUIPlugin()
	{
		super();
		instance = this;
	}

	/**
	 * getImage
	 * 
	 * @param path
	 * @return Image
	 */
	public static Image getImage(String path)
	{
		if (images.get(path) == null)
		{
			ImageDescriptor id = getImageDescriptor(path);
			if (id == null)
			{
				return null;
			}
			Image i = id.createImage();
			images.put(path, i);
			return i;
		}
		else
		{
			return (Image) images.get(path);
		}
	}

	/**
	 * Returns the singleton instance of the plugin.
	 * 
	 * @return - the plugin
	 */
	public static RailsUIPlugin getInstance()
	{
		return instance;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path)
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception
	{
		super.start(context);
		gemManagerTracker = new ServiceTracker(context, IGemManager.class.getName(), null);
		gemManagerTracker.open();

		// TODO Only register AutoTestManager as resource listener if it's being used
		fAutotestManager = new AutotestManager();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fAutotestManager, IResourceChangeEvent.POST_CHANGE);
		getPreferenceStore().addPropertyChangeListener(fAutotestManager);

		// Listen for imported projects and if they look like rails projects, add the rails nature
		fRailsListener = new RailsProjectListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fRailsListener,
				IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE);

//		Simplification of RadRails perspective. Do not show the Rails Shell initially.
//		Job job = new RailsShellOpenJob(context);
//		job.setSystem(!DISPLAY_JOBS);
//		job.schedule(1000);

		Job job = new Job("Start DB Core plugin")
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				// Force the DB plugin to load so that derby will start up
				try
				{
					Platform.getBundle("org.radrails.db.core").loadClass("org.radrails.db.core.DatabasePlugin"); // force
					// DB
					// plugin
					// to
					// load
				}
				catch (Exception e)
				{
					// ignore
				}
				return Status.OK_STATUS;
			}

		};
		job.setSystem(!DISPLAY_JOBS);
		job.schedule(1000);
		// TODO When/if the RailsDBConnector is working properly then uncomment this.
		// connector = new RailsDBConnector();

		job = new Job("Record number of rails projects")
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				int count = RailsPlugin.getRailsProjects().size();
				EventLogger.getInstance().logEvent("rails.project.count", Integer.toString(count));
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		try
		{
			gemManagerTracker.close();

			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fAutotestManager);
			getPreferenceStore().removePropertyChangeListener(fAutotestManager);

			// save pref store - for autotest
			IPersistentPreferenceStore ppStore = (IPersistentPreferenceStore) getPreferenceStore();
			ppStore.save();

			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fRailsListener);

			TailConsoleManager.getInstance().stopAll();
			// TODO When/if the RailsDBConnector is working properly then uncomment this.
			// connector.stop();
		}
		finally
		{
			super.stop(context);
		}
	}

	/**
	 * getGemManager
	 * 
	 * @return - gem manager
	 */
	public IGemManager getGemManager()
	{
		return (IGemManager) gemManagerTracker.getService();
	}

	/**
	 * getAutotestManager
	 * 
	 * @return - autotest manager
	 */
	public AutotestManager getAutotestManager()
	{
		return fAutotestManager;
	}

	/**
	 * Get plugin identifier
	 * 
	 * @return - plugin id
	 */
	public static String getPluginIdentifier()
	{
		return PLUGIN_ID;
	}

	/**
	 * getFileContents
	 * 
	 * @param path
	 * @return - string file contents
	 */
	public String getFileContents(String path)
	{
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(FileLocator
					.openStream(getBundle(), new Path(path), false)));
			StringBuffer buffer = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				buffer.append(line);
				buffer.append("\n");
			}
			return buffer.toString();
		}
		catch (IOException e)
		{
			RailsUILog.log(e);
		}
		finally
		{
			try
			{
				if (reader != null)
					reader.close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}
		return "";
	}

	/**
	 * Gets selected rails project
	 * 
	 * @return - selected project
	 */
	public IProject getSelectedRailsProject()
	{
		return getProjectTracker().getSelectedByNatureID(IRailsConstants.RAILS_PROJECT_NATURE);
	}

	private RubyExplorerTracker getProjectTracker()
	{
		return RubyPlugin.getDefault().getProjectTracker();
	}

	/**
	 * Gets selected rails or ruby project
	 * 
	 * @return - rails or ruby selected project
	 */
	public IProject getSelectedRailsOrRubyProject()
	{
		IProject retVal = getSelectedRailsProject();
		if (retVal != null)
			return retVal;
		return getProjectTracker().getSelectedRubyProject();
	}

	/**
	 * getSelectedOrOnlyRailsProject
	 * 
	 * @return - project
	 */
	public static IProject getSelectedOrOnlyRailsProject()
	{
		IProject project = RailsUIPlugin.getInstance().getSelectedRailsOrRubyProject();
		if (project != null)
			return project;
		Set<IProject> projects = RailsPlugin.getRailsProjects();
		if (projects != null && projects.size() == 1)
		{
			return projects.iterator().next();
		}
		return null;
	}

	/**
	 * Adds the default rails loadpaths
	 * 
	 * @param rubyProject
	 * @param monitor
	 * @throws RubyModelException
	 */
	public static void addDefaultRailsLoadpaths(IRubyProject rubyProject, IProgressMonitor monitor)
			throws RubyModelException
	{
		List<ILoadpathEntry> list = new ArrayList<ILoadpathEntry>();
		IProject project = rubyProject.getProject();
		list.add(RubyCore.newSourceEntry(project.getFullPath())); // Entry for project as src folder root
		ILoadpathEntry[] jreEntries = PreferenceConstants.getDefaultRubyVMLibrary(); // Default Ruby libraries
		for (int i = 0; i < jreEntries.length; i++)
		{
			list.add(jreEntries[i]);
		}
		rubyProject.setRawLoadpath(list.toArray(new ILoadpathEntry[list.size()]), monitor);

		// ROR-292 Grab the version of rails that is stated in config/environment.rb! And then figure out what versions
		// of the other gems that needs!
		String version = RailsPlugin.getRailsVersion(project);
		if (version != null)
		{
			Gem railsGem = new Gem("rails", version, "n/a");// tack the version onto the end of rails, but then we need
			// to check dependencies....
			AptanaRDTPlugin.addGemLoadPath(rubyProject, railsGem, monitor);
		}
		else
		{
			AptanaRDTPlugin.log("Unable to get the version number for rails for project: "
					+ rubyProject.getElementName());
		}
	}

	/**
	 * Has rails frozen in vendor
	 * 
	 * @param rubyProject
	 * @return - true if has rails frozen in vendor
	 */
	public static boolean hasRailsFrozenInVendor(IRubyProject rubyProject)
	{
		ISourceFolderRoot root = rubyProject.getSourceFolderRoot(rubyProject.getProject());
		ISourceFolder folder = root.getSourceFolder(new String[] { "vendor", "rails" });
		if (folder == null)
			return false;
		return folder.exists();
	}

	public static void overrideDocumentRoot(IProject project) throws CoreException
	{
		project.setPersistentProperty(
				new QualifiedName("", "com.aptana.ide.editor.html.preview.HTML_PREVIEW_OVERRIDE"), "true"); // HTMLPreviewConstants.HTML_PREVIEW_OVERRIDE
		project.setPersistentProperty(new QualifiedName("", "com.aptana.ide.editor.html.preview.CONTEXT_ROOT"),
				"/public"); // HTMLPreviewConstants.CONTEXT_ROOT
	}

}
