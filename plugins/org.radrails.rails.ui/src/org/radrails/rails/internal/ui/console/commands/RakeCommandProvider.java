package org.radrails.rails.internal.ui.console.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.ITerminal;

import com.aptana.rdt.rake.IRakeHelper;
import com.aptana.rdt.rake.RakePlugin;

public class RakeCommandProvider extends RailsShellCommandProvider
{

	@Override
	public Set<String> commandsHandled()
	{
		Set<String> commands = new HashSet<String>();
		commands.add(IRailsShellConstants.RAKE);
		return commands;
	}

	@Override
	public List<ICompletionProposal> getCompletionProposals(String prefix, List<String> tokens, int offset)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String token = getLastToken(prefix, tokens);
		if (tokens.size() <= 1)
		{
			if (!tokens.contains(IRailsShellConstants.RAKE))
			{
				proposals.add(createProposal(IRailsShellConstants.RAKE, "Run a rake task", offset, token));
			}
		}
		if (tokens.isEmpty() || (tokens.size() == 1 && !prefix.endsWith(" "))
				|| (tokens.size() == 2 && prefix.endsWith(" ")))
			return proposals;
		Map<String, String> tasks = getRakeTasksHelper().getTasks(getProject(), new NullProgressMonitor());
		List<String> taskKeys = new ArrayList<String>(tasks.keySet());
		Collections.sort(taskKeys);
		for (String task : taskKeys)
		{
			proposals.add(createProposal(task, tasks.get(task), offset, token));
		}
		return proposals;
	}

	@Override
	public void run(ITerminal shell, String command)
	{
		String task = command.substring(4).trim();
		int space = task.indexOf(' ');
		String parameters = "";
		if (space > -1)
		{
			parameters = task.substring(space + 1);
			task = task.substring(0, space);
		}

		try
		{
			ILaunchConfigurationWorkingCopy wc = getRakeTasksHelper().run(getProject(), task, parameters)
					.getWorkingCopy();
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_USE_TERMINAL, IRailsShellConstants.TERMINAL_ID);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_TERMINAL_COMMAND, (String) null);
			wc.doSave().launch(getRunMode(), new NullProgressMonitor());
		}
		catch (CoreException e)
		{
			RailsUILog.log(e);
		}
	}

	protected IRakeHelper getRakeTasksHelper()
	{
		return RakePlugin.getDefault().getRakeHelper();
	}

	@Override
	public boolean projectNeedsToBeSelected()
	{
		return true;
	}
}
