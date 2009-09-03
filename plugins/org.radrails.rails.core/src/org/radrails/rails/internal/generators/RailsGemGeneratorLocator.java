/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.generators;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.radrails.rails.core.RailsLog;
import org.rubypeople.rdt.core.ILoadpathEntry;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;

/**
 * This class locates the installed generators from the rails gem attached to a given project
 * 
 * @author Chris Williams
 * 
 */
public class RailsGemGeneratorLocator extends GeneratorsLocator {

	public RailsGemGeneratorLocator(IProject project) {
		super(project);
	}
	
	public void locateGenerators() {	
		generators.clear();		
		try {
			IPath railsPath = findRailsPath();
			if (railsPath == null) {
				addDefaultGenerators();
				return;
			}
			List<File> generatorFiles = getGeneratorFiles(railsPath);
			if (generatorFiles == null || generatorFiles.isEmpty()) {
				addDefaultGenerators();
				return;
			}
			for (File file : generatorFiles) {
				generators.add(new Generator("/" + file.getName()));
			}		
		} catch (RubyModelException e) {
			RailsLog.log(e);
			addDefaultGenerators();
		}		
	}

	private List<File> getGeneratorFiles(IPath railsPath) {
		List<File> files = new ArrayList<File>();
		File folder = railsPath.append("rails_generator").append("generators").append("components").toFile();
		if (folder == null) return files;
		File[] generatorFolders = folder.listFiles();
		if (generatorFolders == null) return files;
		for (int j = 0; j < generatorFolders.length; j++) {
			File[] actuals = generatorFolders[j].listFiles(new FilenameFilter() {
			
				public boolean accept(File dir, String name) {
					return name.endsWith("_generator.rb");
				}
			
			});
			if (actuals == null) continue;
			for (int x = 0; x < actuals.length; x++) {
				files.add(actuals[x]);
			}
		}
		return files;
	}

	private IPath findRailsPath() throws RubyModelException {
		ILoadpathEntry[] entries = getRubyProject().getResolvedLoadpath(true);
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getEntryKind() != ILoadpathEntry.CPE_LIBRARY) continue;
			IPath path = entries[i].getPath();
			if (path.toPortableString().indexOf("rails-") > 0) {
				return path;
			}
		}
		return null;
	}

	private IRubyProject getRubyProject() {
		return RubyCore.create(getProject());
	}

	private void addDefaultGenerators() {
		Map<String, String> builtinGenerators = new HashMap<String, String>();	
		builtinGenerators.put("/controller_generator.rb", "ControllerName [action, ...]");
		builtinGenerators.put("/integration_test_generator.rb",  "IntegrationTestName");
		builtinGenerators.put("/mailer_generator.rb", "MailerName [view, ...]");
		builtinGenerators.put("/migration_generator.rb", "MigrationName");
		builtinGenerators.put("/model_generator.rb", "ModelName");
		builtinGenerators.put("/observer_generator.rb", "ObserverName");
		builtinGenerators.put("/plugin_generator.rb", "PluginName");
		builtinGenerators.put("/resource_generator.rb", "ModelName [field:type, field:type]");
		builtinGenerators.put("/scaffold_generator.rb", "ModelName [ControllerName] [action, ...]");
		builtinGenerators.put("/session_migration_generator.rb",  "SessionMigrationName");
//		builtinGenerators.put("/web_service_generator.rb", "WebServiceName [method, ...]"); // No longer in Rails 2.x
		Iterator keysIt = builtinGenerators.keySet().iterator();
		while( keysIt.hasNext() ) {
			generators.add( new Generator(keysIt.next().toString()) );
		}
	}
}
