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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.radrails.rails.core.IRailsConstants;

/**
 * Singleton class that manages all of the workspace database connections. Connections are grouped by project,in
 * ProjectDatabaseManager objects.
 * 
 * @author mkent
 * @version 0.3.1
 */
public class DatabaseManager implements IResourceChangeListener
{

	private static DatabaseManager instance;

	private Map<IProject, ProjectDatabaseManager> projectDatabaseManagerMap;

	private Set<IDatabaseListener> listeners = new HashSet<IDatabaseListener>();

	/**
	 * Constructor. Creates and loads the map of databases.
	 */
	private DatabaseManager()
	{
		projectDatabaseManagerMap = new HashMap<IProject, ProjectDatabaseManager>();
		loadMap();

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Loads the database connections for each project in the workspace.
	 */
	private void loadMap()
	{
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++)
		{
			try
			{
				if (projects[i].isOpen() && projects[i].exists()
						&& projects[i].hasNature(IRailsConstants.RAILS_PROJECT_NATURE))
				{
					projectDatabaseManagerMap.put(projects[i], new ProjectDatabaseManager(projects[i]));
				}
			}
			catch (CoreException e)
			{
				DatabaseLog.logError("Rails Project Nature not found", e);
			}
		}
	}

	/**
	 * @return the singleton instance of this class
	 */
	public static DatabaseManager getInstance()
	{
		if (instance == null)
		{
			instance = new DatabaseManager();
		}
		return instance;
	}

	/**
	 * Return the ProjectDatabaseManager for the given project.
	 * 
	 * @param project
	 *            The project to get the ProjectDatabaseManager for.
	 * @return The corresponding ProjectDatabaseManager for the project if it exists, or <code>null</code> if none
	 *         exists.
	 */
	public ProjectDatabaseManager getProjectDatabaseManager(IProject project)
	{
		return projectDatabaseManagerMap.get(project);
	}

	/**
	 * Return all of the ProjectDatabaseManagers.
	 * 
	 * @return A Collection of all known ProjectDatabaseManagers.
	 */
	public Collection<ProjectDatabaseManager> getAllProjectDatabaseManagers()
	{
		return projectDatabaseManagerMap.values();
	}

	/**
	 * Add a ProjectDatabaseManager to the map.
	 * 
	 * @param project
	 *            The project to add a ProjectDatabaseManager for.
	 */
	public void addProjectDatabaseManager(IProject project)
	{
		projectDatabaseManagerMap.put(project, new ProjectDatabaseManager(project));
		notifyListeners();
	}

	private void notifyListeners()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Remove a ProjectDatabaseManager from the map.
	 * 
	 * @param project
	 *            The project to remove a ProjectDatabaseManager for.
	 */
	public void removeProjectDatabaseManager(IProject project)
	{
		projectDatabaseManagerMap.remove(project);
	}

	/**
	 * Called when a resource is changed.
	 * 
	 * @param event
	 *            The event that occurred.
	 */
	public void resourceChanged(IResourceChangeEvent event)
	{
		if (event.getType() == IResourceChangeEvent.POST_CHANGE)
		{
			IResourceDelta[] resources = event.getDelta().getAffectedChildren();
			for (int i = 0; i < resources.length; i++)
			{
				IResource resource = (IResource) resources[i].getResource();
				if (resource instanceof IProject)
				{
					IProject project = (IProject) resource;
					// Make sure it's using the correct nature
					try
					{
						switch (resources[i].getKind())
						{
							case IResourceDelta.ADDED:
								if (project.hasNature(IRailsConstants.RAILS_PROJECT_NATURE))
								{
									addProjectDatabaseManager(project);
									for (IDatabaseListener listener : listeners)
									{
										listener.projectAdded(project);
									}
								}
								break;
							case IResourceDelta.CHANGED:
								if (project.isOpen() && project.hasNature(IRailsConstants.RAILS_PROJECT_NATURE))
								{
									addProjectDatabaseManager(project);
									for (IDatabaseListener listener : listeners)
									{
										listener.projectAdded(project);
									}
								}
								break;
							case IResourceDelta.REMOVED:
								removeProjectDatabaseManager(project);
								for (IDatabaseListener listener : listeners)
								{
									listener.projectRemoved(project);
								}
								break;
						}
					}
					catch (CoreException e)
					{
						DatabaseLog.logError("Rails Project Nature not found", e);
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Attempt to stop any databases that need stopping.
	 */
	public void stopAll()
	{
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:derby:;shutdown=true");
			conn.close();
		}
		catch (Exception e)
		{
			// Evidently it _always_ throws an exception
		}
	}

	public void closeAll()
	{
		// nothing
	}

	public void addListener(IDatabaseListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(IDatabaseListener listener)
	{
		listeners.remove(listener);
	}

	public static Set<String> getEnvironments()
	{
		Set<String> environments = new HashSet<String>();
		Collection<ProjectDatabaseManager> managers = DatabaseManager.getInstance().getAllProjectDatabaseManagers();
		for (ProjectDatabaseManager projectDatabaseManager : managers)
		{
			for (DatabaseDescriptor desc : projectDatabaseManager.getDatabaseDescriptors())
			{
				environments.add(desc.getName());
			}
		}
		environments.add(IDatabaseConstants.ENV_DEVELOPMENT);
		environments.add(IDatabaseConstants.ENV_PRODUCTION);
		environments.add(IDatabaseConstants.ENV_TEST);
		return environments;
	}

	void databaseChanged(IProject project)
	{
		for (IDatabaseListener listener : listeners)
		{
			listener.databaseSettingsChanged(project);
		}

	}

} // DatabaseManager
