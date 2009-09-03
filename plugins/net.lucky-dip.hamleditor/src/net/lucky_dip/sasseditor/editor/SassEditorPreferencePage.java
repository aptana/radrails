package net.lucky_dip.sasseditor.editor;

import net.lucky_dip.hamleditor.Activator;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SassEditorPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * Constructor.
	 */
	public SassEditorPreferencePage() {
		super(GRID);

		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Sass editor preferences");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		addField(new ColorFieldEditor(ISassEditorColorConstants.SASS_CONSTANT, "Constant",
				getFieldEditorParent()));
		addField(new ColorFieldEditor(ISassEditorColorConstants.SASS_CLASS, "Class",
				getFieldEditorParent()));
		addField(new ColorFieldEditor(ISassEditorColorConstants.SASS_ID, "Element",
				getFieldEditorParent()));
		addField(new ColorFieldEditor(ISassEditorColorConstants.SASS_ATTRIBUTE, "Attribute",
				getFieldEditorParent()));
		addField(new ColorFieldEditor(ISassEditorColorConstants.SASS_COMMENT, "Comment",
				getFieldEditorParent()));
		addField(new ColorFieldEditor(ISassEditorColorConstants.SASS_SCRIPT, "Script",
				getFieldEditorParent()));
		addField(new ColorFieldEditor(ISassEditorColorConstants.SASS_TAG, "Sass Tag",
				getFieldEditorParent()));
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