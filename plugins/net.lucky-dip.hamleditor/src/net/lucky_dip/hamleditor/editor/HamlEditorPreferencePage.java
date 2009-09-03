package net.lucky_dip.hamleditor.editor;

import net.lucky_dip.hamleditor.Activator;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class HamlEditorPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 * Constructor.
	 */
	public HamlEditorPreferencePage() {
		super(GRID);

		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Haml editor preferences");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		addField(new ColorFieldEditor(IHamlEditorColorConstants.HAML_DOCTYPE,
				"Doctype", getFieldEditorParent()));
		addField(new ColorFieldEditor(IHamlEditorColorConstants.HAML_CLASS,
				"CSS Class", getFieldEditorParent()));
		addField(new ColorFieldEditor(IHamlEditorColorConstants.HAML_COMMENT,
				"Comment", getFieldEditorParent()));
		addField(new ColorFieldEditor(IHamlEditorColorConstants.HAML_ELEMENT,
				"Element", getFieldEditorParent()));
		addField(new ColorFieldEditor(IHamlEditorColorConstants.HAML_ID,
				"CSS ID", getFieldEditorParent()));
		addField(new ColorFieldEditor(IHamlEditorColorConstants.HAML_RUBY_BACKGROUND,
				"Ruby Code Background", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// Do nothing
	}

	protected void performApply() {
		super.performApply();
	}
}
