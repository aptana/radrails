package org.radrails.rails.internal.ui.wizards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.radrails.db.core.DatabasePlugin;
import org.radrails.rails.core.RailsLog;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.RailsInstallDialog;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.internal.ui.wizards.pages.WizardNewRailsProjectPage;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.rails.ui.RailsUIPlugin;
import org.radrails.server.core.IServerConstants;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.radrails.server.core.launching.IRailsAppLaunchConfigurationConstants;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.ide.core.builder.UnifiedProjectBuilder;
import com.aptana.ide.editors.UnifiedEditorsPlugin;
import com.aptana.ide.editors.profiles.Profile;
import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.IGemManager;
import com.aptana.rdt.core.gems.Version;

public class RailsProjectCreator implements IRunnableWithProgress
{
	private static final String RAILS_VERSION_NOT_SUPPORTING_DATABASE_SWITCH = "1.0.0";
	private IProject newProject;
	private IPath newPath;
	private boolean generateSkeleton;
	private boolean forceTerminalCommand = false;
	private String args;

	private String fRunMode = ILaunchManager.RUN_MODE;

	private String dbType;
	private String railsVersion;

	private boolean fStartServer;

	private RailsProjectCreator(IProject project, IPath path, boolean generateSkeleton, String dbType,
			String railsVersion)
	{
		this.newProject = project;
		this.newPath = path;
		this.generateSkeleton = generateSkeleton;
		this.dbType = dbType;
		this.railsVersion = railsVersion;
	}

	public RailsProjectCreator(String runMode, String args)
	{
		this(null, null, true, "", "");
		this.fRunMode = runMode;
		// Grab project name, create a project
		StringTokenizer tokenizer = new StringTokenizer(args);
		String projectName = tokenizer.nextToken();
		while (projectName.startsWith("--") || (projectName.startsWith("_") && projectName.endsWith("_")))
		{
			projectName = tokenizer.nextToken();
		}
		this.newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		this.args = args;
		Pattern p = Pattern.compile("(_\\d\\.\\d\\.\\d_ )?.*");
		Matcher m = p.matcher(args);
		if (m.find())
		{
			String raw = m.group(1);
			if (raw != null && raw.trim().length() > 0)
			{
				this.railsVersion = raw.trim();
				this.railsVersion = this.railsVersion.substring(1, this.railsVersion.length() - 1);
			}
		}
	}

	public RailsProjectCreator(WizardNewRailsProjectPage page)
	{
		this(page.getProjectHandle(), page.getLocationPath(), page.getGenerateButtonSelection(),
				page.getDatabaseType(), page.getRailsVersion());
		this.forceTerminalCommand = true;
		this.fStartServer = page.startServer();
	}

	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		if (generateSkeleton)
		{
			String railsPath = RailsPlugin.getInstance().getRailsPath();
			if (railsPath == null || railsPath.trim().length() == 0)
			{
				UIJob dialog = new RailsInstallDialog(RailsUIPlugin.getInstance().getGemManager());
				dialog.schedule();
				return;
			}
		}
		monitor.beginTask("Creating project...", 40);

		// Create the project description
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = createProjectDescription(workspace);
		
		// Set the location path of the project
		IPath oldPath = Platform.getLocation();
		if (newPath != null && !oldPath.equals(newPath))
		{
			oldPath = newPath;
			description.setLocation(newPath);
		}

