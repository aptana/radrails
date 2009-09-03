package org.radrails.rails.internal.ui.console.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.ITerminal;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.IGemManager;

public class GemCommandProvider extends RailsShellCommandProvider
{

	private static final String COMMAND = IRailsShellConstants.GEM;

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
			if (!tokens.contains(IRailsShellConstants.GEM))
			{
				proposals.add(createProposal(IRailsShellConstants.GEM, "Run the rubygems utility", offset, token));
			}
		}
		if (tokens.isEmpty())
			return proposals;
		int nonSwitchTokens = getNonSwitchTokenCount(tokens);
		if (nonSwitchTokens == 1 || (nonSwitchTokens == 2 && !prefix.endsWith(" "))) // they've typed "gem", or are
		// typing something after "gem "
		{
			proposals.add(createProposal("install", "Install a gem", offset, token));
			proposals.add(createProposal("uninstall", "Remove a gem", offset, token));
			proposals.add(createProposal("update", "Update gem(s)", offset, token));
			proposals.add(createProposal("cleanup", "Remove old versions of gems", offset, token));
			proposals.add(createProposal("sources",
					"Manage the sources and cache file RubyGems uses to search for gems", offset, token));
		}
		if (subCommandIs(tokens, "sources"))
		{
			Map<String, String> switches = new HashMap<String, String>();
			switches.put("-a", "Add source");
			switches.put("-l", "List sources");
			switches.put("-r", "Remove source");
			switches.put("-c", "Remove all the sources (clear cache)");
			switches.put("-u", "Update source cache");
			proposals.addAll(createIncompatibleSwitches(switches, tokens, offset, token));
		}
		else if ((subCommandIs(tokens, "update") || subCommandIs(tokens, "uninstall"))
				&& ((nonSwitchTokens < 3) || (nonSwitchTokens == 3 && token.length() > 1)))
		{
			Set<Gem> localGems = AptanaRDTPlugin.getDefault().getGemManager().getGems();
			Set<String> gemNames = new HashSet<String>();
			for (Gem gem : localGems)
			{
				gemNames.add(gem.getName());
			}
			for (String gemName : gemNames)
			{
				proposals.add(createProposal(gemName, offset, token));
			}
		}
		else if (!tokens.contains("-l") && !tokens.contains("-r"))
		{
			proposals.add(createProposal("-l", "Local operation", offset, token));
			proposals.add(createProposal("-r", "Remote operations", offset, token));
		}
		if (!tokens.contains("-h"))
			proposals.add(createProposal("-h", "Show help and quit", offset, token));
		return proposals;
	}

	private Collection<? extends ICompletionProposal> createIncompatibleSwitches(Map<String, String> switches,
			List<String> tokens, int offset, String token)
	{
		Collection<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (Map.Entry<String, String> switchEntry : switches.entrySet())
		{
			if (tokens.contains(switchEntry.getKey()))
				return Collections.emptyList();
			ICompletionProposal proposal = createProposal(switchEntry.getKey(), switchEntry.getValue(), offset, token);
			if (proposal != null)
				proposals.add(proposal);
		}
		return proposals;
	}

	private boolean subCommandIs(List<String> tokens, String subCommand)
	{
		if (tokens == null || tokens.isEmpty())
			return false;
		String actualSubCommand = getActualSubCommand(tokens);
		if (actualSubCommand == null)
			return false;
		return actualSubCommand.equals(subCommand);
	}

	private String getActualSubCommand(List<String> tokens)
	{
		boolean takeNextNonSwitchToken = false;
		for (String token : tokens)
		{
			if (takeNextNonSwitchToken && !token.startsWith("-"))
				return token;
			if (token.equals(COMMAND))
			{
				takeNextNonSwitchToken = true;
			}
		}
		return null;
	}

	private int getNonSwitchTokenCount(List<String> tokens)
	{
		int count = 0;
		for (String string : tokens)
		{
			if (string.startsWith("-"))
				continue;
			count++;
		}
		return count;
	}

	@Override
	public void run(final ITerminal shell, final String command)
	{
		final IGemManager gemManager = RailsPlugin.getInstance().getGemManager();
		// just run the command straight up
		Job job = new Job(command)
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					ILaunchConfiguration config = gemManager.run(getArgs(command));
					if (monitor.isCanceled())
					{
						return Status.CANCEL_STATUS;
					}
					ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
					wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_USE_TERMINAL,
							IRailsShellConstants.TERMINAL_ID);
					wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_TERMINAL_COMMAND, (String) null);
					config = wc.doSave();
					final ILaunch launch = config.launch(getRunMode(), monitor);
					while (!launch.isTerminated())
					{
						if (monitor.isCanceled())
						{
							launch.terminate();
							return Status.CANCEL_STATUS;
						}
						Thread.yield();
					}
					gemManager.refresh(monitor);
					monitor.done();
					return Status.OK_STATUS;
				}
				catch (CoreException e)
				{
					return e.getStatus();
				}
			}

		};
		job.schedule();
	}

}
