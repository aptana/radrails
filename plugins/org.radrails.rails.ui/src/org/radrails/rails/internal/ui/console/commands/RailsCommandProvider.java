package org.radrails.rails.internal.ui.console.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.internal.ui.wizards.RailsProjectCreator;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.ITerminal;

public class RailsCommandProvider extends RailsShellCommandProvider
{
	@Override
	public Set<String> commandsHandled()
	{
		Set<String> commands = new HashSet<String>();
		commands.add(IRailsShellConstants.RAILS);
		return commands;
	}

	@Override
	public List<ICompletionProposal> getCompletionProposals(String prefix, List<String> tokens, int offset)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String token = getLastToken(prefix, tokens);
		if (tokens.size() <= 1)
		{
			if (!tokens.contains(IRailsShellConstants.RAILS))
			{
				proposals
						.add(createProposal(IRailsShellConstants.RAILS, "Generate a new rails project", offset, token));
			}
		}

		if (tokens.contains("-h") || tokens.contains("-v"))
			return proposals;

		String lastToken = "";
		if (!tokens.isEmpty())
			lastToken = tokens.get(tokens.size() - 1);

		if (lastToken.equals("-d"))
		{
			List<String> dbNames = RailsPlugin.getEligibleDatabaseNamesforCurrentVM();
			for (String dbName : dbNames)
			{
				proposals.add(createProposal(dbName, offset, token));
			}
			return proposals;
		}

		if (!tokens.contains("-d"))
			proposals.add(createProposal("-d", "Preconfigure for selected database", offset, token));
		if (!tokens.contains("-f"))
			proposals.add(createProposal("-f", "Freeze rails in vendor", offset, token));
		proposals.add(createProposal("-v", "Show version number and quit", offset, token));
		proposals.add(createProposal("-h", "Show help and quit", offset, token));
		return proposals;
	}

	@Override
	public void run(ITerminal shell, String command)
	{
		List<String> tokens = getTokens(command);
		if (tokens.contains("-h") || tokens.contains("-v") || tokens.contains("-p") || tokens.contains("--help")
				|| tokens.contains("--version") || tokens.contains("--pretend"))
		{
			// Just run normally as a launch
			launchInsideShell(shell, command);
		}
		else
		{
			// Run RailsProjectCreator
			try
			{
				RailsProjectCreator creator = new RailsProjectCreator(getRunMode(), getArgs(command));
				WorkspaceModifyDelegatingOperation op = new WorkspaceModifyDelegatingOperation(creator);
				op.run(new NullProgressMonitor());
				shell.setProject(creator.getProject());
			}
			catch (InvocationTargetException e)
			{
				RailsUILog.log(e);
			}
			catch (InterruptedException e)
			{
				RailsUILog.log(e);
			}
		}
	}

	private List<String> getTokens(String command)
	{
		StringTokenizer tokenizer = new StringTokenizer(command);
		List<String> tokens = new ArrayList<String>();
		while (tokenizer.hasMoreTokens())
		{
			tokens.add(tokenizer.nextToken());
		}
		return tokens;
	}
}
