package net.lucky_dip.hamleditor.preferences;

import net.lucky_dip.hamleditor.Activator;
import net.lucky_dip.hamleditor.editor.IHamlEditorColorConstants;
import net.lucky_dip.sasseditor.editor.ISassEditorColorConstants;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{

	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(com.aptana.ide.editors.preferences.IPreferenceConstants.CODE_ASSIST_AUTO_ACTIVATION, false);

		// HAML File Wizard
		store.setDefault(IPreferenceConstants.HAML_EDITOR_INITIAL_CONTENTS,
				Messages.PreferenceInitializer_IntitialHAMLFileContents);
		store.setDefault(IPreferenceConstants.HAML_EDITOR_INITIAL_FILE_NAME,
				Messages.PreferenceInitializer_Default_Haml_Filename);

		// HAML Editor colors
		store.setDefault(IHamlEditorColorConstants.HAML_CLASS, "63,127,127");
		store.setDefault(IHamlEditorColorConstants.HAML_COMMENT, "63,95,191");
		store.setDefault(IHamlEditorColorConstants.HAML_DOCTYPE, "128,128,128");
		store.setDefault(IHamlEditorColorConstants.HAML_ELEMENT, "127,0,127");
		store.setDefault(IHamlEditorColorConstants.HAML_ID, "42,0,255");
		store.setDefault(IHamlEditorColorConstants.HAML_RUBY_BACKGROUND, "232,232,232");

		// HAML Editor prefs (not sure if used anymore?)
		store.setDefault("rails.ui.editor.haml.char_matching", "true");
		store.setDefault("rails.ui.editor.haml.char_matching.background", "0,0,255");

		// Sass File Wizard
		store.setDefault(IPreferenceConstants.SASS_EDITOR_INITIAL_CONTENTS,
				Messages.PreferenceInitializer_IntitialSassFileContents);
		store.setDefault(IPreferenceConstants.SASS_EDITOR_INITIAL_FILE_NAME,
				Messages.PreferenceInitializer_Default_Sass_Filename);

		// SASS Editor colors
		store.setDefault(ISassEditorColorConstants.SASS_ATTRIBUTE, "127,0,127");
		store.setDefault(ISassEditorColorConstants.SASS_CLASS, "63,127,127");
		store.setDefault(ISassEditorColorConstants.SASS_COMMENT, "63,95,191");
		store.setDefault(ISassEditorColorConstants.SASS_CONSTANT, "255,0,0");
		store.setDefault(ISassEditorColorConstants.SASS_ID, "42,0,255");
		store.setDefault(ISassEditorColorConstants.SASS_SCRIPT, "0,255,0");
		store.setDefault(ISassEditorColorConstants.SASS_TAG, "255,0,255");
	}

}
