package org.radrails.rails.ui.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.RailsUIMessages;
import org.radrails.rails.ui.RailsUILog;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.testunit.launcher.TestUnitLaunchShortcut;

import com.aptana.rdt.rake.IRakeHelper;
import com.aptana.rdt.rake.RakePlugin;

/**
 * Launches a group of Test::Unit tests.
 * 
 * @author Kyle
 */
public class TestLauncher extends TestUnitLaunchShortcut
{

	protected IProject fProject;
	private IProgressMonitor monitor;

	public TestLauncher(IProgressMonitor monitor)
	{
		super();
		this.monitor = monitor;
	}

	public TestLauncher()
	{
		this(new NullProgressMonitor());
	}

	public ILaunch goLaunch(IProject project, String mode, String testFile)
	{
		try
		{
			fProject = project;
			return doLaunch(mode, testFile);
		}
		catch (CoreException e)
		{
			RailsUILog.logError("Error launching tests", e);
		}
		return null;
	}

	protected ILaunch doLaunch(String mode, String testFile) throws CoreException
	{
		String rakePath = RakePlugin.getDefault().getRakePath();
		if ((rakePath == null) || rakePath.equals(""))
		{
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable()
			{
				public void run()
				{
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					MessageDialog.openError(shell, "Error running tests", RailsUIMessages.SpecifyRakePath_message);
				}
			});
			return null;
		}
		monitor.worked(1);
		monitor.subTask("Running rake task 'db:test:prepare'");
		// Run db:test:prepare through rake
		IRakeHelper helper = RakePlugin.getDefault().getRakeHelper();
		ILaunchConfiguration config = helper.run(fProject, "db:test:prepare", "");
		config = makePrivate(config);		
		ILaunch launch = config.launch(mode, null);
		long start = System.currentTimeMillis();
		while (!launch.isTerminated())
		{
			if (monitor.isCanceled())
				return null;
			
			if (System.currentTimeMillis() > (start + 60000))
			{
				RailsUILog.logError("Error waiting for test database to prepare, timed out after 1 minute", null);
				return null;
			}
			Thread.yield();
		}
		monitor.worked(4);
		monitor.subTask("Launching tests");
		// now run tests
		config = createConfiguration(testFile);
		if (config != null)
		{
			return config.launch(mode, null);
		}
		return null;
	}

	private ILaunchConfiguration makePrivate(ILaunchConfiguration config) throws CoreException
	{
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
		config = wc.doSave();
		return config;
	}

	protected ILaunchConfiguration createConfiguration(String testFile)
	{
		String tfPath = RailsPlugin.getInstance().getRubyScriptPath(testFile);
		if (tfPath.indexOf(" ") > -1)
		{
			tfPath = '"' + tfPath + '"';
		}
		ILaunchConfiguration config = createConfiguration(testFile, tfPath, fProject, "");
		try
		{
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
			wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
			wc.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, false);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_REQUIRES_REFRESH, true);
			config = wc.doSave();
		}
		catch (CoreException e)
		{
			// ignore
		}
		return config;
	}
}
