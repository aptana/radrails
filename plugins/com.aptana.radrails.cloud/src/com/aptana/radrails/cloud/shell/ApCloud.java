package com.aptana.radrails.cloud.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.ide.core.IdeLog;
import com.aptana.radrails.cloud.Activator;
import com.aptana.radrails.cloud.internal.CloudUtil;
import com.aptana.rdt.AptanaRDTPlugin;

class ApCloud
{

	private IProject fLastProject;
	private Map<String, String> fCachedTasks;

	/**
	 * Gets the rake tasks for the passed in project
	 * 
	 * @param project
	 *            The IProject to gather rake tasks for
	 * @param force
	 *            Whether or not to force a refresh (don't grab cached value)
	 * @return a Map of rake task names to their descriptions
	 */
	public Map<String, String> getTasks(IProject project, boolean force)
	{
		if (!force && projectHasntChanged(project) && haveCachedTasks())
		{
			return fCachedTasks;
		}
		fLastProject = project;
		fCachedTasks = null;

		try
		{
			BufferedReader bufReader = new BufferedReader(new StringReader(getTasksText(project,
					getWorkingDirectory(project))));

			Pattern pat = Pattern.compile("^cap\\s+([\\w:]+)\\s+#\\s+(.+)$");
			String line = null;
			Map<String, String> tasks = new HashMap<String, String>();
			while ((line = bufReader.readLine()) != null)
			{
				Matcher mat = pat.matcher(line);
				if (mat.matches())
				{
					tasks.put(mat.group(1), mat.group(2));
				}
			}
			if (tasks.isEmpty())
				return new HashMap<String, String>();
			fCachedTasks = Collections.unmodifiableMap(tasks);
			return fCachedTasks;
		}
		catch (IOException e)
		{
			IdeLog.logError(Activator.getDefault(), "Error parsing rake tasks", e);
		}
		return new HashMap<String, String>();
	}

	private boolean haveCachedTasks()
	{
		return (fCachedTasks != null && !fCachedTasks.isEmpty());
	}

	private boolean projectHasntChanged(IProject selected)
	{
		return selected != null && selected.equals(fLastProject);
	}

	private static String getTasksText(IProject project, String workingDirectory)
	{
		try
		{
			String rakePath = buildBinExecutablePath(AptanaCloudCommandProvider.APCLOUD);
			if (project != null && rakePath != null && rakePath.trim().length() > 0)
			{
				ILaunchConfigurationWorkingCopy wc = RubyRuntime.createBasicLaunch(rakePath,
						AptanaCloudCommandProvider.LIST_TASKS_SWITCH, project, workingDirectory);
				File file = getRakeTasksFile(project);
				String result = RubyRuntime.launchInBackgroundAndRead(wc.doSave(), file);
				if (result == null)
					return "";
				return result;
			}

		}
		catch (CoreException e)
		{
			IdeLog.logError(Activator.getDefault(), "Error listing apcloud tasks", e);
		}
		return "";
	}

	private static File getRakeTasksFile(IProject proj)
	{
		File file = Activator.getDefault().getStateLocation().append(AptanaCloudCommandProvider.APCLOUD).append(
				proj.getName() + "_tasks.txt").toFile();
		try
		{
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		catch (IOException e)
		{
			// ignore
		}
		return file;
	}

	private static String getWorkingDirectory(IProject project)
	{
		if (project == null)
			return null;
		try
		{
			CapfileFinder finder = new CapfileFinder();
			project.accept(finder, IResource.FOLDER);
			File workingDir = finder.getWorkingDirectory();
			if (workingDir != null)
				return workingDir.getAbsolutePath();
		}
		catch (CoreException e)
		{
			IdeLog.logError(Activator.getDefault(), e.getMessage(), e);
		}
		return project.getLocation().toOSString();
	}

	private static String buildBinExecutablePath(String command)
	{
		// Check the bin directory where ruby executable is.
		IPath path = RubyRuntime.checkInterpreterBin(command);
		if (path != null && path.toFile().exists())
			return path.toOSString();

		// try a bin subdir of gem install directory, then try system path
		path = AptanaRDTPlugin.checkBinDir(command);
		if (path != null && path.toFile().exists())
			return path.toOSString();

		// try system path
		path = RubyCore.checkSystemPath(command);
		if (path != null && path.toFile().exists())
			return path.toOSString();

		return null;
	}

	private static class CapfileFinder implements IResourceProxyVisitor
	{

		private static final String CAPFILE = CloudUtil.CAPFILE;
		private File workingDirectory;

		public boolean visit(IResourceProxy proxy) throws CoreException
		{
			if (proxy.getType() == IResource.FILE)
			{
				IPath path = proxy.requestFullPath();
				if (path.lastSegment().equalsIgnoreCase(CAPFILE))
				{
					workingDirectory = path.removeLastSegments(1).toFile();
				}
			}
			return workingDirectory == null
					&& (proxy.getType() == IResource.FOLDER || proxy.getType() == IResource.PROJECT || proxy.getType() == IResource.ROOT);
		}

		public File getWorkingDirectory()
		{
			return workingDirectory;
		}
	}

}
