/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/


package org.radrails.rails.internal.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.radrails.rails.internal.core.RailsPlugin;

/**
 * Manages locating generators in the system rails 
 * install and the user's home directory (Environment)
 * and generators in rails projects.
 * 
 * @author andy
 * @author Chris Williams
 *
 */
public class GeneratorLocatorsManager {
	
	private static GeneratorLocatorsManager instance = null;
	private static Map<IProject, List<GeneratorsLocator>> projectGeneratorLocators;
		
	/**
	 * Default constructor.
	 */
	private GeneratorLocatorsManager() {
		projectGeneratorLocators = new HashMap<IProject, List<GeneratorsLocator>>();
		Set<IProject> projects = RailsPlugin.getRailsProjects();
		for (IProject project : projects) {
			addLocatorsForProject(project);
		}
		// FIXME Listen to changes in resources to modify this project map!
	}

	private List<GeneratorsLocator> addLocatorsForProject(IProject project) {
		List<GeneratorsLocator> locators = new ArrayList<GeneratorsLocator>();
		locators.add(new VendorGeneratorsLocator(project));
		locators.add(new RailsGemGeneratorLocator(project));
		projectGeneratorLocators.put(project, locators);
		return locators;
	}
	
	/**
	 * @return the singleton instance of <code></code>.
	 */
	public static GeneratorLocatorsManager getInstance() {
		if (instance == null) {
			instance = new GeneratorLocatorsManager();
		}
		return instance;
	}
	
	/**
	 * Gets all the generators associated with the environment and the currently selected project,
	 * without actually doing any reparsing.
	 * 
	 * @return List of previously discovered generators in the environment and current project.
	 */
	public List<Generator> getAllGenerators(IProject currentProject) {		
		List<Generator> generators = new ArrayList<Generator>();			
		if (currentProject != null) {
			List<GeneratorsLocator> locators = getLocators(currentProject);
			for (GeneratorsLocator locator : locators) {
				locator.locateGenerators();
				generators.addAll( locator.getLocatedGenerators() );
			}
		}
		return generators;
	}

	private List<GeneratorsLocator> getLocators(IProject currentProject) {
		List<GeneratorsLocator> locators = projectGeneratorLocators.get(currentProject);
		if (locators == null) {
			locators = addLocatorsForProject(currentProject);
		}
		return locators;
	}
}
