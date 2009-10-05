package net.lucky_dip.sasseditor.editor;

import java.util.ResourceBundle;

import net.lucky_dip.hamleditor.Activator;
import net.lucky_dip.hamleditor.editor.HamlUIColorProvider;
import net.lucky_dip.hamleditor.editor.HamlesqueEditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;

public class SassEditor extends HamlesqueEditor
{

	public static final String SASS_PARTITIONING = "__sass_partitioning";
	public static final String RESOURCE_BUNDLE = "net.lucky_dip.sasseditor.editor.SassEditorMessages";

	public SassEditor()
	{
		super();
		IPreferenceStore defaults = Activator.getDefault().getPreferenceStore();
		IPreferenceStore eclipseUIStore = EditorsUI.getPreferenceStore();
		IPreferenceStore prefStore = new ChainedPreferenceStore(new IPreferenceStore[] { eclipseUIStore, defaults });
		setPreferenceStore(prefStore);

		colorManager = new HamlUIColorProvider();
		setSourceViewerConfiguration(new SassConfiguration(colorManager, this));
	}

	protected void createActions()
	{
		super.createActions();

		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
		IAction a = new TextOperationAction(bundle, "ContentAssistProposal.", this,
				ISourceViewer.CONTENTASSIST_PROPOSALS);

		// CTRL+Space key doesn't work without making this call
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", a);
		a = new TextOperationAction(bundle, "ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction("ContentAssistTip", a);

		a = new SassOrganiseAction(bundle, "Organise.", this);
		setAction("Organise", a);
	}

	protected void editorContextMenuAboutToShow(IMenuManager menu)
	{
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, "Organise");
	}
}
