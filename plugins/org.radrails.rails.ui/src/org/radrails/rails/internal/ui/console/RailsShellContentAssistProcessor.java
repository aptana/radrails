package org.radrails.rails.internal.ui.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.radrails.rails.core.RailsLog;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.RubyPluginImages;
import org.rubypeople.rdt.ui.viewsupport.ImageDescriptorRegistry;

import com.aptana.rdt.rake.IRakeHelper;
import com.aptana.rdt.rake.RakePlugin;

public class RailsShellContentAssistProcessor implements IContentAssistProcessor
{

	private IProject fProject;

	public String getErrorMessage()
	{
		return null;
	}

	public IContextInformationValidator getContextInformationValidator()
	{
		return null;
	}

	public char[] getContextInformationAutoActivationCharacters()
	{
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters()
	{
		return new char[] { ' ' };
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
	{
		return null;
	}

	public ICompletionProposal[] computeCompletionProposals(String prefix, int offset)
	{
		List<String> tokens = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(prefix);
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String token = null;
		try
		{
			token = tokenizer.nextToken();
			if (token.trim().startsWith(IRailsShellConstants.PROMPT))
			{
				token = token.trim().substring(IRailsShellConstants.PROMPT.length());
			}
			if (token.trim().length() == 0)
				token = tokenizer.nextToken();
		}
		catch (NoSuchElementException e)
		{
			// ignore
			token = "";
		}

		// Collect the command tokens
		while (token.trim().length() > 0)
		{
			tokens.add(token);
			token = nextToken(tokenizer);
		}

		if (tokens.isEmpty())
		{
			proposals.add(createProposal("debug", "Run the trailing command under the debugger", offset, ""));
			proposals.add(createProposal("profile", "Run the trailing command under the profiler", offset, ""));
		}
		else
		{
			if (tokens.get(0).equals("debug") || tokens.get(0).equals("profile") || tokens.get(0).equals("ruby"))
			{
				tokens.remove(0);
			}
		}

		String command = "";
		if (!tokens.isEmpty())
			command = tokens.get(0);
		List<RailsShellCommandProvider> providers = getCommandProviders();
		for (RailsShellCommandProvider railsShellCommandProvider : providers)
		{
			try
			{
				if (railsShellCommandProvider.projectNeedsToBeSelected() && fProject == null)
					continue;
				if (!commandMatches(railsShellCommandProvider, command))
					continue;
				proposals.addAll(railsShellCommandProvider.getCompletionProposals(prefix, tokens, offset));
			}
			catch (Exception e)
			{
				RailsLog.log(e);
			}
		}
		// Remove null proposals
		Iterator<ICompletionProposal> iter = proposals.iterator();
		while (iter.hasNext())
		{
			ICompletionProposal prop = iter.next();
			if (prop == null)
				iter.remove();
		}

		// Sort proposals by key!
		Collections.sort(proposals, new Comparator<ICompletionProposal>()
		{
			public int compare(ICompletionProposal o1, ICompletionProposal o2)
			{
				return o1.getDisplayString().compareTo(o2.getDisplayString());
			}
		});
		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
	{
		String prefix = getLinePrefix(viewer, offset);
		// If it's the first token and the user typed a space, replace the space too!
		if (prefix.trim().length() == 0)
		{ // just whitespace
			offset -= prefix.length();
		}
		return computeCompletionProposals(prefix, offset);
	}

	private boolean commandMatches(RailsShellCommandProvider railsShellCommandProvider, String command)
	{
		for (String handledCommand : railsShellCommandProvider.commandsHandled())
		{
			if (handledCommand.startsWith(command))
				return true;
		}
		return false;
	}

	private List<RailsShellCommandProvider> getCommandProviders()
	{
		// Run mode doesn't matter here for completions
		return RailsShell.getCommandProviders(fProject, ILaunchManager.RUN_MODE);
	}

	protected IRakeHelper getRakeTasksHelper()
	{
		return RakePlugin.getDefault().getRakeHelper();
	}

	private String nextToken(StringTokenizer tokenizer)
	{
		try
		{
			return tokenizer.nextToken();
		}
		catch (NoSuchElementException e)
		{
			return "";
		}
	}

	private String getLinePrefix(ITextViewer viewer, int offset)
	{
		String prefix = null;
		try
		{
			IDocument doc = viewer.getDocument();
			prefix = doc.get(0, offset);
			int index = prefix.lastIndexOf("\n");
			if (index > -1)
			{
				prefix = prefix.substring(index + 1);
			}
		}
		catch (BadLocationException e)
		{
			// ignore
		}
		return prefix;
	}

	private ICompletionProposal createProposal(String string, String description, int offset, String token)
	{
		if (token != null && !string.startsWith(token))
			return null;
		if (token.equals(string))
			return null;
		ImageDescriptorRegistry registry = RubyPlugin.getImageDescriptorRegistry();
		Image image = registry.get(RubyPluginImages.DESC_MISC_PUBLIC);
		String display = string;
		if (description != null && description.trim().length() > 0)
		{
			display += " - " + description;
		}
		return new RailsShellCompletionProposal(string, offset - token.length(), token.length(), string.length(),
				image, display, null, description);
	}

	public void setProject(IProject project)
	{
		this.fProject = project;
	}

}
