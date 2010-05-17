package org.radrails.rails.internal.ui.console;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.ITerminal;

public class RailsShellExecutor
{

	private ITerminal shell;
	private String fRunMode;

	public RailsShellExecutor(ITerminal shell)
	{
		this.shell = shell;
	}

	public void run(IProject project, String command)
	{
		run(project, command, ILaunchManager.RUN_MODE);
	}

	public void run(IProject project, String command, String runMode)
	{
		fRunMode = runMode;
		String modifiedFullCommand = command;
		if (modifiedFullCommand.startsWith("ruby "))
		{ // strip off preceding 'ruby'
			modifiedFullCommand = command.substring(5);
		}
		// Look for run mode prefixes
		if (modifiedFullCommand.startsWith("debug "))
		{
			modifiedFullCommand = command.substring(6);
			fRunMode = ILaunchManager.DEBUG_MODE;
		}
		else if (modifiedFullCommand.startsWith("profile "))
		{
			modifiedFullCommand = command.substring(8);
			fRunMode = ILaunchManager.PROFILE_MODE;
		}

		if (modifiedFullCommand.trim().length() == 0)
		{
			shell.write(IDebugUIConstants.ID_STANDARD_INPUT_STREAM, "\n" + IRailsShellConstants.PROMPT);
			return;
		}

		String firstCommandToken = modifiedFullCommand;
		if (firstCommandToken.startsWith("sudo "))
		{
			firstCommandToken = firstCommandToken.substring(5);
		}
		if (firstCommandToken.indexOf(' ') != -1)
		{
			firstCommandToken = firstCommandToken.substring(0, firstCommandToken.indexOf(' '));
		}
		

		List<RailsShellCommandProvider> providers = getCommandProviders(project);
		for (RailsShellCommandProvider railsShellCommandProvider : providers)
		{
			if (!commandMatches(railsShellCommandProvider, firstCommandToken))
				continue;

			if (project == null && railsShellCommandProvider.projectNeedsToBeSelected())
			{
				projectNeedsToBeSelected(shell);
			}
			else
			{
				railsShellCommandProvider.run(shell, modifiedFullCommand);
			}
			return;
		}
	}

	private boolean commandMatches(RailsShellCommandProvider railsShellCommandProvider, String firstCommandToken)
	{
		if (railsShellCommandProvider.handlesAll())
			return true;
		for (String command : railsShellCommandProvider.commandsHandled())
		{
			if (command.equals(firstCommandToken))
				return true;
		}
		return false;
	}

	protected void projectNeedsToBeSelected(final ITerminal shell)
	{
		shell.write(IDebugUIConstants.ID_STANDARD_ERROR_STREAM, Messages.RailsShell_ProjectNeedsToBeSelected + "\n");
		shell.write(IDebugUIConstants.ID_STANDARD_INPUT_STREAM, IRailsShellConstants.PROMPT);
	}

	private List<RailsShellCommandProvider> getCommandProviders(IProject project)
	{
		return RailsShell.getCommandProviders(project, fRunMode);
	}
}
