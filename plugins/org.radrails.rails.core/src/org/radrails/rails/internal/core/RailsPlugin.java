/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.radrails.rails.core.IRailsConstants;
import org.radrails.rails.core.RailsLog;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.util.Util;
import org.rubypeople.rdt.internal.core.RubyModelManager.EclipsePreferencesListener;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.IGemManager;

/**
 * Plugin class for the rails core plugin.
 * 
 * @author mkent
 * @version 0.3.1
 */
public class RailsPlugin extends Plugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "org.radrails.rails.core";

	// Preferences
	public HashSet<String> optionNames = new HashSet<String>(20);
	public Hashtable<String, String> optionsCache;

	public final IEclipsePreferences[] preferencesLookup = new IEclipsePreferences[2];

	static final int PREF_INSTANCE = 0;
	static final int PREF_DEFAULT = 1;

	private static final String WACKY_DEBIAN_RAILS_PATH = "/usr/share/rails/railties/bin/rails";
	private static final String RAILS = "rails";

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions()
	 * @since 0.9.0
	 */
	public static final String RAILS_DEPRECATION_INSTANCE_VARIABLES = PLUGIN_ID
			+ ".compiler.problem.railsInstanceVariable";//$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions()
	 * @since 0.9.0
	 */
	public static final String RAILS_DEPRECATION_RENDER_CALLS = PLUGIN_ID
			+ ".compiler.problem.railsDeprecationRenderCalls";//$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions()
	 * @since 0.9.0
	 */
	public static final String RAILS_DEPRECATION_REDIRECT_CALLS = PLUGIN_ID
			+ ".compiler.problem.railsDeprecationRedirectCalls";//$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions()
	 * @since 0.9.0
	 */
	public static final String RAILS_DEPRECATION_POST_FORMAT = PLUGIN_ID
			+ ".compiler.problem.railsDeprecationPostFormat";//$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions()
	 * @since 0.9.0
	 */
	public static final String RAILS_DEPRECATION_START_END_FORM_TAG = PLUGIN_ID
			+ ".compiler.problem.railsDeprecationStartEndFormTag";//$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions()
	 * @since 0.9.0
	 */
	public static final String RAILS_DEPRECATION_UPDATE_ELEMENT_FUNCTION = PLUGIN_ID
			+ ".compiler.problem.railsDeprecationUpdateElementFunction";//$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions()
	 * @since 0.9.0
	 */
	public static final String RAILS_DEPRECATION_IMAGE_LINK_METHODS = PLUGIN_ID
			+ ".compiler.problem.railsDeprecationImageLinkMethods";//$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions()
	 * @since 0.9.0
	 */
	public static final String RAILS_DEPRECATION_HUMAN_SIZE_HELPER_ALIAS = PLUGIN_ID
			+ ".compiler.problem.railsDeprecationHumanSizeHelperAlias";//$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions()
	 * @since 0.9.0
	 */
	public static final String RAILS_DEPRECATION_ACTIVE_RECORD_FIND_METHODS = PLUGIN_ID
			+ ".compiler.problem.railsDeprecationActiveRecordFindMethods";//$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions()
	 * @since 0.9.0
	 */
	public static final String RAILS_DEPRECATION_PUSH_WITH_ATTRIBUTES = PLUGIN_ID
			+ ".compiler.problem.railsDeprecationPushWithAttributes";//$NON-NLS-1$

	private static RailsPlugin instance;

	/**
	 * Maps to hold cached values for determining the version of rails that a project is tied to.
	 */
	private static Map<IProject, String> fgRailsVersions = new HashMap<IProject, String>(); // project to version as a
	// string
	private static Map<IProject, Long> fgFileModifications = new HashMap<IProject, Long>(); // project to timestamp of
	// modification to
	// config/environment.rb

	private ServiceTracker gemManagerTracker;

	/**
	 * Constructor.
	 */
	public RailsPlugin()
	{
		super();
		instance = this;
	}

	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		gemManagerTracker = new ServiceTracker(context, IGemManager.class.getName(), null);
		gemManagerTracker.open();

		initializePreferences();
	}

	public static boolean hasRailsNature(IProject project)
	{
		if (project == null)
			return false;
		try
		{
			return project.hasNature(IRailsConstants.RAILS_PROJECT_NATURE);
		}
		catch (CoreException e)
		{
			RailsLog.log(e);
		}
		return false;
	}

	public static void addRailsNature(IProject project, IProgressMonitor monitor) throws CoreException
	{
		RubyCore.addRubyNature(project, monitor); // first make sure we're a ruby project
		if (!project.hasNature(IRailsConstants.RAILS_PROJECT_NATURE))
		{
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = IRailsConstants.RAILS_PROJECT_NATURE;
			description.setNatureIds(newNatures);
			project.setDescription(description, monitor);
		}
	}

	/**
	 * Return the project relative path of the rails root folder. Typically this is the project root itself, so the path
	 * is blank. But it may in some cases be a subdirectory.
	 * 
	 * @param project
	 * @return
	 */
	public static IPath findRailsRoot(IProject project)
	{
		// TODO Cache the rails root!?
		return RailsRootFinder.findRailsRoot(project);
	}

	private static class RailsRootFinder implements IResourceVisitor
	{
		private IPath path;

		public static IPath findRailsRoot(IProject project)
		{
			if (looksLikeRailsRoot(project))
			{ // Always prefer project root
				return project.getProjectRelativePath();
			}
			try
			{
				RailsRootFinder finder = new RailsRootFinder();
				project.accept(finder);
				if (finder.getRailsRoot() != null)
				{
					return finder.getRailsRoot();
				}
			}
			catch (CoreException e)
			{
				// ignore
			}
			return project.getProjectRelativePath();
		}

		public boolean visit(IResource resource) throws CoreException
		{
			if (resource.getType() == IResource.FILE)
				return false;
			if (resource.getType() == IResource.FOLDER)
			{
				IFolder folder = (IFolder) resource;
				if (looksLikeRailsRoot(folder))
				{
					path = folder.getProjectRelativePath();
					return false;
				}
			}
			return true;
		}

		public IPath getRailsRoot()
		{
			return path;
		}

	}

	public static boolean looksLikeRailsRoot(IContainer folder)
	{
		if (folder == null)
			return false;
		return folderExists(folder, "app") && folderExists(folder, "script") && folderExists(folder, "config")
				&& folderExists(folder, "public") && fileExists(folder, "config/boot.rb")
				&& fileExists(folder, "config/environment.rb") && fileExists(folder, "script/generate");
	}

	private static boolean folderExists(IContainer rootFolder, String name)
	{
		IFolder folder = rootFolder.getFolder(new Path(name));
		return folder != null && folder.exists();
	}

	private static boolean fileExists(IContainer rootFolder, String name)
	{
		IFile file = rootFolder.getFile(new Path(name));
		return file != null && file.exists();
	}

	/**
	 * If possible, determine the rails version this project is pegged to.
	 * 
	 * @param project
	 * @return
	 */
	public static String getRailsVersion(IProject project)
	{
		if (project == null)
			return null;
		IPath railsRoot = RailsPlugin.findRailsRoot(project);
		if (railsRoot == null || railsRoot.segmentCount() == 0)
		{
			railsRoot = project.getLocation();
		}
		else
		{
			railsRoot = project.getLocation().append(railsRoot);
		}
		if (railsRoot == null)
			return null;
		File file = railsRoot.append("config").append("environment.rb").toFile();
		if (file == null || !file.exists() || !file.isFile())
			return null;
		Long lastModification = fgFileModifications.get(project);
		if (lastModification == null)
			lastModification = Long.MIN_VALUE;
		if (file.lastModified() > lastModification.longValue())
		{
			String version = getRailsVersion(file);
			fgRailsVersions.put(project, version);
			fgFileModifications.put(project, file.lastModified());
		}
		return fgRailsVersions.get(project);
	}

	private static String getRailsVersion(File file)
	{
		try
		{
			String content = new String(Util.getFileCharContent(file, null));
			int index = content.indexOf("RAILS_GEM_VERSION = ");
			if (index == -1)
				return null;
			content = content.substring(index + 20);
			content = content.trim();
			if (content.startsWith("'") || content.startsWith("\""))
			{
				content = content.substring(1);
			}
			int end = content.indexOf("'");
			if (end == -1)
			{
				end = content.indexOf("\"");
			}
			if (end == -1)
				return null;
			return content.substring(0, end);
		}
		catch (IOException e)
		{
			RailsLog.log(e);
			return null;
		}
	}

	public void stop(BundleContext context) throws Exception
	{
		try
		{
			gemManagerTracker.close();
			savePluginPreferences();
		}
		finally
		{
			super.stop(context);
		}
	}

	/**
	 * @return the singleton instance of this class
	 */
	public static RailsPlugin getInstance()
	{
		return instance;
	}

	/**
	 * Writes a script from plugin jar to plugin metadata folder within the workspace.
	 * 
	 * @param rubyFile
	 *            - The file to place on the filesystem
	 * @return Absolute path to specified script file
	 */
	public String getRubyScriptPath(String rubyFile)
	{
		String directoryFile = getStateLocation().toOSString() + File.separator + rubyFile;
		File pluginDirFile = new File(directoryFile);

		if (!pluginDirFile.exists())
		{
			try
			{
				pluginDirFile.createNewFile();
				URL u = getBundle().getEntry("/ruby/" + rubyFile);
				BufferedReader input = new BufferedReader(new InputStreamReader(u.openStream()));
				FileWriter output = new FileWriter(pluginDirFile);
				String line;
				while ((line = input.readLine()) != null)
				{
					output.write(line);
					output.write('\n');
				}
				output.flush();
				output.close();
				input.close();
			}
			catch (IOException e)
			{
				RailsLog.logError("Error writing plugin script to metadata", e);
			}
		}

		String path = "";
		try
		{
			path = pluginDirFile.getCanonicalPath();
		}
		catch (IOException e)
		{
			RailsLog.logError("Error getting file path", e);
		}
		return path;
	}

	public String getRailsPath()
	{
		// if user has already configured, just use what they put in.
		String path = getSavedPath(IRailsConstants.PREF_RAILS_PATH);
		if (path != null && path.trim().length() > 0)
			return path;
		return buildBinExecutablePath(RAILS, "rails");
	}

	public String getMongrelPath()
	{
		// if user has already configured, just use what they put in.
		String path = getSavedPath(IRailsConstants.PREF_MONGREL_PATH);
		if (path != null && path.trim().length() > 0)
			return path;
		return buildBinExecutablePath("mongrel_rails", "mongrel");
	}

	private String getSavedPath(String prefKey)
	{
		String path = getPluginPreferences().getString(prefKey);
		if (path == null || path.trim().length() > 0)
			return null;
		if (path.endsWith(".bat") || path.endsWith(".cmd"))
		{
			return path.substring(0, path.length() - 4);
		}
		return path;
	}

	/**
	 * Searches for the bin script in the interpreter's bin directory, In the gem install paths' bin directories, in the
	 * bin directory of the related gem, and lastly in the System PATH.
	 * 
	 * @param command
	 * @param relatedGemName
	 * @return
	 */
	public File findBinScript(String binScriptName, String relatedGemName)
	{
		String result = buildBinExecutablePath(binScriptName, relatedGemName);
		if (result != null)
			return new File(result);
		return null;
	}

	private String buildBinExecutablePath(String command, String gemName)
	{
		// Check the bin directory where ruby executable is. If it doesn't exist there, try a bin subdir of gem install
		// directory.
		IPath path = RubyRuntime.checkInterpreterBin(command);
		if (path != null && path.toFile().exists())
		{
			// Check for wacky Debian/Ubuntu stuff where they mess around and put a shell script where rails script
			// usually is
			if (command.equals(RAILS))
			{
				IPath debianPath = new Path(WACKY_DEBIAN_RAILS_PATH);
				if (debianPath.toFile().exists())
					return debianPath.toOSString();
			}
			return path.toOSString();
		}
		path = AptanaRDTPlugin.checkBinDir(command);
		if (path != null && path.toFile().exists())
			return path.toOSString();

		if (gemName != null)
		{
			path = AptanaRDTPlugin.checkGemBinDir(gemName, command);
			if (path != null && path.toFile().exists())
				return path.toOSString();
		}

		path = RubyCore.checkSystemPath(command);
		if (path != null && path.toFile().exists())
			return path.toOSString();
		return null;
	}

	public static Set<IProject> getRailsProjects()
	{
		Set<IProject> projectSet = new HashSet<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++)
		{
			IProject project = projects[i];
			try
			{
				if (project.hasNature(IRailsConstants.RAILS_PROJECT_NATURE))
				{
					projectSet.add(project);
				}
			}
			catch (CoreException e)
			{
				// ignore
			}
		}
		return projectSet;
	}

	public IGemManager getGemManager()
	{
		return (IGemManager) gemManagerTracker.getService();
	}

	private void initializePreferences()
	{
		// Create lookups
		preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(PLUGIN_ID);
		preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(PLUGIN_ID);

		// Listen to instance preferences node removal from parent in order to refresh stored one
		IEclipsePreferences.INodeChangeListener listener = new IEclipsePreferences.INodeChangeListener()
		{
			public void added(IEclipsePreferences.NodeChangeEvent event)
			{
				// do nothing
			}

			public void removed(IEclipsePreferences.NodeChangeEvent event)
			{
				if (event.getChild() == preferencesLookup[PREF_INSTANCE])
				{
					preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(PLUGIN_ID);
					preferencesLookup[PREF_INSTANCE].addPreferenceChangeListener(new EclipsePreferencesListener());
				}
			}
		};
		((IEclipsePreferences) preferencesLookup[PREF_INSTANCE].parent()).addNodeChangeListener(listener);
		preferencesLookup[PREF_INSTANCE].addPreferenceChangeListener(new EclipsePreferencesListener());

		// Listen to default preferences node removal from parent in order to refresh stored one
		listener = new IEclipsePreferences.INodeChangeListener()
		{
			public void added(IEclipsePreferences.NodeChangeEvent event)
			{
				// do nothing
			}

			public void removed(IEclipsePreferences.NodeChangeEvent event)
			{
				if (event.getChild() == preferencesLookup[PREF_DEFAULT])
				{
					preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(PLUGIN_ID);
				}
			}
		};
		((IEclipsePreferences) preferencesLookup[PREF_DEFAULT].parent()).addNodeChangeListener(listener);
	}

	public Hashtable<String, String> getOptions()
	{

		// return cached options if already computed
		// if (this.optionsCache != null) return new Hashtable<String, String>(this.optionsCache);

		// init
		Hashtable<String, String> options = new Hashtable<String, String>(10);
		IPreferencesService service = Platform.getPreferencesService();

		// set options using preferences service lookup
		Iterator iterator = optionNames.iterator();
		while (iterator.hasNext())
		{
			String propertyName = (String) iterator.next();
			String propertyValue = service.get(propertyName, null, this.preferencesLookup);
			if (propertyValue != null)
			{
				options.put(propertyName, propertyValue);
			}
		}

		// store built map in cache
		this.optionsCache = new Hashtable<String, String>(options);

		// return built map
		return options;
	}

	public static List<String> getEligibleDatabaseNamesforCurrentVM()
	{
		// FIXME What about the strings in IDatabaseConstants?!
		List<String> dbNames = new ArrayList<String>();
		dbNames.add("ibm_db");
		if (RubyRuntime.currentVMIsJRuby())
		{
			dbNames.add("derby");
		}
		dbNames.add("sqlite3");
		dbNames.add("sqlite2");
		dbNames.add("frontbase");
		dbNames.add("mysql");
		dbNames.add("oracle");
		dbNames.add("postgresql");
		dbNames.add("sqlserver");
		return dbNames;
	}

} // RailsPlugin