package org.radrails.rails.internal.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.radrails.rails.core.IRailsConstants;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.console.RailsShell;
import org.radrails.rails.ui.RailsUI;

public class RailsShellOpenJob extends UIJob
{

	private BundleContext context;

	public RailsShellOpenJob(BundleContext context)
	{
		super("Open Rails Shell");
		this.context = context;
	}

	@Override
	public boolean shouldSchedule()
	{
		return Platform.getPreferencesService().getBoolean(RailsPlugin.PLUGIN_ID,
				IRailsConstants.AUTO_OPEN_RAILS_SHELL, true, null);
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor)
	{
		final IWorkbenchWindow dw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (dw == null)
			return Status.CANCEL_STATUS;
		IWorkbenchPage page = dw.getActivePage();
		if (page == null)
			return Status.CANCEL_STATUS;
		IPerspectiveDescriptor desc = page.getPerspective();
		if (desc == null || desc.getId() == null)
			return Status.CANCEL_STATUS;
		if (!desc.getId().equals(RailsUI.ID_PERSPECTIVE))
			return Status.CANCEL_STATUS;

		final RailsShell shell = RailsShell.open();
		if (javascriptConsoleWillAppear())
		{
			IConsoleListener listener = new IConsoleListener()
			{

				public void consolesRemoved(IConsole[] consoles)
				{
				}

				public void consolesAdded(IConsole[] consoles)
				{
					boolean addedJSConsole = false;
					for (int i = 0; i < consoles.length; i++)
					{
						if (isJavascriptConsole(consoles[i]))
						{
							addedJSConsole = true;
							break;
						}
					}
					if (!addedJSConsole)
						return;
					final IConsoleListener self = this;
					Job job = new UIJob("")
					{
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor)
						{
							// Force Rails Shell over top of JS Console
							ConsolePlugin.getDefault().getConsoleManager().showConsoleView(shell);
							ConsolePlugin.getDefault().getConsoleManager().removeConsoleListener(self);
							return Status.OK_STATUS;
						}
					};
					job.setSystem(true);
					job.schedule();
				}

			};
			ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(listener);
		}
		return Status.OK_STATUS;
	}

	private boolean javascriptConsoleWillAppear()
	{
		if (javascriptConsoleAlreadyOpen())
			return false;
		return javascriptConsoleBundleInstalled();
	}

	private boolean javascriptConsoleBundleInstalled()
	{
		Bundle[] bundles = context.getBundles();
		for (int i = 0; i < bundles.length; i++)
		{
			String name = bundles[i].getSymbolicName();
			if (name.equals("org.eclipse.eclipsemonkey.lang.javascript"))
			{
				return true;
			}
		}
		return false;
	}

	private boolean javascriptConsoleAlreadyOpen()
	{
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		for (int i = 0; i < consoles.length; i++)
		{
			if (isJavascriptConsole(consoles[i]))
				return true;
		}
		return false;
	}

	private boolean isJavascriptConsole(IConsole console)
	{
		String classname = console.getClass().getName();
		return classname.contains("JavaScriptConsole");
	}

}
