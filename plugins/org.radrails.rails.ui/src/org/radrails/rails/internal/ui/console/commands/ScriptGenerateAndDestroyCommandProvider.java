package org.radrails.rails.internal.ui.console.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.radrails.rails.internal.generators.Generator;
import org.radrails.rails.internal.generators.GeneratorLocatorsManager;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.ITerminal;

public class ScriptGenerateAndDestroyCommandProvider extends RailsShellCommandProvider
{

	@Override
	public Set<String> commandsHandled()
	{
		Set<String> commands = new HashSet<String>();
		commands.add(IRailsShellConstants.SCRIPT_DESTROY);
		commands.add(IRailsShellConstants.SCRIPT_GENERATE);
		return commands;
	}

	@Override
	public List<ICompletionProposal> getCompletionProposals(String prefix, List<String> tokens, int offset)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String token = getLastToken(prefix, tokens);
		if (tokens.size() <= 1)
		{
			if (!tokens.contains(IRailsShellConstants.SCRIPT_DESTROY)
					&& !tokens.contains(IRailsShellConstants.SCRIPT_GENERATE))
			{
				proposals.add(createProposal(IRailsShellConstants.SCRIPT_DESTROY,
						"Destroy files which were created by a generator", offset, token));
				proposals.add(createProposal(IRailsShellConstants.SCRIPT_GENERATE,
						"Create files using the specified generator", offset, token));
			}
		}
		if (tokens.size() > 1)
		{
			if (!tokens.contains("-h"))
			{
				proposals.add(createProposal("-h", "Show the help message", offset, token));
			}
			if (tokens.size() <= 2 && !token.startsWith("-")) // already selected a generator
			{
				List<Generator> generators = GeneratorLocatorsManager.getInstance().getAllGenerators(getProject());
				Collections.sort(generators);
				for (Generator generator : generators)
				{ // TODO Grab the descriptions for these generators
					ICompletionProposal proposal = createProposal(generator.getName(), offset, token);
					if (proposal != null)
						proposals.add(proposal);
				}
			}
		}
		return proposals;
	}

	@Override
	public void run(ITerminal shell, String command)
	{
		Map<String, Object> attrs = new HashMap<String, Object>();
		attrs.put(IRubyLaunchConfigurationConstants.ATTR_REQUIRES_REFRESH, true);
		launchInsideShell(shell, command, null, attrs);
	}

}
