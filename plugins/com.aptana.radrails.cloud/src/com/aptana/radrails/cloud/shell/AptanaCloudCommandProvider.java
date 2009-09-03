package com.aptana.radrails.cloud.shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.ITerminal;

import com.aptana.ide.server.cloud.services.model.studio.SiteUtils;
import com.aptana.ide.server.cloud.services.model.studio.StudioSite;
import com.aptana.radrails.cloud.internal.CloudUtil;

public class AptanaCloudCommandProvider extends RailsShellCommandProvider
{

	static final String LIST_TASKS_SWITCH = "-T";
	public static final String APCLOUD = "apcloud";
	public static final String APCLOUDIFY = "apcloudify";

	public static final String STAGING = "staging";
	public static final String PUBLIC = "public";
	public static final String CLOUD_SETUP = "cloud:setup";
	public static final String APTANA_DEPLOY = "cloud:deploy";

	@Override
	public Set<String> commandsHandled()
	{
		Set<String> commands = new HashSet<String>();
		commands.add(APCLOUD);
		commands.add(APCLOUDIFY);
		return commands;
	}

	@Override
	public List<ICompletionProposal> getCompletionProposals(String prefix, List<String> tokens, int offset)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		String token = getLastToken(prefix, tokens);
		// nothing, or first command only
		if (tokens.isEmpty() || (tokens.size() == 1 && !prefix.endsWith(" ")))
		{
			proposals.add(createProposal(APCLOUDIFY,
					"Runs a script to set your rails project up to deploy to the Aptana Cloud", offset, token));
			proposals.add(createProposal(APCLOUD, "Runs commands to deploy your rails project to the Aptana Cloud",
					offset, token));
			return proposals;
		}

		// second or later command
		// TODO Grab the list of tasks and offer them up, just like we do with Rake
		if (tokens.contains(APCLOUD))
		{
			Map<String, String> tasks = new ApCloud().getTasks(getProject(), false);

			if (!containsArg(tokens, tasks.keySet()) && !tokens.contains(LIST_TASKS_SWITCH))
			{
				if (!tokens.contains(PUBLIC) && !tokens.contains(STAGING))
				{
					// Only add non-deploy tasks if user hasn't specified public/staging
					for (Map.Entry<String, String> entry : tasks.entrySet())
					{
						if (!entry.getKey().contains("deploy"))
						{
							proposals.add(createProposal(entry.getKey(), entry.getValue(), offset, token));
						}
					}
					proposals.add(createProposal(LIST_TASKS_SWITCH, "List all the available tasks", offset, token));
				}
				else
				{ // Add tasks containing "deploy" after user adds "public/staging"
					for (Map.Entry<String, String> entry : tasks.entrySet())
					{
						// Add tasks with "deploy" in their names
						if (entry.getKey().contains("deploy"))
						{
							proposals.add(createProposal(entry.getKey(), entry.getValue(), offset, token));
						}
					}
				}
			}
		}
		return proposals;
	}

	private boolean containsArg(List<String> tokens, Set<String> tasks)
	{
		for (String token : tokens)
		{
			for (String task : tasks)
			{
				if (task.equalsIgnoreCase(PUBLIC) || task.equalsIgnoreCase(STAGING))
					continue;
				if (token.equals(task))
					return true;
			}
		}
		return false;
	}

	@Override
	public void run(ITerminal shell, String command)
	{
		if (!command.startsWith(APCLOUDIFY) && !command.startsWith(APCLOUD))
		{
			return;
		}

		// Check if they have the necessary gem installed!
		IStatus result = installCloudGemIfNecessary();
		// if we failed to determine, or failed to install, the gem then don't try to run the command.
		if (!result.isOK())
			return;
		if (command.startsWith(APCLOUDIFY))
		{
			launchApCloudify(shell, command);
		}
		else if (command.startsWith(APCLOUD))
		{
			launchInsideShell(shell, command, getEnvMap());
		}
	}

	protected IStatus installCloudGemIfNecessary()
	{
		return CloudUtil.installCloudGemIfNecessary(null);
	}

	private void launchApCloudify(ITerminal shell, String command)
	{
		// They gave a path, it looks like...
		if (command.trim().length() > APCLOUDIFY.length())
		{
			String path = command.trim().substring(APCLOUDIFY.length()).trim();
			if (path.equals("."))
			{
				// Assume active project
				IProject project = getProject();
				launchInsideShell(shell, APCLOUDIFY + " " + getProjectArg(project), getEnvMap(), refreshAttrMap());
				return;
			}

			// Hope it's a project name, then use full path to project and switch shell's active project to it.
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path);
			if (project != null && project.exists())
			{
				launchInsideShell(shell, APCLOUDIFY + " " + getProjectArg(project), getEnvMap(), refreshAttrMap());
				shell.setProject(project);
				return;
			}
			// Hmm, just try and interpret command as it is.
			launchInsideShell(shell, command, getEnvMap());
			return;
		}

		// No path, assume they want it on active project
		IProject project = getProject();
		launchInsideShell(shell, APCLOUDIFY + " " + getProjectArg(project), getEnvMap(), refreshAttrMap());
	}

	private String getProjectArg(IProject project)
	{
		String result = project.getLocation().toOSString();
		// Wrap in quotes if there's any whitespace!
		if (result.indexOf(' ') == -1)
			return result;
		return '"' + result + '"';
	}

	private Map<String, Object> refreshAttrMap()
	{
		Map<String, Object> attrMap = new HashMap<String, Object>();
		attrMap.put(IRubyLaunchConfigurationConstants.ATTR_REQUIRES_REFRESH, true);
		return attrMap;
	}

	private Map<String, String> getEnvMap()
	{
		IProject project = getProject();
		if (project == null)
			return null;
		StudioSite site = SiteUtils.getSite(project);
		return CloudUtil.getEnvMap(site);
	}

}
