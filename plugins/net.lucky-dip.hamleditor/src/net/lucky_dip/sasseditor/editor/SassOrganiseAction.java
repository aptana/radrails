package net.lucky_dip.sasseditor.editor;

import java.util.ResourceBundle;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

public class SassOrganiseAction extends TextEditorAction {

	private TextEditor editor;

	public SassOrganiseAction(ResourceBundle bundle, String prefix,
			TextEditor editor) {
		super(bundle, prefix, editor);

		this.editor = editor;
	}

	public void run() {
		IDocument doc = editor.getDocumentProvider().getDocument(
				editor.getEditorInput());
		String text = doc.get();

		SassBlock block = new SassBlock(text);
		block.sort();

		doc.set(block.toString());
	}
}
