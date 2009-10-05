package net.lucky_dip.hamleditor.editor.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import com.aptana.ide.core.StringUtils;
import com.aptana.ide.editor.css.CSSLanguageEnvironment;
import com.aptana.ide.editor.html.HTMLLanguageEnvironment;
import com.aptana.ide.metadata.IMetadataEnvironment;

public class HamlContentAssistantProcessor implements IContentAssistProcessor
{

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
	{
		List<CompletionProposal> res = new ArrayList<CompletionProposal>();
		try
		{
			String prefix = getPrefix(viewer, offset);
			if (prefix != null)
			{
				StringTokenizer tokenizer = new StringTokenizer(prefix, ".#", true);
				String lastToken = "";
				while (tokenizer.hasMoreTokens())
				{
					String currentToken = tokenizer.nextToken();
					if (!currentToken.equals(".") && !currentToken.equals("#"))
						currentToken = lastToken + currentToken;
					lastToken = currentToken;
				}
				if (!lastToken.equals(prefix))
					prefix = lastToken;
			}

			if (prefix != null && prefix.startsWith("%"))
			{
				res.addAll(getHTMLTagCompletions(offset, prefix.substring(1)));
			}
			else if (prefix != null && prefix.startsWith("."))
			{

				res.addAll(getCSSClassCompletions(offset, prefix));
			}
			else if (prefix != null && prefix.startsWith("#"))
			{

				res.addAll(getHTMLIDCompletions(offset, prefix));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return (ICompletionProposal[]) res.toArray(new ICompletionProposal[res.size()]);
	}

	private String getPrefix(ITextViewer viewer, int offset) throws BadLocationException
	{
		IDocument doc = viewer.getDocument();
		if (doc == null || offset > doc.getLength())
			return null;

		int length = 0;
		while (--offset >= 0 && validPrefixChar(doc.getChar(offset)))
			length++;

		return doc.get(offset + 1, length);
	}

	private boolean validPrefixChar(char c)
	{
		return Character.isJavaIdentifierPart(c) || c == '.' || c == '#' || c == '!' || c == '-' || c == '%';
	}

	// FIXME This is code directly copied from SasscontentAssistantProcessor. refactor out common code!
	private Collection<CompletionProposal> getHTMLTagCompletions(int offset, String prefix)
	{
		Collection<CompletionProposal> completionProposals = new ArrayList<CompletionProposal>();
		IMetadataEnvironment environment = (IMetadataEnvironment) HTMLLanguageEnvironment.getInstance()
				.getRuntimeEnvironment();
		String[] em = environment.getAllElements();
		Arrays.sort(em);
		for (String e : em)
		{
			if (e.equalsIgnoreCase("!doctype") || !e.startsWith(prefix))
				continue;
			String replaceString = e;
			Image image = null;
			String displayString = replaceString;
			String additionalPropsalInfo = environment.getElementDocumentation(e);
			completionProposals.add(new CompletionProposal(replaceString, offset - prefix.length(), prefix.length(),
					replaceString.length(), image, displayString, null, additionalPropsalInfo));
		}

		return completionProposals;
	}

	private Collection<CompletionProposal> getCSSClassCompletions(int offset, String prefix)
	{

		Collection<String> classes = CSSLanguageEnvironment.getInstance().getClasses(getEditorContentsPath(), "");
		if (classes == null)
			return Collections.emptyList();
		List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
		for (String cssClass : classes)
		{
			if (StringUtils.EMPTY.equals(cssClass))
				continue;

			String trimmedValue = "." + StringUtils.trimStringQuotes(cssClass);
			if (!trimmedValue.startsWith(prefix))
				continue;
			CompletionProposal cp = new CompletionProposal(trimmedValue, offset - prefix.length(), prefix.length(),
					trimmedValue.length());
			proposals.add(cp);
		}

		return proposals;
	}

	private Collection<CompletionProposal> getHTMLIDCompletions(int offset, String prefix)
	{

		Collection<String> ids = CSSLanguageEnvironment.getInstance().getIds(getEditorContentsPath(), "");
		if (ids == null)
			return Collections.emptyList();

		List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
		for (String id : ids)
		{
			if (StringUtils.EMPTY.equals(id))
				continue;

			String trimmedValue = "#" + StringUtils.trimStringQuotes(id);
			if (!trimmedValue.startsWith(prefix))
				continue;
			CompletionProposal cp = new CompletionProposal(trimmedValue, offset - prefix.length(), prefix.length(),
					trimmedValue.length());
			proposals.add(cp);
		}
		return proposals;
	}

	private String getEditorContentsPath()
	{
		IEditorInput pathEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor().getEditorInput();
		if (pathEditor instanceof IFileEditorInput)
		{
			IFileEditorInput fileEI = (IFileEditorInput) pathEditor;
			return fileEI.getFile().getProject().getFullPath().toPortableString();
		}
		return null;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
	{
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters()
	{
		return new char[] { '%', '.', '#' };
	}

	public char[] getContextInformationAutoActivationCharacters()
	{
		return null;
	}

	public IContextInformationValidator getContextInformationValidator()
	{
		return null;
	}

	public String getErrorMessage()
	{
		return null;
	}

}
