package com.aptana.radrails.intro;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;

import com.aptana.ide.documentation.DocumentationPlugin;
import com.aptana.ide.intro.IntroPlugin;
import com.aptana.ide.update.ui.UpdateUIActivator;
import com.aptana.radrails.intro.editors.MyRadRailsEditor;

public class ForcePluginToLoad implements IStartup
{

	private static final String RADRAILS_GETTING_STARTED_URL = "http://partners.aptana.com/adobe/content_ide/tutorials/index.html"; //$NON-NLS-1$
	private static final String RADRAILS_REMOTE_IMAGE_LOCATION = "http://partners.aptana.com/adobe/content_ide/images/toolbar_intro.gif"; //$NON-NLS-1$
	
	// Custom release_message URL prefix
	private static final String RADRAILS_RELEASE_MESSAGE_URL_PREFIX = "http://partners.aptana.com/adobe/update/release-message/"; //$NON-NLS-1$

	public void earlyStartup()
	{

		// Set up custom release_message URL prefix
		UpdateUIActivator.getDefault().getPreferenceStore()
				.setValue(com.aptana.ide.update.preferences.IPreferenceConstants.RELEASE_MESSAGE_URL_PREFIX,
						RADRAILS_RELEASE_MESSAGE_URL_PREFIX);

		// make My Adobe editor as the default intro editor
		IPreferenceStore prefs = IntroPlugin.getDefault().getPreferenceStore();
		prefs.setValue(com.aptana.ide.intro.preferences.IPreferenceConstants.INTRO_EDITOR_ID, MyRadRailsEditor.ID);
		prefs.setValue(com.aptana.ide.intro.preferences.IPreferenceConstants.INTRO_TOOLBAR_IMAGE_LOCATION,
				RADRAILS_REMOTE_IMAGE_LOCATION);
		// Set custom RadRails Getting Started feed
		new InstanceScope().getNode(DocumentationPlugin.PLUGIN_ID).put(DocumentationPlugin.GETTING_STARTED_CONTENT_URL,
				RADRAILS_GETTING_STARTED_URL);
	}

}
