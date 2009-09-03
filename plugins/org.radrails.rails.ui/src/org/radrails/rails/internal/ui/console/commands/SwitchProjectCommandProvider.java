package org.radrails.rails.internal.ui.console.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.ITerminal;

public class SwitchProjectCommandProvider extends RailsShellCommandProvider
{

	@Override
	public Set<String> commandsHandled()
	{
		Set<String> commands = new HashSet<String>();
		commands.add(IRailsShellConstants.CD);
		commands.add(IRailsShellConstants.SWITCH);
		return commands;
	}

	@Override
	public List<ICompletionProposal> getCompletionProposals(String prefix, List<String> tokens, int offset)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		if (tokens.size() <= 1)
		{
			String token = "";
			// if there's one token and no space, check token and provide cd/switch if prefix matches
			if (tokens.size() == 1 && !prefix.endsWith(" "))
			{
				token = tokens.get(0);
			}
			proposals.add(createProposal(IRailsShellConstants.CD, "Alias for switch", offset, token));
			proposals.add(createProposal(IRailsShellConstants.SWITCH, "Switch current project", offset, token));
		}
		
		// if there's one token and no space, check token and provide cd/switch if prefix matches
		if (tokens.size() == 1 && !prefix.endsWith(" "))
		{
			String token = tokens.get(0);
			
		}

		if (tokens.size() == 1 || (tokens.size() == 2 && !prefix.endsWith(" ")))
		{
			String token = getLastToken(prefix, tokens);
			Set<IProject> projects = RailsPlugin.getRailsProjects();
			for (IProject project : projects)
			{
				proposals.add(createProposal(project.getName(), offset, token));
			}
		}
		return proposals;
	}

	@Override
	public void run(ITerminal shell, String command)
	{
		StringTokenizer tokenizer = new StringTokenizer(command);
		tokenizer.nextToken(); // cd or switch
		try
		{
			String projectName = tokenizer.nextToken();
			IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (newProject != null && newProject.exists())
			{
				shell.setProject(newProject);
				shell.write(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM, "Switched current working project to "
						+ projectName);
			}
			else
			{
				shell.write(IDebugUIConstants.ID_STANDARD_ERROR_STREAM, "No such project: " + projectName);
			}
		}
		catch (RuntimeException e)
		{
			shell.write(IDebugUIConstants.ID_STANDARD_ERROR_STREAM, "Must specify project name as second argument");
		}
		finally
		{
			shell.write(IDebugUIConstants.ID_STANDARD_INPUT_STREAM, "\n" + IRailsShellConstants.PROMPT);
		}
	}

}
