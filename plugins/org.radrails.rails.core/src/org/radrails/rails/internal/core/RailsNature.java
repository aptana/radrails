package org.radrails.rails.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class RailsNature implements IProjectNature {

	private IProject fProject;

	public void configure() throws CoreException {
		// do nothing
	}

	public void deconfigure() throws CoreException {
		// do nothing
	}

	public IProject getProject() {
		return fProject;
	}

	public void setProject(IProject project) {
		fProject = project;
	}

}
