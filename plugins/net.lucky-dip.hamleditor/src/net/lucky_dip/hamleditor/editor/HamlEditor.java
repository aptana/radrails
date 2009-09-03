package net.lucky_dip.hamleditor.editor;

import java.util.ResourceBundle;

import net.lucky_dip.hamleditor.Activator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;

public class HamlEditor extends HamlesqueEditor {

	public static final String HAML_PARTITIONING = "__haml_partitioning";
	public static final String RESOURCE_BUNDLE = "net.lucky_dip.hamleditor.editor.HamlEditorMessages";

	public HamlEditor() {
		super();
		IPreferenceStore defaults = Activator.getDefault().getPreferenceStore();
		IPreferenceStore eclipseUIStore = EditorsUI.getPreferenceStore();
		IPreferenceStore prefStore = new ChainedPreferenceStore(new IPreferenceStore[] {
				eclipseUIStore, defaults });
		setPreferenceStore(prefStore);

		colorManager = new ColorManager(prefStore);
		setSourceViewerConfiguration(new HamlConfiguration(colorManager, this));
	}

	protected void createActions() {
		super.createActions();

		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
		IAction a = new TextOperationAction(bundle, "ContentAssistProposal.", this,
				ISourceViewer.CONTENTASSIST_PROPOSALS);

		// CTRL+Space key doesn't work without making this call
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", a);
		a = new TextOperationAction(bundle, "ContentAssistTip.", this,
				ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction("ContentAssistTip", a);

		a = new TextOperationAction(bundle, "ContentFormatProposal.", this, ISourceViewer.FORMAT);
		setAction("ContentFormatProposal", a);
	}
}
