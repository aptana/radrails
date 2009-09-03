package org.radrails.rails.ui.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.internal.ui.console.RailsShellCompletionProposal;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.rails.ui.RailsUIPlugin;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.util.Util;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.RubyPluginImages;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.ITerminal;
import org.rubypeople.rdt.launching.RubyRuntime;
import org.rubypeople.rdt.ui.viewsupport.ImageDescriptorRegistry;

import com.aptana.ide.core.IdeLog;

public abstract class RailsShellCommandProvider
{

	private static final String CONSOLE_ENCODING = "UTF-8";
	
	private String fRunMode;
	private IProject fProject;

	public abstract List<ICompletionProposal> getCompletionProposals(String prefix, List<String> tokens, int offset);

	protected ICompletionProposal createProposal(String string, int offset, String token)
	{
		return createProposal(string, "", offset, token);
	}

	public abstract Set<String> commandsHandled();

	protected ICompletionProposal createProposal(String string, String description, int offset, String token)
	{
		if (token != null && !string.startsWith(token))
			return null;
		ImageDescriptorRegistry registry = RubyPlugin.getImageDescriptorRegistry();
		Image image = registry.get(RubyPluginImages.DESC_MISC_PUBLIC);
		String display = string;
		if (description != null && description.trim().length() > 0)
		{
			display += " - " + description;
		}
		return new RailsShellCompletionProposal(string, offset - token.length(), token.length(), string.length(),
				image, display, null, description);
	}

	public abstract void run(final ITerminal shell, final String command);

	protected String getRunMode()
	{
		return fRunMode;
	}

	protected IProject getProject()
	{
		return fProject;
	}

	protected String getArgs(String command)
	{
		if (command.startsWith("sudo "))
		{
			command = command.substring(5);
		}
		int index = command.indexOf(' ');
		String parameters = "";
		if (index != -1)
		{
			parameters = command.substring(index).trim();
		}
		return parameters;
	}

	/**
	 * Does a project need to be selected to run this command?
	 * 
	 * @return
	 */
	public boolean projectNeedsToBeSelected()
	{
		return false;
	}

	public void initialize(IProject project, String runMode)
	{
		this.fProject = project;
		this.fRunMode = runMode;
	}

	protected String getLastToken(String prefix, List<String> tokens)
	{
		if (prefix != null && prefix.endsWith(" "))
			return "";
		if (tokens.size() > 0)
			return tokens.get(tokens.size() - 1);
		return "";
	}

	protected void launchInsideShell(ITerminal shell, String command)
	{
		launchInsideShell(shell, command, null);
	}

	protected void launchInsideShell(ITerminal shell, String command, Map<String, String> env)
	{
		launchInsideShell(shell, command, env, null);
	}

	protected void launchInsideShell(ITerminal shell, String command, Map<String, String> env, Map<String, Object> attrs)
	{
		String file = getFile(command);
		String workingDirectory = getRailsRoot().makeAbsolute().toOSString();

		String fullFilePath = getFileIfExists(file, workingDirectory);
		if (fullFilePath == null)
		{
			shell.write(IDebugUIConstants.ID_STANDARD_ERROR_STREAM, "Unknown command '" + command
					+ "'. Type 'help' to show the list of available commands.\n");
			shell.write(IDebugUIConstants.ID_STANDARD_INPUT_STREAM, IRailsShellConstants.PROMPT);
			return;
		}
		// If this is the special provider that handles all non-specific commands, check the command to see if we might
		// need to launch as non-ruby process.
		if (handlesAll() && launchAsNonRubyProcess(file, fullFilePath, command))
			return;

		try
		{
			ILaunchConfigurationWorkingCopy wc = RubyRuntime.createBasicLaunch(fullFilePath, getArgs(command),
					fProject, workingDirectory);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_USE_TERMINAL, IRailsShellConstants.TERMINAL_ID);
			if (env != null && !env.isEmpty())
			{
				wc.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
				wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, env);
			}
			if (attrs != null && !attrs.isEmpty())
			{
				for (Map.Entry<String, Object> entry : attrs.entrySet())
				{
					if (entry.getValue() instanceof List)
					{
						wc.setAttribute(entry.getKey(), (List) entry.getValue());
					}
					else if (entry.getValue() instanceof Map)
					{
						wc.setAttribute(entry.getKey(), (Map) entry.getValue());
					}
					else if (entry.getValue() instanceof String)
					{
						wc.setAttribute(entry.getKey(), (String) entry.getValue());
					}
					else if (entry.getValue() instanceof Integer)
					{
						wc.setAttribute(entry.getKey(), (Integer) entry.getValue());
					}
					else if (entry.getValue() instanceof Boolean)
					{
						wc.setAttribute(entry.getKey(), (Boolean) entry.getValue());
					}
				}
			}

