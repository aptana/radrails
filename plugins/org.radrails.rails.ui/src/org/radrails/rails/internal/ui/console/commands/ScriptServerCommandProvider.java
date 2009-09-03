package org.radrails.rails.internal.ui.console.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.radrails.db.core.DatabaseManager;
import org.radrails.db.core.IDatabaseConstants;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.radrails.server.core.IServerConstants;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.rubypeople.rdt.launching.ITerminal;

public class ScriptServerCommandProvider extends RailsShellCommandProvider
{

	@Override
	public Set<String> commandsHandled()
	{
		Set<String> commands = new HashSet<String>();
		commands.add(IRailsShellConstants.SCRIPT_SERVER);
		return commands;
	}

	@Override
	public List<ICompletionProposal> getCompletionProposals(String prefix, List<String> tokens, int offset)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String token = getLastToken(prefix, tokens);
		if (tokens.size() <= 1)
		{
			if (!tokens.contains(IRailsShellConstants.SCRIPT_SERVER))
			{
				proposals.add(createProposal(IRailsShellConstants.SCRIPT_SERVER, "Rails application server", offset,
						token));
			}
		}
		String lastToken = "";
		if (!tokens.isEmpty())
			lastToken = tokens.get(tokens.size() - 1);

		if (lastToken.equals("-e"))
		{
			Set<String> environments = DatabaseManager.getEnvironments();
			for (String env : environments)
			{
				proposals.add(createProposal(env, offset, ""));
			}
			return proposals;
		}

		if (lastToken.equals("-b") || lastToken.equals("-p"))
			return Collections.emptyList();
		if (tokens.size() > 0)
		{
			if (!tokens.contains("-p"))
				proposals.add(createProposal("-p", "Runs Rails on the specified port. Default: 3000", offset, token));
			if (!tokens.contains("-b"))
				proposals.add(createProposal("-b", "Binds Rails to the specified IP. Default: 0.0.0.0", offset, token));
			if (!tokens.contains("-d"))
				proposals.add(createProposal("-d", "Make server run as a daemon", offset, token));
			if (!tokens.contains("-e"))
				proposals.add(createProposal("-e",
						"Specifies the environment to run this server under. Default: development", offset, token));
			if (!tokens.contains("-h"))
				proposals.add(createProposal("-h", "Show the help message", offset, token));
		}
		return proposals;
	}

	@Override
	public void run(ITerminal shell, final String command)
	{
		final Server server = new Server(getProject(), "transient", IServerConstants.TYPE_MONGREL, getHost(command),
				getPort(command), getEnvironment(command))
		{

			@Override
			public String getProgramArguments()
			{
				return getArgs(command);
			}

		};
		ServerManager.getInstance().addServer(server);
		final ILaunch launch = runInNewConsole(shell, command);
		Job job = new Job("Running Rails server from Rails Shell")
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				while (!launch.isTerminated())
				{
					Thread.yield();
					if (monitor.isCanceled())
					{
						server.stop();
						ServerManager.getInstance().removeServer(server);
						return Status.CANCEL_STATUS;
					}
				}
				ServerManager.getInstance().removeServer(server);
				return Status.OK_STATUS;
			}

		};
		job.setSystem(true);
		job.setPriority(Job.DECORATE);
		job.schedule();
	}

	private String getHost(String command)
	{
		return getArg(command, "binding", Server.DEFAULT_RAILS_HOST);
	}

	private String getEnvironment(String command)
	{
		return getArg(command, "environment", IDatabaseConstants.ENV_DEVELOPMENT);
	}

	private String getPort(String command)
	{
		return getArg(command, "port", IServerConstants.DEFAULT_WEBRICK_PORT);
	}

	private String getArg(String command, String full, String defaultValue)
	{
		int index = command.indexOf("--" + full);
		String value = defaultValue;
		if (index != -1)
		{
			value = command.substring(index + 7);
			if (value.indexOf(' ') != -1)
				value = value.substring(0, value.indexOf(' '));
			return value;
		}
		else
		{
			char letter = full.charAt(0);
			index = command.indexOf("-" + letter);
			if (index != -1)
			{
				value = command.substring(index + 3);
				if (value.indexOf(' ') != -1)
					value = value.substring(0, value.indexOf(' '));
				return value;
			}
		}
		return value;
	}

}
