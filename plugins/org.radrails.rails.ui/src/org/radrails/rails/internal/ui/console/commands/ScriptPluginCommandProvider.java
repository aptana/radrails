package org.radrails.rails.internal.ui.console.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.radrails.rails.core.railsplugins.RailsPluginsManager;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.ITerminal;

public class ScriptPluginCommandProvider extends RailsShellCommandProvider
{

	@Override
	public Set<String> commandsHandled()
	{
		Set<String> commands = new HashSet<String>();
		commands.add(IRailsShellConstants.SCRIPT_PLUGIN);
		return commands;
	}

	@Override
	public List<ICompletionProposal> getCompletionProposals(String prefix, List<String> tokens, int offset)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String token = getLastToken(prefix, tokens);
		if (tokens.size() <= 1)
		{
			if (!tokens.contains(IRailsShellConstants.SCRIPT_PLUGIN))
			{
				proposals.add(createProposal(IRailsShellConstants.SCRIPT_PLUGIN, "Rails plugin manager", offset, token));
			}
		}
		if (tokens.size() == 1 || (tokens.size() == 2 && !prefix.endsWith(" ")))
		{
			proposals.add(createProposal("discover", "Discover plugin repositories", offset, token));
			proposals.add(createProposal("list", "List available plugins", offset, token));
			proposals
					.add(createProposal("install", "Install plugin(s) from known repositories or URLs", offset, token));
			proposals.add(createProposal("update", "Update installed plugins", offset, token));
			proposals.add(createProposal("remove", "Uninstall plugins", offset, token));
			proposals.add(createProposal("source", "Add a plugin source repository", offset, token));
			proposals.add(createProposal("unsource", "Remove a plugin repository", offset, token));
			proposals.add(createProposal("sources", "List currently configured plugin repositories", offset, token));
		}
		return proposals;
	}

	@Override
	public void run(ITerminal shell, String command)
	{
		command = command.substring(IRailsShellConstants.SCRIPT_PLUGIN.length()).trim();
		RailsPluginsManager.run(getProject(), command);
	}

}