			wc.doSave().launch(fRunMode, new NullProgressMonitor());
		}
		catch (CoreException e)
		{
			RailsUILog.log(e);
		}
	}

	/**
	 * tries to launch the file as a non ruby launch (if it appears that it should be launched that way).
	 * 
	 * @param file
	 * @param fullFilePath
	 * @param command
	 * @return boolean indicating whether we attempted to launch as non-ruby (if false, try launching as ruby process).
	 */
	private boolean launchAsNonRubyProcess(String file, String fullFilePath, String command)
	{
		IPath path = RubyCore.checkSystemPath(file);
		if (path == null || !path.toOSString().equals(fullFilePath))
			return false;
		if (looksLikeRubyFile(fullFilePath))
			return false;
		try
		{
			// TODO Include env vars!
			Process p = Runtime.getRuntime().exec(command, null, getProject().getLocation().toFile());
			Launch launch = new Launch(null, getRunMode(), null);
			launch.setAttribute(IRubyLaunchConfigurationConstants.ATTR_USE_TERMINAL, IRailsShellConstants.TERMINAL_ID);
			IProcess process = DebugPlugin.newProcess(launch, p, command);
			launch.addProcess(process);
			DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);
		}
		catch (IOException e)
		{
			IdeLog.logError(RailsUIPlugin.getInstance(), e.getMessage(), e);
		}
		return true;
	}

	// FIXME Where should this method go?
	public static String getFileIfExists(String file, String workingDirectory, IProject project)
	{
		if (project != null)
		{
			IFile actualFile = project.getFile(file);
			if (actualFile.exists())
			{
				return actualFile.getLocation().toOSString();
			}

			actualFile = project.getFile(file + ".rb");
			if (actualFile.exists())
			{
				actualFile.getLocation().toOSString();
			}
		}
		File rubyFile = null;
		if (workingDirectory != null)
		{
			// Also check relative to working directory
			IPath path = new Path(workingDirectory).append(file);
			rubyFile = path.toFile();
			if (rubyFile != null && rubyFile.exists())
			{
				return rubyFile.getAbsolutePath();
			}

			// append ".rb"
			path = new Path(workingDirectory).append(file + ".rb");
			rubyFile = path.toFile();
			if (rubyFile != null && rubyFile.exists())
			{
				return rubyFile.getAbsolutePath();
			}
		}
		// Check in bin script locations!
		// TODO What if we found it in PATH? It might not be a ruby script! Can we sniff the file and see?
		rubyFile = RailsPlugin.getInstance().findBinScript(file, null);
		if (rubyFile != null && rubyFile.exists())
		{
			return rubyFile.getAbsolutePath();
		}

		return null;
	}

	private String getFileIfExists(String file, String workingDirectory)
	{
		return getFileIfExists(file, workingDirectory, fProject);
	}

	protected ILaunch runInNewConsole(ITerminal shell, String command)
	{
		try
		{
			ILaunchConfigurationWorkingCopy wc = RubyRuntime.createBasicLaunch(getFile(command), getArgs(command),
					getProject(), getRailsRoot().makeAbsolute().toOSString());
			Map<String, String> map = new HashMap<String, String>();
			map.put(IRubyLaunchConfigurationConstants.ATTR_RUBY_COMMAND, "ruby");
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, map);
			wc.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, CONSOLE_ENCODING);
			ILaunch launch = wc.doSave().launch(fRunMode, new NullProgressMonitor());
			shell.write(IDebugUIConstants.ID_STANDARD_INPUT_STREAM, IRailsShellConstants.PROMPT);
			return launch;
		}
		catch (CoreException e)
		{
			RailsUILog.log(e);
		}
		return null;
	}

	private String getFile(String command)
	{
		int index = command.indexOf(' ');
		String file = command;
		if (index != -1)
		{
			file = command.substring(0, index);
		}
		return file;
	}

	private IPath getRailsRoot()
	{
		IPath railsRoot = RailsPlugin.findRailsRoot(fProject);
		if (railsRoot == null || railsRoot.segmentCount() == 0)
		{
			railsRoot = fProject.getLocation();
		}
		else
		{
			railsRoot = fProject.getLocation().append(railsRoot);
		}
		return railsRoot;
	}

	public boolean handlesAll()
	{
		return false;
	}

	/**
	 * Sniff to see if file looks like a ruby file.
	 * 
	 * @param fullFilePath
	 * @return
	 */
	private boolean looksLikeRubyFile(String fullFilePath)
	{
		if (fullFilePath.endsWith(".rb") || fullFilePath.endsWith(".rbw"))
			return true;
		FileInputStream fis = null;
		try
		{
			File file = new File(fullFilePath);
			fis = new FileInputStream(file);
			char[] c = Util.getInputStreamAsCharArray(fis, 1024, null);
			String contents = new String(c);
			return contents.contains("rubygems") || (contents.contains("#!") && contents.contains("ruby"));
		}
		catch (IOException e)
		{
			// ignore
		}
		finally
		{
			try
			{
				if (fis != null)
					fis.close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}
		return false;
	}

}
