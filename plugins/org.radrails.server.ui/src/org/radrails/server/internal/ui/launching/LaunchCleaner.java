package org.radrails.server.internal.ui.launching;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.radrails.server.core.launching.IRailsAppLaunchConfigurationConstants;

public class LaunchCleaner implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		if (event == null)
			return;
		IResourceDelta delta = event.getDelta();
		checkDelta(delta);
	}

	private void checkDelta(IResourceDelta delta) {
		if (delta == null)
			return;
		IResource resource = delta.getResource();
		if (!(resource instanceof IWorkspaceRoot)
				&& !(resource instanceof IProject))
			return; // don't dive any deeper than project
		if (resource instanceof IProject) {
			if (IResourceDelta.REMOVED != delta.getKind())
				return;
			IPath path = delta.getFullPath();
			String name = path.lastSegment();
			projectRemoved(name);

		}
		IResourceDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; i++) {
			checkDelta(children[i]);
		}
	}

	private void projectRemoved(String name) {
		try {
			ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(IRailsAppLaunchConfigurationConstants.LAUNCH_TYPE_ID);
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type);
			for (int i = 0; i < configs.length; i++) {
				String projectName = configs[i].getAttribute(IRailsAppLaunchConfigurationConstants.PROJECT_NAME, (String) null);
				if (projectName != null && projectName.equals(name)) {
					configs[i].delete();
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
