package com.aptana.radrails.cloud.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.ide.core.model.user.AptanaUser;
import com.aptana.ide.core.model.user.User;
import com.aptana.ide.server.cloud.services.model.studio.StudioSite;
import com.aptana.radrails.cloud.Activator;
import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.IGemManager;
import com.aptana.rdt.core.gems.Version;

public abstract class CloudUtil
{

	private static final String APTANA_SITE_ENV = "APTANA_SITE";
	private static final String APTANA_PASSWORD_ENV = "APTANA_PASSWORD";
	private static final String APTANA_ID_ENV = "APTANA_ID";
	private static final String MINIMUM_RUBYGEMS_VERSION = "1.3.1";
	private static final String MINIMUM_APCLOUD_GEM_VERSION = "1.0.8";
	private static final String ADD_APTANA_GEMS_SOURCE_COMMAND = "sources -a http://gems.aptana.com";
	private static final String APCLOUD_GEM_NAME = "aptana_cloud";
	public static final String CAPFILE = "Capfile";

	/**
	 * Checks if the required cloud gem is installed. If not it then tries to install it. This method will block until
	 * the gem is installed! RUN INSIDE A JOB!
	 * 
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the user. It is the caller's responsibility to
	 *            call done() on the given monitor. Accepts <code>null</code>, indicating that no progress should be
	 *            reported and that the operation cannot be cancelled.
	 * @return IStatus indicating if gem is present (OK), CANCEL if monitor is canceled, or not OK if we were unable to
	 *         determine if gem was installed, or were unable to install it.
	 */
	public static IStatus installCloudGemIfNecessary(IProgressMonitor monitor)
	{
		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Installing aptana_cloud gem", 100);

		IGemManager gemManager = AptanaRDTPlugin.getDefault().getGemManager();
		if (gemManager == null)
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Unable to find a gem manager.", null);
		monitor.worked(5);
		if (gemManager.gemInstalled(APCLOUD_GEM_NAME))
		{
			return updateApcloudGem(gemManager, monitor);
		}
		monitor.worked(10);

		IStatus result = null;
		// Detect rubygems version. If older than 1.3.1 upgrade rubygems
		if (rubygemsOutOfDate(gemManager))
		{
			result = upgradeRubyGems(gemManager, monitor);
		}
		monitor.worked(35);

		if (result != null && !result.isOK())
			return result;

		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 5);
		result = addAptanaSource(gemManager, monitor);
		subMonitor.done();
		if (!result.isOK())
			return result;

