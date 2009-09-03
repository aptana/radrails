package org.radrails.rails.internal.core;

import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.radrails.rails.core.IRailsConstants;
import org.radrails.rails.internal.parser.warnings.RailsDeprecationOptions;

public class RailsPluginPreferenceInitializer extends AbstractPreferenceInitializer
{
	public void initializeDefaultPreferences()
	{
		HashSet<String> optionNames = RailsPlugin.getInstance().optionNames;
		// Lint visitor settings
		Map<String, String> defaultOptionsMap = new RailsDeprecationOptions().getMap(); // compiler defaults

		// Store default values to default preferences
		IEclipsePreferences defaultPreferences = new DefaultScope().getNode(RailsPlugin.PLUGIN_ID);
		for (Map.Entry<String, String> entry : defaultOptionsMap.entrySet())
		{
			String optionName = entry.getKey();
			defaultPreferences.put(optionName, entry.getValue());
			optionNames.add(optionName);
		}
		RailsPlugin.getInstance().optionsCache = null;

		// Auto-open rails shell by default
		defaultPreferences.putBoolean(IRailsConstants.AUTO_OPEN_RAILS_SHELL, true);
	}
}
