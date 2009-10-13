/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.server.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.radrails.db.core.IDatabaseConstants;
import org.radrails.rails.core.RailsLog;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.core.RailsRuntime;
import org.rubypeople.rdt.core.SocketUtil;
import org.rubypeople.rdt.debug.ui.RdtDebugUiConstants;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.IGemManager;

/**
 * This class represents a web server that is capable of running Rails applications. It is an observable object, and is
 * observed by the ServersView class. Instead of traditional <code>setXXX</code> methods, <code>updateXXX</code> methods
 * are provided. These update methods will set the attribute and then notify all observers of the change.
 * 
 * @author mkent
 */
public class Server extends Observable
{

	private static final String CONSOLE_ENCODING = "UTF-8";

	private static final int THIRTY_SECONDS = 30 * 1000;

	public static final String DEFAULT_RAILS_HOST = "0.0.0.0";
	public static final String DEFAULT_RADRAILS_HOST = "127.0.0.1";

	private IProject project;
	private String name;
	private String type;
	private String port;
	private String host = DEFAULT_RADRAILS_HOST;
	private String environment;
	private String runMode;
	private String status;

	private IProcess serverProcess;
	private int pid = -1;
	private boolean keepStarting;

	private Set<Observer> observers = new HashSet<Observer>();

	/**
	 * Constructor. Status is set to <code>IServerConstants.STOPPED</code> by default.
	 * 
	 * @param project
	 *            the IProject the server is associated with
	 * @param name
	 *            the name of the server
	 * @param type
	 *            the type of the server (use {@link IServerConstants TYPE_XXX})
	 * @param host
	 *            the host/IP of the server
	 * @param portNumber
	 *            the number of the port the server will operate on
	 * @param environment
	 *            the runtime environment of the server (use {@link IDatabaseConstants ENV_XXX})
	 */
	public Server(IProject project, String name, String type, String host, String portNumber, String environment)
	{
		Assert.isNotNull(project, "Server must have a non-null project");
		this.project = project;
		this.name = name;
		this.type = type;
		if (this.type == null || this.type.trim().length() == 0)
		{
			this.type = IServerConstants.TYPE_WEBRICK;
		}
		this.host = host;
		if (this.host == null)
		{
			this.host = DEFAULT_RADRAILS_HOST;
		}
		this.port = portNumber;
		if (this.port == null || this.port.trim().length() == 0)
		{
			this.port = ServerManager.getInstance().getNextAvailablePort();
		}
		else
		{
			try
			{ // try to force to integer
				Integer.parseInt(this.port);
			}
			catch (NumberFormatException nfe)
			{
				this.port = ServerManager.getInstance().getNextAvailablePort();
			}
		}
		this.environment = environment;
		if (this.environment == null || this.environment.trim().length() == 0)
		{
			this.environment = IDatabaseConstants.ENV_DEVELOPMENT;
		}
		this.status = IServerConstants.STOPPED;
		this.runMode = ILaunchManager.RUN_MODE;
	}

	/**
	 * Constructor. Name defaults to projectNameServer, port defaults to next available, and environment defaults to
	 * development.
	 * 
	 * @param project
	 *            the location of the project the server is associated with
	 * @param type
	 *            the type of server (Mongrel, WEBrick, Lighttpd)
	 */
	public Server(IProject project, String type)
	{
		this(project, project.getName() + "Server", type, DEFAULT_RADRAILS_HOST, ServerManager.getInstance()
				.getNextAvailablePort(), IDatabaseConstants.ENV_DEVELOPMENT);
	}

	public Server(IProject project, String serverName, String serverType, String port)
	{
		this(project, serverName, serverType, DEFAULT_RADRAILS_HOST, port, IDatabaseConstants.ENV_DEVELOPMENT);
	}

	/**
	 * @return the location of the project the server is associated with
	 */
	public File getProjectFile()
	{
		return project.getLocation().toFile();
	}

	/**
	 * @return the name of the server
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the port number that the server is available on
	 */
	public String getPort()
	{
		return port;
	}

