package net.lucky_dip.sasseditor.editor.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.lucky_dip.hamleditor.HTMLCSSKeywords;
import net.lucky_dip.sasseditor.editor.SassEditor;
import net.lucky_dip.sasseditor.editor.scanners.SassPartitionScanner;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
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

public class SassContentAssistantProcessor implements IContentAssistProcessor
{

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

	private String getPrefix(ITextViewer viewer, int offset) throws BadLocationException
	{
		IDocument doc = viewer.getDocument();
		if (doc == null || offset > doc.getLength())
			return null;

		int length = 0;
		while (--offset >= 0
				&& (Character.isJavaIdentifierPart(doc.getChar(offset)) || doc.getChar(offset) == '.'
						|| doc.getChar(offset) == '#' || doc.getChar(offset) == '!' || doc.getChar(offset) == '-'))
			length++;

		return doc.get(offset + 1, length);
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
	{
		IDocument doc = viewer.getDocument();
		List<CompletionProposal> res = new ArrayList<CompletionProposal>();

		try
		{
			ITypedRegion region = null;
			if (doc instanceof IDocumentExtension3)
				region = ((IDocumentExtension3) doc).getPartition(SassEditor.SASS_PARTITIONING, offset > 0 ? offset - 1
						: 0, false);
			String prefix = getPrefix(viewer, offset);
			if (prefix != null && prefix.startsWith("."))
			{
				res.addAll(getCSSClassCompletions(offset, prefix));
			}
			else if (prefix != null && prefix.startsWith("#"))
			{
				res.addAll(getHTMLIDCompletions(offset, prefix));
			}
			else if (region != null && region.getType().equals(SassPartitionScanner.SASS_ATTRIBUTE))
			{
				res.addAll(getCSSPropertyCompletions(offset, prefix));
			}
			else if (region != null && region.getType().equals(SassPartitionScanner.SASS_CONSTANT))
			{
				res.addAll(getSassVariableCompletions(doc, offset, prefix));
			}
			else
			{
				// We may be trying to complete an html tag, or a CSS psuedo class, or a CSS attribute name!
				// FIXME See if we can narrow this down any by indentation!
				// FIXME This also may be completing the value for a CSS property here (variables or actual normal values)!
				res.addAll(getHTMLTagCompletions(offset, prefix));
				res.addAll(getCSSPropertyCompletions(offset, prefix));
				res.addAll(getCSSPsuedoClassCompletions(offset, prefix));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return (ICompletionProposal[]) res.toArray(new ICompletionProposal[res.size()]);
	}

	private Collection<? extends CompletionProposal> getCSSPsuedoClassCompletions(int offset, String prefix)
	{
		Collection<CompletionProposal> res = new ArrayList<CompletionProposal>();
		for (String pseudoClass : HTMLCSSKeywords.CSS_PSEUDO_CLASSES)
		{
			if (!pseudoClass.startsWith(prefix))
				continue;
			CompletionProposal cp = new CompletionProposal(pseudoClass, offset - prefix.length(), prefix.length(),
					pseudoClass.length());
			res.add(cp);
		}
		return res;
	}

	private Collection<CompletionProposal> getCSSPropertyCompletions(int offset, String prefix)
	{
		Collection<CompletionProposal> res = new ArrayList<CompletionProposal>();
		List<String> matches = new ArrayList<String>(HTMLCSSKeywords.getCssAttributeMatches(prefix, offset));
		Collections.sort(matches);
		for (String match : matches)
		{
			// Add colons and space after (but not in display string)
			String replacementString = match + ": ";
			CompletionProposal cp = new CompletionProposal(replacementString, offset - prefix.length(),
					prefix.length(), replacementString.length(), null, match, null, null);
			res.add(cp);
		}
		return res;
	}

	private Collection<CompletionProposal> getHTMLTagCompletions(int offset, String prefix)
	{
		// FIXME Hooking up to the CSS environment, we get back 40 tags, whereas hooking up to HTML we get 105!
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
		// return HTMLCSSKeywords.getHtmlTagMatches(prefix, offset, region);
	}

	private Collection<? extends CompletionProposal> getSassVariableCompletions(IDocument doc, int offset, String prefix)
			throws BadLocationException, BadPartitioningException
	{
		List<CompletionProposal> res = new ArrayList<CompletionProposal>();
		ITypedRegion[] regions = null;
		if (doc instanceof IDocumentExtension3)
			regions = ((IDocumentExtension3) doc).computePartitioning(SassEditor.SASS_PARTITIONING, 0, doc.getLength(),
					false);
		String[] variableNames = getConstants(doc, regions);
		for (String cname : variableNames)
		{
			String replacementString = "!" + cname;
			if (replacementString.equals(prefix) || !replacementString.startsWith(prefix))
				continue;
			String displayString = cname;
			CompletionProposal cp = new CompletionProposal(replacementString, offset - prefix.length(),
					prefix.length(), replacementString.length(), null, displayString, null, null);
			res.add(cp);
		}
		return res;
	}

	private Collection<CompletionProposal> getHTMLIDCompletions(int offset, String prefix)
	{
		List<CompletionProposal> res = new ArrayList<CompletionProposal>();
		Collection<String> ids = CSSLanguageEnvironment.getInstance().getIds(getEditorContentsPath(), "");
		if (ids != null)
		{
			for (String e : ids)
			{
				if (StringUtils.EMPTY.equals(e))
					continue;

				String trimmedValue = "#" + StringUtils.trimStringQuotes(e);
				CompletionProposal cp = new CompletionProposal(trimmedValue, offset - prefix.length(), prefix.length(),
						trimmedValue.length());
				res.add(cp);
			}
		}
		return res;
	}

	private Collection<CompletionProposal> getCSSClassCompletions(int offset, String prefix)
	{
		List<CompletionProposal> res = new ArrayList<CompletionProposal>();
		Collection<String> classes = CSSLanguageEnvironment.getInstance().getClasses(getEditorContentsPath(), "");
		if (classes != null)
		{
			for (String e : classes)
			{
				if (StringUtils.EMPTY.equals(e))
					continue;

				String trimmedValue = "." + StringUtils.trimStringQuotes(e);
				CompletionProposal cp = new CompletionProposal(trimmedValue, offset - prefix.length(), prefix.length(),
						trimmedValue.length());
				res.add(cp);
			}
		}
		return res;
	}

	private String[] getConstants(IDocument document, ITypedRegion[] regions)
	{
		if (regions == null)
			return new String[0];
		List<String> res = new ArrayList<String>();
		for (int i = 0; i < regions.length; i++)
		{
			if (regions[i].getType().equals(SassPartitionScanner.SASS_CONSTANT))
			{
				try
				{
					int offset = regions[i].getOffset() + 1;
					int length = regions[i].getLength() - 1;
					if (length > 0)
					{
						String constant = document.get(offset, length);
						if (!res.contains(constant))
						{
							res.add(constant);
						}
					}
				}
				catch (BadLocationException e)
				{
					e.printStackTrace();
				}
			}
		}
		Collections.sort(res);
		return res.toArray(new String[res.size()]);
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
	{
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters()
	{
		return null;
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
