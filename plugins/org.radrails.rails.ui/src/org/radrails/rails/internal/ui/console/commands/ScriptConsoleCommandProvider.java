package org.radrails.rails.internal.ui.console.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.ITerminal;

public class ScriptConsoleCommandProvider extends RailsShellCommandProvider
{

	private static final String COMMAND = IRailsShellConstants.SCRIPT_CONSOLE;

	@Override
	public Set<String> commandsHandled()
	{
		Set<String> commands = new HashSet<String>();
		commands.add(COMMAND);
		return commands;
	}

	@Override
	public List<ICompletionProposal> getCompletionProposals(String prefix, List<String> tokens, int offset)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String token = getLastToken(prefix, tokens);
		if (tokens.size() <= 1)
		{
			if (!tokens.contains(IRailsShellConstants.SCRIPT_CONSOLE))
			{
				proposals.add(createProposal(IRailsShellConstants.SCRIPT_CONSOLE,
						"Executes IRB with the context of your Rails application", offset, token));
			}
		}
		if (tokens.isEmpty())
			return proposals;
		if (!tokens.contains("development") && !tokens.contains("production") && !tokens.contains("test"))
		{
			proposals.add(createProposal("development",
					"Specifies to run under the development environment [Optional]", offset, token));
			proposals.add(createProposal("production", "Specifies to run under the production environment", offset,
					token));
			proposals.add(createProposal("test", "Specifies to run under the test environment", offset, token));
		}
		if (!tokens.contains("-s"))
			proposals.add(createProposal("-s", "Rollback database modifications on exit", offset, token));
		return proposals;
	}

	@Override
	public void run(ITerminal shell, String command)
	{
		runInNewConsole(shell, command);
	}

	@Override
	public boolean projectNeedsToBeSelected()
	{
		return true;
	}

}