	public int getPortInt()
	{
		try
		{
			return Integer.parseInt(port);
		}
		catch (Exception e)
		{
			return 3000;
		}
	}

	/**
	 * @return the type of the server
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @return the runtime environment of the server
	 */
	public String getEnvironment()
	{
		return environment;
	}

	/**
	 * @return the status of the server
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * Sets the port number of the server and notifies observers of the change.
	 * 
	 * @param port
	 *            the number of the port the server operates on
	 */
	public void updatePort(String port)
	{
		this.port = port;
		setChanged();
		notifyServerObservers(IServerConstants.UPDATE);
	}

	/**
	 * Sets the name of the server and notifies observers of the change.
	 * 
	 * @param name
	 *            the name of the server
	 */
	public void updateName(String name)
	{
		this.name = name;
		setChanged();
		notifyServerObservers(IServerConstants.UPDATE);
	}

	/**
	 * Sets the environment of the server and notifies observers of the change. Use {@link IDatabaseConstants ENV_XXX}
	 * 
	 * @param environment
	 *            the environment of the server
	 */
	public void updateEnvironment(String environment)
	{
		this.environment = environment;
		setChanged();
		notifyServerObservers(IServerConstants.UPDATE);
	}

	/**
	 * Sets the run mode of the server and notifies observers of the change. Use {@link ILaunchManager XXX_MODE}
	 * 
	 * @param runMode
	 *            the run mode of the server
	 */
	public void updateRunMode(String runMode)
	{
		this.runMode = runMode;
		setChanged();
		notifyServerObservers(IServerConstants.UPDATE);
	}

	/**
	 * Sets the status of the server and notifies observers of the change. Use <code>IServerConstants.STARTED</code> or
	 * <code>IServerConstants.STOPPED</code>
	 * 
	 * @param status
	 *            the status of the server
	 */
	public void updateStatus(String status)
	{
		this.status = status;
		setChanged();
		notifyServerObservers(IServerConstants.UPDATE);
	}

	public void updateHost(String newHost)
	{
		if (newHost == null)
		{
			newHost = DEFAULT_RAILS_HOST;
		}
		this.host = newHost;
		setChanged();
		notifyServerObservers(IServerConstants.UPDATE);
	}

