package org.radrails.rails.internal.ui.console.commands;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.ITerminal;

public class GenericRubyLaunchCommandProvider extends RailsShellCommandProvider
{

	@Override
	public Set<String> commandsHandled()
	{
		return Collections.emptySet();
	}

	@Override
	public boolean handlesAll()
	{
		return true;
	}

	@Override
	public List<ICompletionProposal> getCompletionProposals(String prefix, List<String> tokens, int offset)
	{
		return Collections.emptyList();
	}

	@Override
	public void run(ITerminal shell, String command)
	{
		launchInsideShell(shell, command);
	}

}
