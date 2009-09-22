package com.aptana.radrails.intro;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IStartup;
import org.osgi.service.prefs.BackingStoreException;

import com.aptana.ide.documentation.DocumentationPlugin;
import com.aptana.ide.intro.IntroPlugin;
import com.aptana.radrails.intro.editors.MyRadRailsEditor;

public class ForcePluginToLoad implements IStartup
{
	private static final String RADRAILS_PLUGINS_XML_URL = "http://download.aptana.com/tools/radrails/plugin/plugins_radrails_1.5.xml"; //$NON-NLS-1$
	private static final String RADRAILS_LOCAL_PLUGIN_LISTING_PATH = "plugins_radrails_1.5.xml"; //$NON-NLS-1$


	private static final String RADRAILS_GETTING_STARTED_URL = "http://www.aptana.com/tools/radrails/getting_started"; //$NON-NLS-1$
	private static final String RADRAILS_REMOTE_IMAGE_LOCATION = "http://www.aptana.com/tools/radrails/images/my_radrails.gif"; //$NON-NLS-1$

	// Custom release_message URL prefix
	private static final String RADRAILS_RELEASE_MESSAGE_URL_PREFIX = "http://www.aptana.com/tools/radrails/release-message/"; //$NON-NLS-1$

	public void earlyStartup()
	{
		// Override the URL for Help > Aptana Help > Release Notes
		System.setProperty(DocumentationPlugin.RELEASE_NOTES_URL_SYSTEM_PROPERTY, "http://www.aptana.com/tools/radrails/releasenotes/");
		
		// Set custom RadRails plugins.xml feed
		new InstanceScope().getNode(com.aptana.ide.update.Activator.PLUGIN_ID).put(
				com.aptana.ide.update.IPreferenceConstants.REMOTE_PLUGIN_LISTING_URL, RADRAILS_PLUGINS_XML_URL);
		URL radrailsLocalListingURL = FileLocator.find(Activator.getDefault().getBundle(), new Path(
				RADRAILS_LOCAL_PLUGIN_LISTING_PATH), null);
		if (radrailsLocalListingURL != null)
		{
			new InstanceScope().getNode(com.aptana.ide.update.Activator.PLUGIN_ID).put(
					com.aptana.ide.update.IPreferenceConstants.LOCAL_PLUGIN_LISTING_URL,
					radrailsLocalListingURL.toString());
		}
		
		// FIXME Uncomment when we're building against studio in git!
		// Set up custom release_message URL prefix
		IEclipsePreferences updateUIPrefs = new InstanceScope().getNode(com.aptana.ide.update.Activator.PLUGIN_ID);
		updateUIPrefs.put(
				com.aptana.ide.update.IPreferenceConstants.RELEASE_MESSAGE_URL_PREFIX,
				RADRAILS_RELEASE_MESSAGE_URL_PREFIX);
		try {
			updateUIPrefs.flush();
		} catch (BackingStoreException e) {
			// TODO
		}

		// make My RadRails editor as the default intro editor
		IEclipsePreferences introPluginPrefs = new InstanceScope().getNode(IntroPlugin.PLUGIN_ID);
		introPluginPrefs
				.put(com.aptana.ide.intro.preferences.IPreferenceConstants.INTRO_EDITOR_ID, MyRadRailsEditor.ID);
		introPluginPrefs.put(com.aptana.ide.intro.preferences.IPreferenceConstants.INTRO_TOOLBAR_IMAGE_LOCATION,
				RADRAILS_REMOTE_IMAGE_LOCATION);
		try
		{
			introPluginPrefs.flush();
		}
		catch (BackingStoreException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Set custom RadRails Getting Started feed
		IEclipsePreferences docPluginPrefs = new InstanceScope().getNode(DocumentationPlugin.PLUGIN_ID);
		docPluginPrefs.put(DocumentationPlugin.GETTING_STARTED_CONTENT_URL, RADRAILS_GETTING_STARTED_URL);
		try
		{
			docPluginPrefs.flush();
		}
		catch (BackingStoreException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
