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

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.text.ruby.hover.RubyEditorTextHoverDescriptor;
import org.rubypeople.rdt.internal.ui.text.ruby.hover.RubyEditorTextHoverProxy;

import com.aptana.ide.editor.erb.contentassist.ERBContentAssistProcessor;
import com.aptana.ide.editor.erb.parsing.ERBMimeType;
import com.aptana.ide.editors.preferences.IPreferenceConstants;
import com.aptana.ide.editors.unified.BaseContributor;
import com.aptana.ide.editors.unified.EditorFileContext;
import com.aptana.ide.editors.unified.LanguageRegistry;
import com.aptana.ide.editors.unified.UnifiedReconcilingStrategy;
import com.aptana.ide.editors.unified.colorizer.LanguageColorizer;

/**
 * @author Kevin Lindsey
 */
public class ERBContributor extends BaseContributor
{
	private LanguageColorizer _colorizer;
	private ERBContentAssistProcessor _caProcessor;
	private IEditorPart editor;

	/**
	 * ERBContributer
	 * @param editor 
	 */
	public ERBContributor(IEditorPart editor)
	{
		super(ERBMimeType.MimeType);

		this._colorizer = LanguageRegistry.getLanguageColorizer(ERBMimeType.MimeType);
		this._colorizer = (this._colorizer == null) ? null : this._colorizer;
		this.editor = editor;
	}

	/**
	 * @see com.aptana.ide.editors.unified.IUnifiedEditorContributor#getReconcilingStrategy()
	 */
	public UnifiedReconcilingStrategy getReconcilingStrategy()
	{
		return new UnifiedReconcilingStrategy();
	}

	/**
	 * @see com.aptana.ide.editors.unified.IUnifiedEditorContributor#getLocalContentAssistProcessor(org.eclipse.jface.text.source.ISourceViewer,
	 *      java.lang.String)
	 */
	public IContentAssistProcessor getLocalContentAssistProcessor(ISourceViewer sourceViewer, String contentType)
	{
		if (contentType.equals(ERBMimeType.MimeType))
		{
			EditorFileContext context = getFileContext();

			if (context != null)
			{
				this._caProcessor = new ERBContentAssistProcessor(editor, context);

				return this._caProcessor;
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see com.aptana.ide.editors.unified.BaseContributor#isAutoActivateContentAssist()
	 */
	public boolean isAutoActivateContentAssist()
	{
		return ERBPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.CODE_ASSIST_AUTO_ACTIVATION);
	}
	
	
	@Override
	public ITextHover getLocalTextHover(ISourceViewer sourceViewer, String contentType) {
		if (contentType == null || !contentType.equals(ERBMimeType.MimeType)) return super.getLocalTextHover(sourceViewer, contentType);
		RubyEditorTextHoverDescriptor[] hoverDescs= RubyPlugin.getDefault().getRubyEditorTextHoverDescriptors();
		int i= 0;
		while (i < hoverDescs.length) {
			if (hoverDescs[i].isEnabled() &&  hoverDescs[i].getStateMask() == SWT.NONE)
				return new RubyEditorTextHoverProxy(hoverDescs[i], editor);
			i++;
		}
		return null;
	}

}
