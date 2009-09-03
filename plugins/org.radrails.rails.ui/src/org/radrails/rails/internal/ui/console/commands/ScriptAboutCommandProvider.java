package org.radrails.rails.internal.ui.console.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.ITerminal;

public class ScriptAboutCommandProvider extends RailsShellCommandProvider
{
	@Override
	public Set<String> commandsHandled()
	{
		Set<String> commands = new HashSet<String>();
		commands.add(IRailsShellConstants.SCRIPT_ABOUT);
		return commands;
	}

	@Override
	public List<ICompletionProposal> getCompletionProposals(String prefix, List<String> tokens, int offset)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String token = getLastToken(prefix, tokens);
		if (tokens.size() <= 1)
		{
			if (!tokens.contains(IRailsShellConstants.SCRIPT_ABOUT))
			{
				proposals.add(createProposal(IRailsShellConstants.SCRIPT_ABOUT,
						"About your Rails application's environment", offset, token));
			}
		}
		return proposals;
	}

	@Override
	public void run(ITerminal shell, String command)
	{
		launchInsideShell(shell, command);
	}

}