	/**
	 * Updates the server status in a separate thread.
	 */
	protected void syncUpdateStatus(final String status)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				updateStatus(status);
			}
		});
	}

	/**
	 * Sets the changed flag. Clients should call this method before calling the <code>notifyObservers()</code> method
	 * explicitly.
	 */
	public void touch()
	{
		setChanged();
	}

	/**
	 * @return true if the server's status is <code>IServerConstants.STARTED</code>, false otherwise.
	 */
	public boolean isStarted()
	{
		return status.equals(IServerConstants.STARTED);
	}

	/**
	 * @return true if the server's status is <code>IServerConstants.STARTING</code>, false otherwise.
	 */
	public boolean isStarting()
	{
		return status.equals(IServerConstants.STARTING);
	}

	/**
	 * @return true if the server's status is <code>IServerConstants.STOPPED</code>, false otherwise.
	 */
	public boolean isStopped()
	{
		return !isStarted() && !isStarting() && !isStopping();
	}

	/**
	 * @return true if the server's status is <code>IServerConstants.STOPPING</code>, false otherwise.
	 */
	public boolean isStopping()
	{
		return status.equals(IServerConstants.STOPPING);
	}

	/**
	 * Starts the server. Subclass implementations must call {@link #setChanged() setChanged} and
	 * {@link #notifyObservers() notifyObservers} after the server is started. Pass {
	 * <code>IServerConstants.UPDATE</code> as the second argument to {@link #notifyObservers() notifyObservers}.
	 */
	public void start()
	{
		start(true);
	}

	private boolean isMongrel()
	{
		return getType().equals(IServerConstants.TYPE_MONGREL);
	}

	/**
	 * Stops the server. Subclass implementations must call {@link #setChanged() setChanged} and
	 * {@link #notifyObservers() notifyObservers} after the server is started. Pass <code>IServerConstants.UPDATE</code>
	 * as the second argument to {@link #notifyObservers() notifyObservers}.
	 */
	public void stop()
	{
		if (serverProcess != null || pid != -1)
		{
			// Make sure to stop the start job, just in case the user is
			// stopping the server while it's still starting
			keepStarting = false;

			Job j = new Job("Stopping server")
			{
				protected IStatus run(IProgressMonitor monitor)
				{
					stopServer();
					return Status.OK_STATUS;
				}
			};
			j.schedule();
		}
	}

	/**
	 * Restart the WEBrick Server.
	 * 
	 * @see org.radrails.server.core.Server#stop()
	 */
	public void restart()
	{
		Job j = new Job("Restarting server")
		{
			protected IStatus run(IProgressMonitor monitor)
			{
				keepStarting = false;
				stopServer();
				startServer(false);
				return Status.OK_STATUS;
			}
		};
		j.schedule();
	}

	/**
	 * Internal implementation of stopping the server. Attempts to terminate the Process object.
	 */
	private void stopServer()
	{
		syncUpdateStatus(IServerConstants.STOPPING);
		if (getRunMode().equals(ILaunchManager.DEBUG_MODE))
		{
			// FIXME We need to properly kill the debugger!
			IDebugTarget debugtarget = serverProcess.getLaunch().getDebugTarget();
			try
			{
				debugtarget.terminate();
			}
			catch (DebugException e)
			{
				RailsLog.log(e);
				syncUpdateStatus(IServerConstants.STARTED);
				return;
			}
		}
		else
		{
			// try to kill the process
			if (serverProcess != null)
			{
				int tries = 0;
				while (!serverProcess.isTerminated() && tries < 10)
				{
					try
					{
						serverProcess.terminate();
						Thread.sleep(10);
						tries++;
					}
					catch (DebugException e)
					{
						ServerLog.logError("Error terminating server process", e);
					}
					catch (InterruptedException e)
					{
						ServerLog.logError("Error sleeping", e);
					}
				}
			}
			if (!Platform.getOS().equals(Platform.OS_WIN32))
			{
				if (pid < 0)
				{
					grabPid();
				}

				try
				{
					if (pid > 0 && killProcess())
					{
						pid = -1;
						syncUpdateStatus(IServerConstants.STOPPED);
						return;
					}
				}
				catch (CoreException e)
				{
					// ignore
				}
				catch (InterruptedException e)
				{
					// ignore
				}
				catch (IllegalThreadStateException e)
				{
					// ignore
				}
			}
		}
		if (serverProcess != null && serverProcess.isTerminated())
		{
			pid = -1;
		}
		// Notify the view that the server has stopped
		syncUpdateStatus(IServerConstants.STOPPED);
	}

	private void grabPid()
	{
		BufferedReader reader = null;
		try
		{
			String[] allCmds = new String[] { "ps", "wwwx" };
			Process p = DebugPlugin.exec(allCmds, null);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			String args = getProgramArguments();
			while ((line = reader.readLine()) != null)
			{
				if (line.indexOf(args) != -1)
				{
					String modified = line.trim();
					int index = modified.indexOf(' ');
					if (index == -1)
						continue;
					String pid = modified.substring(0, index);
					try
					{
						this.pid = Integer.parseInt(pid);
						break;
					}
					catch (NumberFormatException e)
					{
						// ignore
					}
				}
			}
		}
		catch (CoreException e)
		{
			ServerLog.log(e);
		}
		catch (IOException e)
		{
			ServerLog.log(e);
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

	}

	private boolean killProcess() throws CoreException, InterruptedException
	{
		List<String> killCommands = new ArrayList<String>();
		killCommands.add(getRubyScriptPath("kill.rb"));
		killCommands.add("INT");
		killCommands.add("" + pid + "");
		Process p = RailsRuntime.rubyExec((String[]) killCommands.toArray(new String[] {}), getProjectFile(), false);
		return 0 == p.exitValue();
	}

	private boolean isWebrick()
	{
		return getType().equals(IServerConstants.TYPE_WEBRICK);
	}

	public void started(IProcess process)
	{
		try
		{
			serverProcess = process;
			IStreamMonitor stdOut = null;
			IStreamMonitor stdErr = null;
			// Prepare the stream listeners
			if (serverProcess.getStreamsProxy() != null)
			{
				stdOut = serverProcess.getStreamsProxy().getOutputStreamMonitor();
				stdErr = serverProcess.getStreamsProxy().getErrorStreamMonitor();
			}
			// Now add a listener for process being terminated, because user
			// could terminate from console view.
			DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener()
			{

				public void handleDebugEvents(DebugEvent[] events)
				{
					if (events == null)
						return;
					for (int i = 0; i < events.length; i++)
					{
						if (events[i].getKind() == DebugEvent.TERMINATE && serverProcess.equals(events[i].getSource()))
						{
							pid = -1;
							syncUpdateStatus(IServerConstants.STOPPED);
						}
					}

				}

			});
			keepStarting = true;
			addPIDListener(stdOut);
			addServerStartedListeners(stdOut, stdErr);
			// Create a new console page for the server process
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm:ss aa");
			String date = dateFormat.format(new Date());

			serverProcess.setAttribute(IProcess.ATTR_PROCESS_LABEL, project.getName() + " - " + getDisplayName()
					+ " Server - (" + date + ")");

			// Wait for the server to start
			long start = System.currentTimeMillis();
			while (keepStarting && !serverProcess.isTerminated())
			{
				Thread.yield();
				if ((start + THIRTY_SECONDS) < System.currentTimeMillis())
					break; // Safety valve to exit loop in 30 seconds
			}
			keepStarting = false;
			if (serverProcess.isTerminated())
				syncUpdateStatus(IServerConstants.STOPPED);
			if (this.status.equals(IServerConstants.STARTING))
				syncUpdateStatus(IServerConstants.STARTED);
		}
		catch (Exception e)
		{
			ServerLog.logError("Error running the server command", e);
			syncUpdateStatus(e.getLocalizedMessage() + "  See the log for details.");
		}
	}

	private void addServerStartedListeners(IStreamMonitor stdOut, IStreamMonitor stdErr)
	{
		if (isMongrel())
		{
			addMongrelStartedListener(stdOut, stdErr);
		}
		else if (isWebrick())
		{
			addServerStartedListener(stdErr, "WEBrick::HTTPServer#start");
		}
	}

	private void addMongrelStartedListener(IStreamMonitor stdOut, IStreamMonitor stdErr)
	{
		addServerStartedListener(stdOut, "=> Ctrl-C to shutdown server");
		addServerStartedListener(stdErr, "available at");
	}

	private void addServerStartedListener(IStreamMonitor monitor, final String string)
	{
		if (monitor == null)
			return;
		monitor.addListener(new IStreamListener()
		{

			public void streamAppended(String text, IStreamMonitor monitor)
			{
				if (text == null)
					return;
				if (!keepStarting)
				{ // Server has started
					monitor.removeListener(this);
					return;
				}
				String[] lines = text.split("\n");
				for (int i = 0; i < lines.length; i++)
				{
					if (lines[i].indexOf(string) != -1)
					{
						keepStarting = false;
						monitor.removeListener(this);
						return;
					}
				}
			}
		});
	}

	private void addPIDListener(IStreamMonitor stdOut)
	{
		if (stdOut == null)
			return;
		stdOut.addListener(new IStreamListener()
		{

			public void streamAppended(String text, IStreamMonitor monitor)
			{
				if (pid != -1)
					return;
				if (text == null)
					return;
				String[] lines = text.split("\n");
				String num = lines[0].trim();
				if (num.startsWith("\""))
				{
					num = num.substring(1);
				}
				if (num.endsWith("\""))
				{
					num = num.substring(0, num.length() - 1);
				}
				try
				{
					pid = Integer.parseInt(num.trim());
				}
				catch (NumberFormatException e)
				{
					pid = -2;
				}
				monitor.removeListener(this);
			}
		});
	}

	/**
	 * Internal implementation of starting the server. Runs the server command, opens a console window, and attaches the
	 * output streams to the console.
	 * 
	 * @param publicLaunchConfig
	 */
	private void startServer(boolean publicLaunchConfig)
	{
		if (!SocketUtil.portFree(Integer.parseInt(getPort())))
		{
			Display.getDefault().asyncExec(new Runnable()
			{

				public void run()
				{
					MessageDialog
							.openError(
									Display.getDefault().getActiveShell(),
									"Port not free",
									"The selected port for this server is not free. Perhaps the server is already running, or another server has the same port?");
				}

			});
			return;
		}
		try
		{
			ILaunchConfiguration config = findOrCreateLaunchConfiguration(publicLaunchConfig);
			int port = Integer.parseInt(getPort());
			if (port < 1024)
			{
				ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
				wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_IS_SUDO, true);
				config = wc.doSave();
			}
			config.launch(runMode, null);
		}
		catch (CoreException e)
		{
			ServerLog.logError("Error running the server command", e);
			syncUpdateStatus(e.getLocalizedMessage() + "  See the log for details.");
		}
	}

	/**
	 * The type of server displayed in console name.
	 * 
	 * @return
	 */
	protected String getDisplayName()
	{
		return getType();
	}

	protected ILaunchConfiguration findOrCreateLaunchConfiguration(boolean publicLaunchConfig) throws CoreException
	{
		ILaunchConfigurationType configType = getRubyApplicationConfigType();

		ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(configType);
		List<ILaunchConfiguration> candidateConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
		for (int i = 0; i < configs.length; i++)
		{
			ILaunchConfiguration config = configs[i];
			boolean projectsEqual = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, "")
					.equals(getProject().getName());
			if (projectsEqual)
			{
				boolean argumentsSame = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
						"").equals(getProgramArguments());
				if (argumentsSame)
				{
					String vmName = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, "");
					String vmType = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, "");
					IVMInstall vm = RubyRuntime.getDefaultVMInstall(); // VMs
					// must
					// match
					// ?
					if (vmName.equals(vm.getName()) && vm.getVMInstallType().getId().equals(vmType))
						candidateConfigs.add(config);
				}
			}
		}

		switch (candidateConfigs.size())
		{
			case 0:
				return createServerLaunchConfiguration(publicLaunchConfig);
			default:
				return (ILaunchConfiguration) candidateConfigs.get(0);
		}
	}

	private ILaunchConfiguration createServerLaunchConfiguration(boolean publicLaunchConfig)
	{
		IPath railsRoot = RailsPlugin.findRailsRoot(project);
		if (railsRoot == null || railsRoot.segmentCount() == 0)
		{
			railsRoot = project.getLocation();
		}
		else
		{
			railsRoot = project.getLocation().append(railsRoot);
		}

		String fileName = null;
		if (isMongrel() && isOldRails())
		{
			fileName = getMongrelRailsScript();
		}
		else
		{
			IPath serverScript = railsRoot.append("script").append("server");
			if (project.getLocation().isPrefixOf(serverScript))
			{
				fileName = serverScript.toPortableString().substring(
						project.getLocation().toPortableString().length() + 1);
			}
			else
			{
				fileName = serverScript.toPortableString();
			}
		}

		ILaunchConfiguration config = null;
		try
		{
			ILaunchConfigurationType configType = getRubyApplicationConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, RubyRuntime
					.generateUniqueLaunchConfigurationNameFrom(getName()));
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, getProject().getName());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, fileName);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, railsRoot.toFile()
					.getAbsolutePath());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, RubyRuntime.getDefaultVMInstall()
					.getName());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, RubyRuntime.getDefaultVMInstall()
					.getVMInstallType().getId());
			wc.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, RdtDebugUiConstants.RUBY_SOURCE_LOCATOR);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getProgramArguments());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
					"-e p(Process.pid.to_s) -e load(ARGV.shift)");
			wc.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, CONSOLE_ENCODING);
			Map<String, String> map = new HashMap<String, String>();
			map.put(IRubyLaunchConfigurationConstants.ATTR_RUBY_COMMAND, "ruby");
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, map);
			wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, !publicLaunchConfig);
			wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
			config = wc.doSave();
		}
		catch (CoreException ce)
		{
			// ignore for now
		}
		return config;
	}

	private boolean isOldRails()
	{
		return doIsOldRails(RailsPlugin.getRailsVersion(project));
	}

	private boolean doIsOldRails(String version)
	{
		// Assume we're newer than 1.1.3 by default now. Rails is way newer for most people
		try
		{
			if (version == null)
				return false;
			String[] numbers = version.split("\\.");
			if (numbers == null || numbers.length == 0)
				return true;

			if (numbers[0].equals("0"))
				return true; // if major version is less than 1, we're too old
			if (!numbers[0].equals("1"))
				return false; // if we're greater than 1, we're new
			int second = Integer.parseInt(numbers[1]);
			if (second > 1)
				return false; // if minor version is greater than 1, we're new
			if (second == 0)
				return true; // if minor version is 0, we're too old
			// if version so far is 1.1 ...
			if (numbers.length == 2)
				return true; // if there's no bugfix version, we're too old
			int third = Integer.parseInt(numbers[2]);
			if (third == 6)
				return true;
			if (third >= 3)
				return false; // if we're 1.1.3 or higher, we're new
		}
		catch (Exception e)
		{
			ServerLog.log(e);
		}
		return false;
	}

	private String getMongrelRailsScript()
	{
		return RailsPlugin.getInstance().getMongrelPath();
	}

	private ILaunchConfigurationType getRubyApplicationConfigType()
	{
		return getLaunchManager().getLaunchConfigurationType(IRubyLaunchConfigurationConstants.ID_RUBY_APPLICATION);
	}

	private ILaunchManager getLaunchManager()
	{
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Construct the arguments to script\server
	 * 
	 * @return
	 */
	public String getProgramArguments()
	{
		StringBuffer args = new StringBuffer();
		if (isMongrel() && isOldRails())
		{
			args.append("start");
			args.append(" -l ");
			args.append(getMongrelLogPath());
		}
		else
		{
			args.append(getServerArg());
		}
		args.append(" --port=");
		args.append(getPort());
		args.append(" --environment=");
		args.append(getEnvironment());
		if (!getHost().equals(DEFAULT_RAILS_HOST))
		{
			// If user is running mongrel_rails, it doesn't like --binding
			if (args.toString().startsWith("start"))
			{
				args.append(" --address=");
				args.append(host);
			}
			else
			{
				args.append(" --binding=");
				args.append(host);
			}
		}
		return args.toString();
	}

	private String getMongrelLogPath()
	{
		IPath path = getProject().getLocation().append(RailsPlugin.findRailsRoot(getProject()));
		path = path.append("log").append("mongrel.log");
		return '"' + path.toOSString() + '"';
	}

	/**
	 * The argument to script\server which tells the type of server to launch.
	 * 
	 * @return
	 */
	protected String getServerArg()
	{
		if (isWebrick())
		{
			return "webrick";
		}
		else if (isMongrel())
		{
			return "mongrel";
		}
		else if (isLighttpd())
		{
			return "lighttpd";
		}
		throw new IllegalStateException("Server has a type that isn't Mongrel, WEBrick or Lighttpd!");
	}

	private boolean isLighttpd()
	{
		return getType().equals(IServerConstants.TYPE_LIGHTTPD);
	}

	/**
	 * Writes a script from plugin jar to plugin metadata folder within the workspace.
	 * 
	 * @param rubyFile
	 *            - The file to place on the filesystem
	 * @return Absolute path to specified script file
	 */
	protected String getRubyScriptPath(String rubyFile)
	{
		String directoryFile = ServerPlugin.getInstance().getStateLocation().toOSString() + File.separator + rubyFile;
		File pluginDirFile = new File(directoryFile);

		if (!pluginDirFile.exists())
		{
			BufferedReader input = null;
			FileWriter output = null;
			try
			{
				pluginDirFile.createNewFile();
				URL u = ServerPlugin.getInstance().getBundle().getEntry("/ruby/" + rubyFile);
				input = new BufferedReader(new InputStreamReader(u.openStream()));
				output = new FileWriter(pluginDirFile);
				String line;
				while ((line = input.readLine()) != null)
				{
					output.write(line);
					output.write('\n');
				}
				output.flush();

			}
			catch (IOException e)
			{
				ServerLog.logError("Error copying script file from jar to metadata", e);
			}
			finally
			{
				if (output != null)
				{
					try
					{
						output.close();
					}
					catch (IOException e)
					{
						// ignore
					}
				}
				if (input != null)
				{
					try
					{
						input.close();
					}
					catch (IOException e)
					{
						// ignore
					}
				}
			}
		}

		String path = "";
		try
		{
			path = pluginDirFile.getCanonicalPath();
		}
		catch (IOException e)
		{
			ServerLog.logError("Error getting script file path", e);
		}
		return path;
	}

	public String getRunMode()
	{
		return runMode;
	}

	@Override
	public boolean equals(Object arg0)
	{
		if (!(arg0 instanceof Server))
			return false;
		Server other = (Server) arg0;

		return getType().equals(other.getType()) && getPort().equals(other.getPort())
				&& getProject().equals(other.getProject()) && getEnvironment().equals(other.getEnvironment());
	}

	public void updateType(String newType)
	{
		type = newType;
		setChanged();
		notifyServerObservers(IServerConstants.UPDATE);
	}

	public void addServerObserver(Observer ob)
	{
		synchronized (observers)
		{
			observers.add(ob);
		}
	}

	public void notifyServerObservers(Object arg)
	{
		Set<Observer> copy;
		synchronized (observers)
		{
			copy = new HashSet<Observer>(observers);
		}
		for (Observer observer : copy)
		{
			observer.update(this, arg);
		}
	}

	public IProject getProject()
	{
		return project;
	}

	public IProcess getProcess()
	{
		return serverProcess;
	}

	public void deleteServerObserver(Observer ob)
	{
		synchronized (observers)
		{
			observers.remove(ob);
		}
	}

	public String getHost()
	{
		if (host == null)
		{
			return DEFAULT_RADRAILS_HOST;
		}
		return host;
	}

	public String getBrowserHost()
	{
		String host = getHost();
		if (Platform.getOS().equals(Platform.OS_WIN32) && host.equals(DEFAULT_RAILS_HOST))
		{
			return DEFAULT_RADRAILS_HOST;
		}
		return host;
	}

	public void start(final boolean publicLaunchConfig)
	{
		if (isMongrel())
		{
			final IGemManager gemManager = RailsPlugin.getInstance().getGemManager();
			if (gemManager != null && gemManager.isInitialized())
			{
				if (!gemManager.gemInstalled("mongrel") && RailsPlugin.getInstance().getMongrelPath() == null)
				{
					// FIXME Pull out strings for translation
					if (MessageDialog
							.openQuestion(Display.getDefault().getActiveShell(), "Mongrel is not installed",
									"It appears that you do not have the mongrel gem installed. Would you like us to install it?"))
					{
						Job job = new Job("Installing mongrel")
						{
							@Override
							protected IStatus run(IProgressMonitor monitor)
							{
								return gemManager.installGem(new Gem("mongrel", Gem.ANY_VERSION, null), monitor);
							}
						};
						job.setUser(true);
						job.schedule();
						return;
					}
				}
			}
		}
		Job j = new Job("Starting server")
		{
			protected IStatus run(IProgressMonitor monitor)
			{
				startServer(publicLaunchConfig);
				return Status.OK_STATUS;
			}
		};
		j.schedule();

	}

	public boolean isLocalhost()
	{
		return getHost() != null
				&& (getHost().equals(DEFAULT_RADRAILS_HOST) || getHost().equals(DEFAULT_RAILS_HOST) || getHost()
						.equals("localhost"));
	}

	/**
	 * Special method to see if there's a process that matches this server already running...
	 */
	void checkIfLeftHanging()
	{
		if (Platform.getOS().equals(Platform.OS_WIN32))
			return;
		grabPid();
		if (this.pid != -1)
		{
			this.status = IServerConstants.STARTED;
		}
	}

}
