package org.radrails.rails.internal.generators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

public abstract class GeneratorsLocator  {
	
	protected List<Generator> generators;
	private IProject project;
	
	public GeneratorsLocator(IProject project) {
		generators = new ArrayList<Generator>();
		this.project = project;
	}
	
	public abstract void locateGenerators();
	
	protected IProject getProject() {
		return project;
	}

	public List<Generator> getLocatedGenerators() {
		return generators;
	}
}
