/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL youelect, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ide.editor.erb.contentassist;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;
import org.rubypeople.rdt.internal.ui.rubyeditor.RubyEditorMessages;

import com.aptana.ide.core.IdeLog;
import com.aptana.ide.editor.erb.ERBPlugin;
import com.aptana.ide.editors.unified.contentassist.ICompletionProposalContributor;
import com.aptana.ide.lexer.Lexeme;
import com.aptana.ide.lexer.LexemeList;

/**
 * @author
 */
public class ERBTemplateCompletionProposalContributor extends TemplateCompletionProcessor implements
		ICompletionProposalContributor
{

	/**
	 * @see com.aptana.ide.editors.unified.contentassist.ICompletionProposalContributor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer,
	 *      int, int, com.aptana.ide.lexer.LexemeList, char, char)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset, int position,
			LexemeList lexemeList, char activationChar, char previousChar)
	{
		return this.computeCompletionProposals(viewer, offset, position, lexemeList, activationChar, previousChar,
				false);
	}

	/**
	 * @see com.aptana.ide.editors.unified.contentassist.ICompletionProposalContributor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer,
	 *      int, int, com.aptana.ide.lexer.LexemeList, char, char, boolean)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset, int position,
			LexemeList lexemeList, char activationChar, char previousChar, boolean autoActivated)
	{
		// TODO Push up code so we never get here. In the HTML processor, it should check the auto activation characters
		// of each child if we're auto activated, and only ask those with a match to the activation char
		if (autoActivated)
			return new ICompletionProposal[0];
		Lexeme lex = lexemeList.getLexemeFromOffset(offset);
		if (lex != null && lex.getText().equals("\"\""))
			return new ICompletionProposal[0];
		return this.computeCompletionProposals(viewer, offset);
	}

	private static final Image templateImage = ERBPlugin.imageDescriptorFromPlugin("com.aptana.ide.editor.erb",
			"icons/rails.gif").createImage();

	private static final char[] COMPLETION_CHARS = { '.' };

	/**
	 * proposalList
	 */
	protected Vector proposalList = new Vector();

	/**
	 * validator
	 */
	protected IContextInformationValidator validator = new Validator();

	private ITextViewer viewer;

	/**
	 * Simple content assist tip closer. The tip is valid in a range of 5 characters around its popup location.
	 */
	protected static class Validator implements IContextInformationValidator, IContextInformationPresenter
	{

		/**
		 * fInstallOffset
		 */
		protected int fInstallOffset;

		/**
		 * @see IContextInformationValidator#isContextInformationValid(int)
		 */
		public boolean isContextInformationValid(int offset)
		{
			return Math.abs(fInstallOffset - offset) < 5;
		}

		/**
		 * @see IContextInformationValidator#install(IContextInformation, ITextViewer, int)
		 */
		public void install(IContextInformation info, ITextViewer viewer, int offset)
		{
			fInstallOffset = offset;
		}

		/**
		 * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int,
		 *      TextPresentation)
		 */
		public boolean updatePresentation(int documentPosition, TextPresentation presentation)
		{
			return false;
		}
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer,
	 *      int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
	{
		this.viewer = viewer;
		WordPartDetector wordPart = new WordPartDetector(viewer, documentOffset);

		return turnProposalVectorIntoAdaptedArray(wordPart, viewer, documentOffset);
	}

	/**
	 * Turns the vector into an Array of ICompletionProposal objects
	 * 
	 * @param word
	 * @param viewer
	 * @param documentOffset
	 * @return An array of ICompletionProposals based on the list.
	 */
	protected ICompletionProposal[] turnProposalVectorIntoAdaptedArray(WordPartDetector word, ITextViewer viewer,
			int documentOffset)
	{

		ICompletionProposal[] templates = determineTemplateProposals(viewer, documentOffset);

		return templates;
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer,
	 *      int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
	{
		IContextInformation[] result = new IContextInformation[5];
		for (int i = 0; i < result.length; i++)
			result[i] = new ContextInformation(MessageFormat.format(RubyEditorMessages.getResourceBundle().getString(
					"CompletionProcessor.ContextInfo.display.pattern"), new Object[] { new Integer(i),
					new Integer(offset) }), //$NON-NLS-1$
					MessageFormat.format(RubyEditorMessages.getResourceBundle().getString(
							"CompletionProcessor.ContextInfo.value.pattern"), //$NON-NLS-1$
							new Object[] { new Integer(i), new Integer(offset - 5), new Integer(offset + 5) }));
		return result;
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters()
	{
		return COMPLETION_CHARS;
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters()
	{
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage()
	{
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator()
	{
		return validator;
	}

	/**
	 * getPrefix
	 * 
	 * @param doc
	 * @param offset
	 * @return String
	 */
	protected String getPrefix(IDocument doc, int offset)
	{
		int length = 0;
		String prefix = null;
		try
		{
			while ((offset - length > 0) && Pattern.matches("\\w", doc.get(offset - length - 1, 1)))
			{
				length++;
			}
			prefix = doc.get(offset - length, length);
		}
		catch (BadLocationException e)
		{
			IdeLog.logError(ERBPlugin.getDefault(), "Error gathering proposal prefix", e);
		}
		return prefix;
	}

	/**
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#extractPrefix(org.eclipse.jface.text.ITextViewer,
	 *      int)
	 */
	protected String extractPrefix(ITextViewer viewer, int offset)
	{
		int i = offset;
		IDocument document = viewer.getDocument();
		if (i > document.getLength())
		{
			return ""; //$NON-NLS-1$
		}

		try
		{
			while (i > 0)
			{
				char ch = document.getChar(i - 1);
				if (Character.isJavaIdentifierPart(ch))
				{
					i--;
					String pref = null;
					if (i - 4 > 0)
					{
						pref = document.get(i - 4, 3);
						if (pref.equals("<%="))
						{
							i -= 3;
						}
						else if (pref.endsWith("<%"))
						{
							pref = document.get(i - 3, 2);
							i -= 2;
						}
					}
					else if (i - 3 > 0)
					{
						pref = document.get(i - 3, 2);
						if (pref.endsWith("<%"))
						{
							i -= 2;
						}
					}
				}
				else if (ch == '=')
				{
					i--;
				}
				else if (ch == '%')
				{
					i--;
				}
				else if (ch == '<')
				{
					i--;
				}
				else
				{
					break;
				}
			}
			return document.get(i, offset - i);
		}
		catch (BadLocationException e)
		{
			return ""; //$NON-NLS-1$
		}
	}

	private ICompletionProposal[] determineTemplateProposals(ITextViewer refViewer, int documentOffset)
	{
		String prefix = getPrefix(viewer.getDocument(), documentOffset);
		ICompletionProposal[] matchingTemplateProposals;
		if (prefix.length() == 0)
		{
			matchingTemplateProposals = super.computeCompletionProposals(refViewer, documentOffset);
		}
		else
		{
			ICompletionProposal[] templateProposals = super.computeCompletionProposals(refViewer, documentOffset);
			List<ICompletionProposal> templateProposalList = new ArrayList<ICompletionProposal>(
					templateProposals.length);
			for (int i = 0; i < templateProposals.length; i++)
			{
				if (templateProposals[i].getDisplayString().toLowerCase().startsWith(prefix))
				{
					templateProposalList.add(templateProposals[i]);
				}
			}
			matchingTemplateProposals = templateProposalList.toArray(new ICompletionProposal[templateProposalList
					.size()]);
		}
		Arrays.sort(matchingTemplateProposals, new TemplateProposalComparator());
		return matchingTemplateProposals;
	}

	/**
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getTemplates(java.lang.String)
	 */
	protected Template[] getTemplates(String contextTypeId)
	{
		return RhtmlTemplateManager.getDefault().getTemplateStore().getTemplates();
	}

	/**
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getContextType(org.eclipse.jface.text.ITextViewer,
	 *      org.eclipse.jface.text.IRegion)
	 */
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region)
	{
		return RhtmlTemplateManager.getDefault().getContextTypeRegistry().getContextType(RhtmlContextType.ID);
	}

	/**
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getImage(org.eclipse.jface.text.templates.Template)
	 */
	protected Image getImage(Template template)
	{
		return templateImage;
	}

	// ////////////////////////////////////////////////DirAlphaComparator
	// To sort directories before files, then alphabetically.
	class TemplateProposalComparator implements Comparator<ICompletionProposal>
	{

		/**
		 * Comparator interface requires defining compare method.
		 * 
		 * @param filea
		 * @param fileb
		 * @return int
		 */
		public int compare(ICompletionProposal filea, ICompletionProposal fileb)
		{
			// ... Sort directories before files,
			// otherwise alphabetical ignoring case.
			return filea.getDisplayString().compareToIgnoreCase(fileb.getDisplayString());
		}
	}

}
