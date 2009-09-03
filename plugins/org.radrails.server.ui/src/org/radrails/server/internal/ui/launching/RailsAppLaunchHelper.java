package org.radrails.server.internal.ui.launching;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.ui.RailsUIPlugin;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.radrails.server.core.launching.IRailsAppLaunchConfigurationConstants;

public class RailsAppLaunchHelper {

	public static void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// if a project is selected, choose that app by default, otherwise just pick the first
		IProject curSel = RailsUIPlugin.getInstance().getSelectedRailsProject();
		String defaultProject = null;
		if(curSel != null) {
			defaultProject = curSel.getName();
		} else {
			Set<IProject> projects = RailsPlugin.getRailsProjects();
			if(projects.size() > 0) {
				defaultProject = projects.iterator().next().getName();
			}
		}
		
		// select the first server for the default project
		if(defaultProject != null) {
			configuration.setAttribute(IRailsAppLaunchConfigurationConstants.PROJECT_NAME, defaultProject);
			
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(defaultProject);
			Collection servers = ServerManager.getInstance().getServersForProject(project);
			Server s = null;
			if (!servers.isEmpty())
				s = (Server) servers.iterator().next();	// TODO Ask user which server they want?		
			if (s != null) {
				configuration.setAttribute(IRailsAppLaunchConfigurationConstants.SERVER_NAME, s.getName());
			}
		}
		configuration.setAttribute(IRailsAppLaunchConfigurationConstants.LAUNCH_BROWSER, true);
		configuration.setAttribute(IRailsAppLaunchConfigurationConstants.USE_INTERNAL_BROWSER, true);
	}
}
