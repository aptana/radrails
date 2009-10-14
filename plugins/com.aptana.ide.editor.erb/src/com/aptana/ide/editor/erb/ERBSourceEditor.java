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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.rubypeople.rdt.internal.debug.ui.actions.ToggleBreakpointAdapter;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.ui.actions.OpenAction;
import org.rubypeople.rdt.ui.actions.OpenEditorActionGroup;

import com.aptana.ide.editor.erb.actions.ExtractPartialAction;
import com.aptana.ide.editor.html.HTMLSourceEditor;
import com.aptana.ide.editors.unified.IFileServiceFactory;
import com.aptana.ide.editors.unified.IUnifiedEditorContributor;

/**
 * @author Kevin Lindsey
 */
public class ERBSourceEditor extends HTMLSourceEditor
{

	private OpenEditorActionGroup oeg;

	/**
	 * ERB source editor constructor
	 */
	public ERBSourceEditor()
	{
		super();
		addPluginToPreferenceStore(ERBPlugin.getDefault());
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException
	{
		IEditorInput oldInput = getEditorInput();
		if (oldInput != null)
			RubyPlugin.getDefault().getRubyDocumentProvider().disconnect(oldInput);

		super.doSetInput(input);

		RubyPlugin.getDefault().getRubyDocumentProvider().connect(input);
	}

	@Override
	public void dispose()
	{
		IEditorInput input = getEditorInput();
		if (input != null)
			RubyPlugin.getDefault().getRubyDocumentProvider().disconnect(input);

		if (oeg != null)
			oeg.dispose();
		oeg = null;

		super.dispose();
	}

	@Override
	protected void initializeKeyBindingScopes()
	{
		super.initializeKeyBindingScopes();
		setKeyBindingScopes(new String[] {
				"com.aptana.ide.editors.UnifiedEditorsScope", IERBEditorActionConstants.ERB_EDITOR_CONTEXT }); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see com.aptana.ide.editor.html.HTMLSourceEditor#createLocalContributor()
	 */
	protected IUnifiedEditorContributor createLocalContributor()
	{
		this.contributor = new ExtendedHTMLContributor(this);
		return this.contributor;
	}

	/**
	 * @see com.aptana.ide.editor.html.HTMLSourceEditor#getFileServiceFactory()
	 */
	public IFileServiceFactory getFileServiceFactory()
	{
		return ERBFileServiceFactory.getInstance();
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent)
	{
		super.createPartControl(parent);

		ITextViewerExtension tve = (ITextViewerExtension) getSourceViewer();
		tve.appendVerifyKeyListener(new ExpressionCloser((SourceViewer) getSourceViewer()));
	}

	@Override
	protected void createActions()
	{
		super.createActions();

		Action action = new OpenAction(this);
		action.setActionDefinitionId(IERBEditorActionConstants.OPEN_EDITOR);
		setAction(IERBEditorActionConstants.OPEN_EDITOR, action); //$NON-NLS-1$

		action = new ExtractPartialAction(this);
		action.setActionDefinitionId(IERBEditorActionConstants.EXTRACT_PARTIAL_ACTION);
		setAction(IERBEditorActionConstants.EXTRACT_PARTIAL_ACTION, action);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IERBHelpContextIds.EXTRACT_PARTIAL_ACTION);

		action = new ContentAssistAction(RubyPlugin.getDefault().getPluginProperties(), "ContentAssistProposal.", this);
		action.setActionDefinitionId(IERBEditorActionConstants.CONTENT_ASSIST_PROPOSALS);
		setAction(IERBEditorActionConstants.CONTENT_ASSIST_PROPOSALS, action);
	}

	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu)
	{
		super.editorContextMenuAboutToShow(menu);
		IAction action = getAction(IERBEditorActionConstants.EXTRACT_PARTIAL_ACTION);
		menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, action);

		action = getAction(IERBEditorActionConstants.OPEN_EDITOR);
		menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, action);
	}

	@Override
	public Object getAdapter(Class adapter)
	{
		if (IToggleBreakpointsTarget.class.equals(adapter))
			return new ToggleBreakpointAdapter();
		return super.getAdapter(adapter);
	}

}