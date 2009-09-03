/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.db.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.radrails.rails.core.RailsLog;

/**
 * Represents a project's database connections.
 * 
 * @author mbaumbach
 * @author cwilliams
 * @version 0.3.0
 */
public class ProjectDatabaseManager implements IResourceChangeListener
{

	private IProject project;

	ProjectDatabaseManager(IProject project)
	{
		this.project = project;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	private IPath getDatabaseYMLPath()
	{
		return DatabaseYml.getPath(project);
	}

	/**
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event)
	{
		resourcesChanged(event.getDelta(), 1);
	}

	/**
	 * Called when a resource has changed.
	 * 
	 * @param delta
	 *            The change delta that occured.
	 * @param indent
	 *            How far down the tree/indent this is happening. This is incremented as this method is recursive.
	 */
	private void resourcesChanged(IResourceDelta delta, int indent)
	{
		oneResourceChanged(delta, indent);
		IResourceDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; i++)
		{
			resourcesChanged(children[i], indent + 1);
		}
	}

	/**
	 * Called when a resource has changed. Closes the connection and clears the list of connections.
	 * 
	 * @param delta
	 *            The change delta that occured.
	 * @param indent
	 *            How far down the tree/indent this is happening. This is incremented as this method is recursive.
	 */
	private void oneResourceChanged(IResourceDelta delta, int indent)
	{
		if (delta == null)
			return;
		IResource resource = delta.getResource();
		if (resource == null)
			return;
		String location = "";
		if (resource.getLocation() != null)
			location = resource.getLocation().toString();
		if (location.equals(getDatabaseYMLPath()))
		{
			DatabaseManager.getInstance().databaseChanged(resource.getProject());
		}

	}

	public Collection<DatabaseDescriptor> getDatabaseDescriptors()
	{
		try
		{
			DatabaseYml yml = DatabaseYml.create(project);
			return yml.getDescriptors();
		}
		catch (Exception e)
		{
			RailsLog.log(e);
		}
		return Collections.emptyList();
	}

	public Connection getConnection(String env)
	{
		Collection<DatabaseDescriptor> descriptors = getDatabaseDescriptors();
		for (DatabaseDescriptor databaseDescriptor : descriptors)
		{
			if (databaseDescriptor.getName().equals(env))
			{
				try
				{
					Class.forName(databaseDescriptor.getDriver());
					return DriverManager.getConnection(databaseDescriptor.getUrl());
				}
				catch (ClassNotFoundException e)
				{
					DatabaseLog.log(e);
				}
				catch (SQLException e)
				{
					DatabaseLog.log(e);
				}
			}
		}
		return null;
	}
}
