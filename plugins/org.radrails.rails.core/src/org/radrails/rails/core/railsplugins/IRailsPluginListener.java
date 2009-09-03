package org.radrails.rails.core.railsplugins;

import org.eclipse.core.resources.IProject;

public interface IRailsPluginListener {
	
	public void pluginInstalled(IProject project, RailsPluginDescriptor plugin);
	
	public void pluginRemoved(IProject project, RailsPluginDescriptor plugin);
	
	public void remotePluginsRefreshed();

}
