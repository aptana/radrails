/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.generators;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.radrails.rails.core.RailsLog;
import org.radrails.rails.internal.core.RailsPlugin;

public class VendorGeneratorsLocator extends GeneratorsLocator implements IResourceChangeListener
{

	private static final String GENERATORS_DIRECTORY = "generators";

	public VendorGeneratorsLocator(IProject project)
	{
		super(project);
	}

	public void locateGenerators()
	{
		String[] generatorLocations = { "lib", "vendor" };
		generators.clear();

		for (int i = 0; i < generatorLocations.length; i++)
		{
			IFolder folder = getRailsRoot().getFolder(new Path(generatorLocations[i]).append(GENERATORS_DIRECTORY));
			populateGeneratorsAtPath(folder);
		}
		populateGeneratorsFromPlugins(getRailsRoot().getFolder(new Path("vendor").append("plugins")));
	}

	private IContainer getRailsRoot()
	{
		IPath root = RailsPlugin.findRailsRoot(getProject());
		if (root == null || root.segmentCount() == 0)
			return getProject();
		return getProject().getFolder(root);
	}

	private void populateGeneratorsFromPlugins(IFolder pluginsDir)
	{
		if (pluginsDir == null)
			return;
		if (!pluginsDir.exists())
			return;
		try
		{
			IResource[] members = pluginsDir.members();
			for (int i = 0; i < members.length; i++)
			{
				IResource plugin = members[i];
				if (plugin.getType() != IResource.FOLDER)
					continue; // skip if it's not a folder
				// Search for generators under each plugin's directory
				IFolder generatorsFolder = ((IFolder) plugin).getFolder(GENERATORS_DIRECTORY);
				populateGeneratorsAtPath(generatorsFolder);
			}
		}
		catch (CoreException e)
		{
			RailsLog.logError("Could not fetch members of " + pluginsDir.getName(), e);
		}
	}

	private void populateGeneratorsAtPath(IFolder folder)
	{
		if (folder == null)
			return;
		if (!folder.exists())
			return;
		if (!folder.getName().equals(GENERATORS_DIRECTORY))
			return;
		try
		{
			IResource[] genFolderMembers = folder.members();
			for (int i = 0; i < genFolderMembers.length; i++)
			{
				if (genFolderMembers[i].getType() != IResource.FOLDER)
					continue;
				IFolder generatorFolder = (IFolder) genFolderMembers[i];
				IFile generatorFile = generatorFolder.getFile(generatorFolder.getName() + "_generator.rb");
				if (!generatorFile.exists())
					continue;
				generators.add(new Generator(generatorFile.getLocation().toPortableString()));
			}
		}
		catch (CoreException e)
		{
			RailsLog.logError("Could not fetch members of " + folder.getName(), e);
		}
	}

	public void resourceChanged(IResourceChangeEvent event)
	{
		RailsLog.logInfo("resource changed " + event.getDelta().getFullPath().toString(), null);
	}

}
