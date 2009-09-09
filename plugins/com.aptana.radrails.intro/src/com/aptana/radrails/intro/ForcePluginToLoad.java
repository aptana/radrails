package com.aptana.radrails.intro;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IStartup;
import org.osgi.service.prefs.BackingStoreException;

import com.aptana.ide.documentation.DocumentationPlugin;
import com.aptana.ide.intro.IntroPlugin;
import com.aptana.ide.update.ui.UpdateUIActivator;
import com.aptana.radrails.intro.editors.MyRadRailsEditor;

public class ForcePluginToLoad implements IStartup {

	private static final String RADRAILS_GETTING_STARTED_URL = "http://content.aptana.com/radrails/tutorials/index.php"; //$NON-NLS-1$
	private static final String RADRAILS_REMOTE_IMAGE_LOCATION = "http://partners.aptana.com/adobe/content_ide/images/toolbar_intro.gif"; //$NON-NLS-1$

	// Custom release_message URL prefix
	private static final String RADRAILS_RELEASE_MESSAGE_URL_PREFIX = "http://content.aptana.com/radrails/update/release-message/"; //$NON-NLS-1$

	public void earlyStartup() {
		// Set up custom release_message URL prefix
		IEclipsePreferences updateUIPrefs = new InstanceScope()
				.getNode(UpdateUIActivator.PLUGIN_ID);
		updateUIPrefs
				.put(
						com.aptana.ide.update.preferences.IPreferenceConstants.RELEASE_MESSAGE_URL_PREFIX,
						RADRAILS_RELEASE_MESSAGE_URL_PREFIX);
		try {
			updateUIPrefs.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// make My Adobe editor as the default intro editor
		IEclipsePreferences introPluginPrefs = new InstanceScope()
				.getNode(IntroPlugin.PLUGIN_ID);
		introPluginPrefs
				.put(
						com.aptana.ide.intro.preferences.IPreferenceConstants.INTRO_EDITOR_ID,
						MyRadRailsEditor.ID);
		introPluginPrefs
				.put(
						com.aptana.ide.intro.preferences.IPreferenceConstants.INTRO_TOOLBAR_IMAGE_LOCATION,
						RADRAILS_REMOTE_IMAGE_LOCATION);
		try {
			introPluginPrefs.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Set custom RadRails Getting Started feed
		IEclipsePreferences docPluginPrefs = new InstanceScope()
				.getNode(DocumentationPlugin.PLUGIN_ID);
		docPluginPrefs.put(DocumentationPlugin.GETTING_STARTED_CONTENT_URL,
				RADRAILS_GETTING_STARTED_URL);
		try {
			docPluginPrefs.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
