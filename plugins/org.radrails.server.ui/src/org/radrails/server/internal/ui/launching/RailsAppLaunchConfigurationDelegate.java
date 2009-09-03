package org.radrails.server.internal.ui.launching;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.radrails.rails.ui.browser.BrowserUtil;
import org.radrails.server.core.IServerConstants;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.radrails.server.core.launching.IRailsAppLaunchConfigurationConstants;
import org.radrails.server.internal.ui.ServerUILog;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.ComboDialogField;

public class RailsAppLaunchConfigurationDelegate extends LaunchConfigurationDelegate
{

	public void launch(final ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException
	{
		Server s = getServerForLaunch(configuration);
		if (s == null)
		{
			// Ask user to explicitly choose a server!
			IProject project = getProject(configuration);
			if (project != null)
			{
				Collection<Server> servers = ServerManager.getInstance().getServersForProject(project);
				ServerChoiceRunnable runnable = new ServerChoiceRunnable(servers);
				Display.getDefault().syncExec(runnable);
				s = runnable.getServer();
				if (s != null)
				{
					try
					{
						ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
						wc.setAttribute(IRailsAppLaunchConfigurationConstants.SERVER_NAME, s.getName());
						wc.doSave();
					}
					catch (CoreException e)
					{
						ServerUILog.log(e);
					}
				}
			}
		}
		if (s != null)
		{
			if (s.isStopped())
			{
				s.updateRunMode(mode);
				s.start(false);
			}
			if (configuration.getAttribute(IRailsAppLaunchConfigurationConstants.LAUNCH_BROWSER, false))
			{
				LaunchBrowserThread t = new LaunchBrowserThread(s, configuration);
				if (s.isStarted())
				{
					t.started();
				}
				else
				{
					ServerManager.getInstance().addServerObserver(t);
				}
			}
		}
	}

	private Server getServerForLaunch(ILaunchConfiguration config)
	{
		try
		{
			String serverName = config.getAttribute(IRailsAppLaunchConfigurationConstants.SERVER_NAME, "");
			if (serverName != null && serverName.trim().length() > 0)
			{
				Server s = ServerManager.getInstance().getServer(serverName);
				if (s != null)
				{
					// TODO Ensure that the server is actually tied to the project we're trying to launch!
					return s;
				}
			}
			IProject project = getProject(config);
			if (project == null)
				return null;
			Collection<Server> servers = ServerManager.getInstance().getServersForProject(project);
			if (servers != null && servers.size() == 1)
				return servers.iterator().next();
		}
		catch (CoreException e)
		{
			ServerUILog.logError("error reading launch config attributes", e);
		}
		return null;
	}

	private IProject getProject(ILaunchConfiguration config) throws CoreException
	{
		String projectName = config.getAttribute(IRailsAppLaunchConfigurationConstants.PROJECT_NAME, (String) null);
		if (projectName == null)
			return null;
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

	private String getLaunchUrl(Server server, ILaunchConfiguration config) throws CoreException
	{
		// figure out base URL from server and port
		String baseUrl = "http://" + server.getBrowserHost() + ":" + server.getPort();
		String actionUrl = config.getAttribute(IRailsAppLaunchConfigurationConstants.ACTION_PATH, "");
		// append a leading slash if not present
		if (!actionUrl.startsWith("/"))
		{
			actionUrl = "/" + actionUrl;
		}
		return baseUrl + actionUrl;
	}

	private class LaunchBrowserThread implements Observer
	{

		private Server server;
		private ILaunchConfiguration config;

		public LaunchBrowserThread(Server s, ILaunchConfiguration c)
		{
			server = s;
			config = c;
		}

		public void started()
		{
			try
			{
				launchBrowser(server, config);
			}
			catch (CoreException e)
			{
				ServerUILog.logError("Error launching browser", e);
			}
			catch (IOException e)
			{
				ServerUILog.logError("Error launching browser", e);
			}
		}

		public void update(Observable arg0, Object arg1)
		{
			Server serv = (Server) arg0;
			String action = (String) arg1;
			if (serv.getName().equals(server.getName()) && action.equals(IServerConstants.UPDATE))
			{
				if (serv.getStatus().equals(IServerConstants.STARTED))
				{
					started();
				}
			}
		}

		private void launchBrowser(Server server, ILaunchConfiguration config) throws CoreException, IOException
		{
			final String launchUrl = getLaunchUrl(server, config);
			ServerUILog.logInfo("launching this url: " + launchUrl, null);

			if (config.getAttribute(IRailsAppLaunchConfigurationConstants.USE_EXTERNAL_BROWSER, false))
			{
				String browserExe = config.getAttribute(IRailsAppLaunchConfigurationConstants.BROWSER_EXE, "");

				if (browserExe != null && !browserExe.equals("") && new File(browserExe).exists())
				{
					if (Platform.getOS().equals(Platform.OS_MACOSX))
					{
						Runtime.getRuntime().exec(new String[] { "open", browserExe, launchUrl });
					}
					else
					{
						Runtime.getRuntime().exec(new String[] { browserExe, launchUrl });
					}
				}
			}
			else if (config.getAttribute(IRailsAppLaunchConfigurationConstants.USE_INTERNAL_BROWSER, false))
			{
				// launch the internal browser
				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						try
						{
							BrowserUtil.openOrActivateBrowser(launchUrl);
						}
						catch (PartInitException e)
						{
							ServerUILog.log(e);
						}
						catch (MalformedURLException e)
						{
							ServerUILog.log(e);
						}
					}
				});
			}
			ServerManager.getInstance().deleteServerObserver(this);
		}
	}

	private class ChooseServerDialog extends Dialog
	{

		private Collection<Server> servers;

		private ComboDialogField serverField;

		protected ChooseServerDialog(Shell parentShell, Collection<Server> servers)
		{
			super(parentShell);
			this.servers = servers;

			serverField = new ComboDialogField(SWT.DROP_DOWN | SWT.READ_ONLY);
			serverField.setLabelText("Server:");
			String[] names = new String[servers.size()];
			int i = 0;
			for (Server server : servers)
			{
				names[i++] = server.getName();
			}
			serverField.setItems(names);
			serverField.setText(servers.iterator().next().getName());
		}

		protected void configureShell(Shell newShell)
		{
			super.configureShell(newShell);
			newShell.setText("Choose server");
		}

		public Server getServer()
		{
			String name = serverField.getText();
			for (Server server : servers)
			{
				if (server.getName().equals(name))
					return server;
			}
			return null;
		}

		@Override
		protected Control createDialogArea(Composite parent)
		{
			Composite composite = (Composite) super.createDialogArea(parent);

			Composite control = new Composite(composite, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			control.setLayout(layout);

			Label label = new Label(control, SWT.WRAP);
			label
					.setText("We are unable to find the server previously associated with this launch. Please choose one of the existing servers to use for launching this Rails Application.");
			GridData data = new GridData();
			data.horizontalSpan = 2;
			data.widthHint = 300;
			label.setLayoutData(data);

			serverField.doFillIntoGrid(control, 2);

			return composite;
		}

	}

	private class ServerChoiceRunnable implements Runnable
	{

		private Collection<Server> servers;
		private Server server;

		public ServerChoiceRunnable(Collection<Server> servers)
		{
			this.servers = servers;
		}

		public void run()
		{
			ChooseServerDialog dialog = new ChooseServerDialog(Display.getDefault().getActiveShell(), servers);
			if (dialog.open() == Dialog.OK)
			{
				server = dialog.getServer();
			}
		}

		public Server getServer()
		{
			return server;
		}
	}
}