		// Create and open the project
		try
		{
			if (!newProject.exists())
			{
				newProject.create(description, new SubProgressMonitor(monitor, 10));
			}

			if (!newProject.isOpen())
			{
				newProject.open(new SubProgressMonitor(monitor, 10));
			}

			// Add the Rails nature, which will add Ruby nature if it needs to
			RailsPlugin.addRailsNature(newProject, new SubProgressMonitor(monitor, 10));

			// Generate the Rails skeleton if requested
			if (generateSkeleton)
			{

				// Run the rails command to create the project files
				ILaunch launch = runRailsCommand(newProject.getLocation().append("../").toFile(), newProject.getName(),
						newProject.getLocation().toFile().getName(), new SubProgressMonitor(monitor, 10));

				if (launch != null)
				{
					final IProcess process = launch.getProcesses()[0];
					DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener()
					{

						public void handleDebugEvents(DebugEvent[] events)
						{
							if (events == null)
								return;
							for (int i = 0; i < events.length; i++)
							{
								if (events[i].getKind() == DebugEvent.TERMINATE
										&& process.equals(events[i].getSource()))
								{
									finishCreatingProject(new NullProgressMonitor());
									DebugPlugin.getDefault().removeDebugEventListener(this);
									return;
								}
							}
						}
					});
				}
				else
				{
					finishCreatingProject(monitor);
				}
			}
		}
		catch (CoreException e)
		{
			throw new InvocationTargetException(e);
		}
		// Generate a server
		if (mongrelNotInstalled())
		{
			generateServer(IServerConstants.TYPE_WEBRICK);
		}
		else
		{
			generateServer(IServerConstants.TYPE_MONGREL);
		}
		monitor.done();
	}

	private IProjectDescription createProjectDescription(IWorkspace workspace)
	{
		IProjectDescription description = workspace.newProjectDescription(newProject.getName());
		description.setNatureIds(new String[] { "com.aptana.ide.project.nature.web" }); //$NON-NLS-1$
		ICommand command = description.newCommand();
		command.setBuilderName(UnifiedProjectBuilder.BUILDER_ID);
		description.setBuildSpec(new ICommand[] {command});
		return description;
	}

	private boolean mongrelNotInstalled()
	{
		IGemManager gemManager = RailsUIPlugin.getInstance().getGemManager();
		return gemManager != null && gemManager.isInitialized() && !gemManager.gemInstalled("mongrel");
	}

	protected void finishCreatingProject(IProgressMonitor monitor)
	{
		IRubyProject rubyProject = RubyCore.create(newProject);
		createCodeAssistProfile(rubyProject);
		if (dbType.equals("derby"))
		{
			replaceDatabaseYML();
		}
		replaceIndex();
		try
		{
			RailsUIPlugin.addDefaultRailsLoadpaths(rubyProject, monitor);
		}
		catch (RubyModelException e)
		{
			RailsUILog.log(e);
		}
		
		overrideDocumentRoot();		
		
		String serverName = getServerName();
		ILaunchConfiguration config = null;
		try
		{
			ILaunchConfigurationType configType = getLaunchManager().getLaunchConfigurationType(
					IRailsAppLaunchConfigurationConstants.LAUNCH_TYPE_ID);
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager()
					.generateUniqueLaunchConfigurationNameFrom(newProject.getName()));
			wc.setAttribute(IRailsAppLaunchConfigurationConstants.PROJECT_NAME, newProject.getName());
			wc.setAttribute(IRailsAppLaunchConfigurationConstants.SERVER_NAME, serverName);
			wc.setAttribute(IRailsAppLaunchConfigurationConstants.LAUNCH_BROWSER, true);
			wc.setAttribute(IRailsAppLaunchConfigurationConstants.USE_INTERNAL_BROWSER, true);
			config = wc.doSave();
		}
		catch (CoreException e)
		{
			RailsUILog.log(e);
		}
		if (fStartServer && config != null)
		{
			startServer(config);
		}
	}

	private void overrideDocumentRoot()
	{
		try
		{
			RailsUIPlugin.overrideDocumentRoot(newProject);
		}
		catch (CoreException e)
		{
			RailsUILog.log(e);
		}
	}

	private void startServer(final ILaunchConfiguration c)
	{
		Job job = new Job("Starting rails server")
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					c.launch(ILaunchManager.RUN_MODE, monitor);
				}
				catch (CoreException e)
				{
					RailsUILog.log(e);
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	private String getServerName()
	{
		Collection<Server> servers = ServerManager.getInstance().getServersForProject(newProject);
		String serverName = null;
		if (servers.size() > 0)
		{
			Server server = servers.iterator().next();
			serverName = server.getName();
		}
		return serverName;
	}

	private void replaceIndex()
	{
		Job job = new Job("Replace project index file")
		{
			protected IStatus run(IProgressMonitor monitor)
			{

				IFile sample = newProject.getFile("public/index.html");
				try
				{
					if (sample.exists())
					{
						sample.delete(true, null);
					}
					Version theVersion = getRailsVersion();
					if (theVersion != null && theVersion.isLessThan("2.0.0"))
					{
						sample.create(this.getClass().getResourceAsStream("/resources/index_1.2.html"), true, null);
					}
					else
					{
						sample.create(this.getClass().getResourceAsStream("/resources/index.html"), true, null);
					}
				}
				catch (CoreException e)
				{
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	protected Version getRailsVersion()
	{
		try
		{
			if (railsVersion == null || railsVersion.trim().length() == 0)
			{
				List<Version> availableVersions = AptanaRDTPlugin.getDefault().getGemManager().getVersions("rails");
				if (availableVersions == null || availableVersions.isEmpty())
					return null;
				Collections.sort(availableVersions);
				return availableVersions.get(availableVersions.size() - 1);
			}
			return new Version(railsVersion);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private void replaceDatabaseYML()
	{
		Job job = new Job("Create database.yml for derby")
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				DatabasePlugin.startDerby();
				String pathToFile = newProject.getLocation().append("config/database.yml").toFile().toString();
				String projectName = newProject.getName();
				projectName = projectName.replace(' ', '_');
				String newContents = "development:\n" + "  adapter: jdbc\n"
						+ "  driver: org.apache.derby.jdbc.ClientDriver\n" + "  url: jdbc:derby://localhost/"
						+ projectName
						+ "_development;create=true\n"
						+ "  username: app\n"
						+ "  password: app\n"
						+ "  \n"
						+ "# Warning: The database defined as 'test' will be erased and\n"
						+ "# re-generated from your development database when you run 'rake'.\n"
						+ "# Do not set this db to the same as development or production.\n"
						+ "test:\n"
						+ "  adapter: jdbc\n"
						+ "  driver: org.apache.derby.jdbc.ClientDriver\n"
						+ "  url: jdbc:derby://localhost/"
						+ projectName
						+ "_test;create=true\n"
						+ "  username: app\n"
						+ "  password: app\n"
						+ "  \n"
						+ "production:\n"
						+ "  adapter: jdbc\n"
						+ "  driver: org.apache.derby.jdbc.ClientDriver\n"
						+ "  url: jdbc:derby://localhost/"
						+ projectName + "_production;create=true\n" + "  username: app\n" + "  password: app\n";
				boolean success = writeNewContents(pathToFile, newContents);
				if (!success)
				{
					return new Status(IStatus.ERROR, RailsUIPlugin.getPluginIdentifier(), -1, "Was unable to overwrite database.yml file with configuration set to use derby.", null);
				}
				DatabasePlugin.startDerby();
				return Status.OK_STATUS;
			}

			private boolean writeNewContents(final String pathToFile, String newContents)
			{
				PrintWriter writer = null;
				try
				{
					writer = new PrintWriter(new FileWriter(pathToFile));
					writer.println(newContents);
				}
				catch (FileNotFoundException fnfe)
				{
					UIJob job = new UIJob("Display error")
					{

						public IStatus runInUIThread(IProgressMonitor monitor)
						{
							MessageDialog.openError(Display.getDefault().getActiveShell(),
									"Error creating derby database.yml", "File " + pathToFile + " not found");
							return Status.OK_STATUS;
						}
					};
					job.schedule();
					return false;
				}
				catch (final IOException ioe)
				{
					UIJob job = new UIJob("Display error")
					{

						public IStatus runInUIThread(IProgressMonitor monitor)
						{
							MessageDialog.openError(Display.getDefault().getActiveShell(),
									"Error creating derby database.yml", ioe.getLocalizedMessage());
							return Status.OK_STATUS;
						}
					};
					job.schedule();
					return false;
				}
				finally
				{
					if (writer != null)
						writer.close();
				}
				return true;
			}
		};
		job.schedule();
	}

	private void generateServer(String type)
	{
		Server s = new Server(newProject, newProject.getName(), type, Server.DEFAULT_RAILS_HOST, null, null);
		ServerManager.getInstance().addServer(s);
	}

	/**
	 * Create a profile for the included prototype/scriptaculous
	 * 
	 * @param rubyProject
	 */
	private void createCodeAssistProfile(final IRubyProject rubyProject)
	{
		Display.getDefault().asyncExec(new Runnable()
		{

			public void run()
			{
				String profilePath = rubyProject.getProject().getLocation().toFile().toURI().toString();
				if (profilePath.endsWith("/") || profilePath.endsWith("\\"))
				{
					profilePath = profilePath.substring(0, profilePath.length() - 1);
				}
				Profile profile = UnifiedEditorsPlugin.getDefault().getProfileManager().createProfile(
						rubyProject.getElementName(), profilePath);
				IPath path = rubyProject.getProject().getLocation().append("public").append("javascripts");
				String[] uris = new String[5];
				uris[0] = path.append("prototype.js").toFile().toURI().toString();
				uris[1] = path.append("effects.js").toFile().toURI().toString();
				uris[2] = path.append("dragdrop.js").toFile().toURI().toString();
				uris[3] = path.append("controls.js").toFile().toURI().toString();
				uris[4] = path.append("application.js").toFile().toURI().toString();
				profile.addURIs(uris);
				UnifiedEditorsPlugin.getDefault().getProfileManager().addProfile(profile);
				UnifiedEditorsPlugin.getDefault().getProfileManager().setCurrentProfile(profile);
			}

		});
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
	 * Runs the rails command to create a new project.
	 * 
	 * @param location
	 *            the full path to the project root
	 * @param args
	 *            the arguments to the rails command
	 * @param monitor
	 *            the progress monitor for the operation
	 */
	private ILaunch runRailsCommand(File location, String projName, String appName, IProgressMonitor monitor)
	{
		if (appName.indexOf(" ") != -1 && appName.charAt(0) != '"')
		{
			// surround with quotes
			appName = '"' + appName + '"';
		}
		String rails = RailsPlugin.getInstance().getRailsPath();

		try
		{
			ILaunchConfigurationType configType = getRubyApplicationConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, RubyRuntime
					.generateUniqueLaunchConfigurationNameFrom("rails_" + appName));
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, rails);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, RubyRuntime.getDefaultVMInstall()
					.getName());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, RubyRuntime.getDefaultVMInstall()
					.getVMInstallType().getId());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getArgs(appName));
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
			if (forceTerminalCommand)
				wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_TERMINAL_COMMAND, "rails " + getArgs(appName));
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, projName);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, location.toString());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_IS_SUDO, false);
			wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
			wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_REQUIRES_REFRESH, true);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_USE_TERMINAL, IRailsShellConstants.TERMINAL_ID);
			ILaunchConfiguration config = wc.doSave();
			return config.launch(fRunMode, monitor);
		}
		catch (CoreException e)
		{
			RailsLog.logError("Error running rake task", e);
		}
		return null;
	}

	public String getArgs(String appName)
	{
		if (this.args != null)
			return args;
		String generatedArgs = "";
		if (railsVersion != null)
			generatedArgs += "_" + railsVersion + "_ ";	
		generatedArgs += appName;
		Version railsVersion = getRailsVersion();
		if (railsVersion != null && railsVersion.isLessThanOrEqualTo(RAILS_VERSION_NOT_SUPPORTING_DATABASE_SWITCH))
		{
			return generatedArgs;
		}		
		String dbTypeToUse = dbType;
		if (dbType.equals("derby"))
		{
			dbTypeToUse = "sqlite3";
		}
		generatedArgs += " -d " + dbTypeToUse;
		return generatedArgs;
	}

	public IProject getProject()
	{
		return newProject;
	}

}