		subMonitor = new SubProgressMonitor(monitor, 45);
		result = gemManager.installGem(new Gem(APCLOUD_GEM_NAME, Gem.ANY_VERSION, null), subMonitor);
		subMonitor.done();
		return result;
	}

	private static boolean rubygemsOutOfDate(IGemManager gemManager)
	{
		Version rubygemsVersion = gemManager.getVersion();
		if (rubygemsVersion == null || rubygemsVersion.isLessThan(MINIMUM_RUBYGEMS_VERSION))
		{
			return true;
		}
		return false;
	}

	private static IStatus upgradeRubyGems(IGemManager gemManager, IProgressMonitor monitor)
	{
		return gemManager.updateSystem(monitor);
	}

	private static IStatus updateApcloudGem(IGemManager gemManager, IProgressMonitor monitor)
	{
		if (minimumApcloudVersionInstalled(gemManager))
			return Status.OK_STATUS;
		// Upgrade it!
		IStatus result = gemManager.update(new Gem(APCLOUD_GEM_NAME, Gem.ANY_VERSION, null), monitor);
		if (result != null && !result.isOK())
			return result;

		// Check that it really was successful
		if (!minimumApcloudVersionInstalled(gemManager))
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Failed to update aptana_cloud gem to "
					+ MINIMUM_APCLOUD_GEM_VERSION, null);
		return Status.OK_STATUS;
	}

	private static boolean minimumApcloudVersionInstalled(IGemManager gemManager)
	{
		List<Version> versions = gemManager.getVersions(APCLOUD_GEM_NAME);
		for (Version version : versions)
		{
			if (version.isGreaterThanOrEqualTo(MINIMUM_APCLOUD_GEM_VERSION))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds gems.aptana.com as a gem source.
	 * 
	 * @param gemManager
	 * @param monitor
	 * @return
	 */
	private static IStatus addAptanaSource(IGemManager gemManager, IProgressMonitor monitor)
	{
		// TODO Add this to IGemManager!
		try
		{
			ILaunchConfiguration config = gemManager.run(ADD_APTANA_GEMS_SOURCE_COMMAND);
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_USE_TERMINAL, IRailsShellConstants.TERMINAL_ID);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_TERMINAL_COMMAND, ADD_APTANA_GEMS_SOURCE_COMMAND);
			config = wc.doSave();
			final ILaunch launch = config.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
			while (!launch.isTerminated())
			{
				if (monitor.isCanceled())
				{
					launch.terminate();
					return Status.CANCEL_STATUS;
				}
				Thread.yield();
			}
			// Check exit status
			if (launch.getProcesses() != null && launch.getProcesses()[0] != null
					&& launch.getProcesses()[0].getExitValue() != 0)
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1,
						"Adding http://gems.aptana.com as a gem source failed", null);
		}
		catch (CoreException e)
		{
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

	/**
	 * Creates an environment variable map to use in launch configurations for cloud related commands to pre-populate
	 * the username, password and site id.
	 * 
	 * @param site
	 * @return
	 */
	public static Map<String, String> getEnvMap(StudioSite site)
	{
		Map<String, String> envMap = new HashMap<String, String>();
		User user = AptanaUser.getSignedInUser();
		if (user != null)
		{
			envMap.put(APTANA_ID_ENV, user.getUsername());
			envMap.put(APTANA_PASSWORD_ENV, user.getPassword());
		}
		if (site != null)
		{
			envMap.put(APTANA_SITE_ENV, site.getId());
		}
		return envMap;
	}

	public static ILaunch run(String command, String args, IProject project, Map<String, String> envMap,
			boolean forceRefresh) throws CoreException
	{
		return run(command, args, project, envMap, forceRefresh, false);
	}

	public static ILaunch run(String command, String args, IProject project, Map<String, String> envMap,
			boolean forceRefresh, boolean isSudo) throws CoreException
	{
		String commandLine = command + ' ' + args;
		String workingDirectory = null;
		if (project != null)
		{
			workingDirectory = project.getLocation().toFile().getAbsolutePath();
		}
		String fileName = RailsShellCommandProvider.getFileIfExists(command, workingDirectory, project);

		ILaunchConfigurationType configType = getRubyApplicationConfigType();
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, RubyRuntime
				.generateUniqueLaunchConfigurationNameFrom(commandLine));
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, fileName);
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, RubyRuntime.getDefaultVMInstall()
				.getName());
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, RubyRuntime.getDefaultVMInstall()
				.getVMInstallType().getId());
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$
		if (envMap != null && !envMap.isEmpty())
		{
			wc.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
			wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, envMap);
		}
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_TERMINAL_COMMAND, commandLine);
		if (project != null)
		{
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
		}
		if (workingDirectory != null)
		{
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, workingDirectory);
		}
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_IS_SUDO, false);
		wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
		wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
		if (forceRefresh)
		{
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_REQUIRES_REFRESH, true);
		}
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_USE_TERMINAL, IRailsShellConstants.TERMINAL_ID);
		ILaunchConfiguration config = wc.doSave();
		return config.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
	}

	private static ILaunchConfigurationType getRubyApplicationConfigType()
	{
		return getLaunchManager().getLaunchConfigurationType(IRubyLaunchConfigurationConstants.ID_RUBY_APPLICATION);
	}

	private static ILaunchManager getLaunchManager()
	{
		return DebugPlugin.getDefault().getLaunchManager();
	}
}
