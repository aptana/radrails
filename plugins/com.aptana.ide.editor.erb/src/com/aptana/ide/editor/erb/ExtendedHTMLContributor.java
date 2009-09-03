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
package com.aptana.ide.editor.erb;

import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.rubypeople.rdt.internal.ui.text.RubyDoubleClickSelector;

import com.aptana.ide.editor.erb.contentassist.ERBTemplateCompletionProposalContributor;
import com.aptana.ide.editor.erb.parsing.ERBMimeType;
import com.aptana.ide.editor.html.HTMLContributor;
import com.aptana.ide.editor.html.contentassist.HTMLContentAssistProcessor;
import com.aptana.ide.editors.unified.IUnifiedEditorContributor;

/**
 * @author Kevin Lindsey
 */
public class ExtendedHTMLContributor extends HTMLContributor
{
	private IEditorPart editor;

	/**
	 * ExtendedHTMLContributor
	 * @param editor 
	 */
	public ExtendedHTMLContributor(IEditorPart editor)
	{
		super();
		this.editor = editor;
	}

	/**
	 * @see com.aptana.ide.editor.html.HTMLContributor#getChildContributors()
	 */
	public IUnifiedEditorContributor[] getChildContributors()
	{
		if (this.childContributors == null)
		{
			// get superclasses contribute list
			IUnifiedEditorContributor[] childContributors = super.getChildContributors();
			
			// make a new list with room for our contributers
			this.childContributors = new IUnifiedEditorContributor[childContributors.length + 1];
			
			// copy superclass list
			System.arraycopy(childContributors, 0, this.childContributors, 0, childContributors.length);
			
			// create our contributer
			ERBContributor contributor = new ERBContributor(editor);
			
			// associate contributer
			contributor.setParent(this);
			
			// add our contributer
			this.childContributors[this.childContributors.length - 1] = contributor;
		}
		
		return this.childContributors;
	}
	
	/**
	 * @see com.aptana.ide.editors.unified.IUnifiedEditorContributor#getLocalContentAssistProcessor(org.eclipse.jface.text.source.ISourceViewer,
	 *      java.lang.String)
	 */
	public IContentAssistProcessor getLocalContentAssistProcessor(ISourceViewer sourceViewer, String contentType)
	{
		IContentAssistProcessor processor = super.getLocalContentAssistProcessor(sourceViewer, contentType);
		
		if (processor instanceof HTMLContentAssistProcessor)
		{
			HTMLContentAssistProcessor htmlProcessor = (HTMLContentAssistProcessor)processor;
			
			htmlProcessor.addCompletionProposalContributor(new ERBTemplateCompletionProposalContributor());
		}
		
		return processor;
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		if (contentType.equals(ERBMimeType.MimeType)) {
			return new RubyDoubleClickSelector();
		}
		return super.getDoubleClickStrategy(sourceViewer, contentType);
	}
}
