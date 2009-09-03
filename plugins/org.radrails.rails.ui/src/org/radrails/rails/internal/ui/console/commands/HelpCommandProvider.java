package org.radrails.rails.internal.ui.console.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.internal.ui.console.Messages;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.ITerminal;

public class HelpCommandProvider extends RailsShellCommandProvider
{
	private static final String COMMAND = "help";

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
			if (!tokens.contains(COMMAND))
			{
				proposals.add(createProposal(COMMAND, "Show information about the available commands", offset, token));
			}
		}
		return proposals;
	}

	@Override
	public void run(ITerminal shell, String command)
	{
		// FIXME Check out all the available commands and build the help dynamically.
		shell.write(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM, Messages.RailsShell_HelpText + "\n");
		shell.write(IDebugUIConstants.ID_STANDARD_INPUT_STREAM, IRailsShellConstants.PROMPT);
	}

}