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
package com.aptana.ide.editor.erb.actions;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.radrails.rails.internal.core.RailsPlugin;

import com.aptana.ide.core.IdeLog;
import com.aptana.ide.editor.erb.ERBPlugin;

/**
 * 
 * @author Chris Williams
 *
 */
public class ExtractPartialAction extends Action {

	private ITextEditor fEditor;
	private IFile fFile;
	private String fExtension;
	
	public ExtractPartialAction(ITextEditor editor) {
		super();
		setText(EditorMessages.getString("ExtractPartial.label"));
		this.fEditor = editor;
	}
	
	public void run() {		
		ISelection sel = fEditor.getSelectionProvider().getSelection();
		if (validSelection(sel)) {
			TextSelection textSel = (TextSelection) sel;
			extract(textSel);
		}
		
	}

	private void extract(TextSelection textSel) {
		InputDialog dialog = new InputDialog(Display.getDefault().getActiveShell(), "Extract Partial", "Enter the partial name:", "", new IInputValidator() {
		
			public String isValid(String newText) {
				if (newText == null || newText.trim().length() == 0) return "You have entered an empty filename. Please enter at least one character.";
				IFile file = getFile(newText);
				if (file.exists()) return "A file that name already exists. Please choose a new name.";
				return null;
			}
		
		});
		if (dialog.open() != Dialog.OK) return;
		String fileName = dialog.getValue();
		IFile file = getFile(fileName);
		if (!file.exists()) {
			try {
				ByteArrayInputStream bas = new ByteArrayInputStream(textSel.getText().getBytes());
				// create the partial file
				file.create(bas, true, null);
				// delete the partial text from the editor
				String replacement = "<%= render :partial => '" + file.getName().substring(1, file.getName().length() - getExtension().length()) + "' %>";
				fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput()).replace(textSel.getOffset(), textSel.getLength(), replacement);
			} catch (CoreException e) {
				IdeLog.logError(ERBPlugin.getDefault(), e.getMessage(), e);
			} catch (BadLocationException e) {
				IdeLog.logError(ERBPlugin.getDefault(), e.getMessage(), e);
			}
		}
	}

	private IFile getFile(String fileName) {
		fileName = addExtension(fileName);
		if (!fileName.startsWith("_")) fileName = "_" + fileName;
		IPath path = getFile().getFullPath().removeLastSegments(1).append(fileName);
		return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	}

	private IFile getFile() {
		IFileEditorInput fei = (IFileEditorInput) fEditor.getEditorInput();
		return fei.getFile();
	}

	private String addExtension(String fileName) {
		if (!fileName.endsWith(getExtension())) return fileName + getExtension();
		return fileName;
	}

	private String getExtension() {
		if (fFile == null || !getFile().equals(fFile)) {
			fFile = getFile();
			fExtension = ".html.erb";
			if (fFile != null) {
				IProject project = fFile.getProject();
				if (project != null) {
					String version = RailsPlugin.getRailsVersion(project);
					if (version != null && (version.startsWith("1.") || version.startsWith("1."))) {
						fExtension = ".rhtml";
					}
				}
			}
		}
		return fExtension;
	}

	private boolean validSelection(ISelection sel) {
		return sel != null && sel instanceof TextSelection && !sel.isEmpty();
	}
}