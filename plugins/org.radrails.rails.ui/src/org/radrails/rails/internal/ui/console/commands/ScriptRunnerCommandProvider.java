package org.radrails.rails.internal.ui.console.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.ITerminal;

public class ScriptRunnerCommandProvider extends RailsShellCommandProvider
{

	@Override
	public Set<String> commandsHandled()
	{
		Set<String> commands = new HashSet<String>();
		commands.add(IRailsShellConstants.SCRIPT_RUNNER);
		return commands;
	}

	@Override
	public List<ICompletionProposal> getCompletionProposals(String prefix, List<String> tokens, int offset)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String token = getLastToken(prefix, tokens);
		if (tokens.isEmpty())
		{
			proposals
					.add(createProposal(IRailsShellConstants.SCRIPT_RUNNER, "Rails client code runner", offset, token));
		}
		else
		{
			proposals.add(createProposal("-e",
					"Specifies the environment to run this server under. Default: development", offset, token));
			proposals.add(createProposal("-h", "Show the help message", offset, token));
		}
		return proposals;
	}

	@Override
	public void run(ITerminal shell, String command)
	{
		launchInsideShell(shell, command);
	}

}
