package org.radrails.rails.internal.ui.browser;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.eclipse.ui.texteditor.ITextEditor;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.rubypeople.rdt.internal.ui.rubyeditor.EditorUtility;
import org.rubypeople.rdt.internal.ui.rubyeditor.ExternalRubyFileEditorInput;
import org.rubypeople.rdt.ui.RubyUI;

public class OpenEditorAction implements IIntroAction {

	public void run(IIntroSite site, Properties params) {
		String path = (String) params.get("path");
		if (path.startsWith("<A href")) {
			// screwed up link!
			return;
		}
		String rawLine = (String) params.get("line");
		int line = -1;
		try {
			line = Integer.parseInt(rawLine);
		} catch (NumberFormatException e1) {
			line = -1;
		}
		try {
			IFile file = getFile(path);
			IEditorPart part;
			if (file == null) {
				IEditorInput input = new ExternalRubyFileEditorInput(new File(path));
				part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, RubyUI.ID_EXTERNAL_EDITOR);
			} else {
				part = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
			}
			if (part == null) {
				RailsUILog.logError("Error creating editor input for stack trace from browser", null);
				// wrongly detected stack trace
				return;
			}			
			if (line == -1) return;
			if (part instanceof ITextEditor) {
				ITextEditor editor = (ITextEditor) part;
				IDocument doc = editor.getDocumentProvider().getDocument(part.getEditorInput());				
				try {
					int offset = doc.getLineOffset(line - 1);
					EditorUtility.revealInEditor(part, offset, 0);
				} catch (NumberFormatException e) {
					// ignore
				} catch (BadLocationException e) {
					// ignore
				}
			}	
		} catch (CoreException e) {
			RailsUILog.logError("Could not open editor or set line in editor", e);
		}
	}

	private IFile getFile(String path) {
		if (path.indexOf(":/") != -1) { // absolute path
			return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(path));
		}
		if (!path.startsWith("./")) {
			return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
		}
		Collection servers = ServerManager.getInstance().getServers();
		for (Iterator iter = servers.iterator(); iter.hasNext();) {
			Server s = (Server) iter.next();
			if (s.isStarted()) {
				String tmp = "/" + s.getProject().getName() + path.substring(1);
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(tmp));
				if (file != null && file.exists()) return file;
			}
		}
		return null;
	}
}
