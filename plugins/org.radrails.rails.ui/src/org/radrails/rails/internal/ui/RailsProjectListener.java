package org.radrails.rails.internal.ui;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import org.radrails.rails.core.RailsLog;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.ui.RailsUIPlugin;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyCore;

import com.aptana.ide.editors.UnifiedEditorsPlugin;

/**
 * Listen to resource changes to try and detect imported Rails projects. If found, add Rails nature.
 * 
 * @author Chris Williams
 */
public class RailsProjectListener implements IResourceChangeListener
{

	public void resourceChanged(IResourceChangeEvent event)
	{
		if (event == null)
			return;
		if (event.getType() == IResourceChangeEvent.PRE_DELETE)
		{
			IProject project = (IProject) event.getResource();
			if (RailsPlugin.hasRailsNature(project))
			{
				railsProjectRemoved(project);
			}
			return;
		}
		if (event.getType() == IResourceChangeEvent.POST_CHANGE)
		{
			IResourceDelta delta = event.getDelta();
			checkDelta(delta);
		}
	}

	private void railsProjectRemoved(IProject project)
	{
		// Remove the DB connections
		// TODO When/if the RailsDBConnector is working properly then uncomment this.
		// RailsDBConnector.removeProjectDBConnections(project);
		cleanProfile(project.getLocation());
	}

	private void cleanProfile(final IPath path)
	{
		Job job = new UIJob("Clean profile")
		{

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				String profilePath = path.toFile().toURI().toString();
				if (profilePath.endsWith("/") || profilePath.endsWith("\\"))
				{
					profilePath = profilePath.substring(0, profilePath.length() - 1);
				}
				UnifiedEditorsPlugin.getDefault().getProfileManager().removeProfile(profilePath);
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	private void checkDelta(IResourceDelta delta)
	{
		if (delta == null)
			return;
		IResource resource = delta.getResource();
		if (!(resource instanceof IContainer))
			return; // ignore files, but traverse workspace root, project root and folders
		if (resource instanceof IProject || resource instanceof IFolder)
		{
			if (IResourceDelta.REMOVED == delta.getKind())
				return;
			// Can't do just added, because "import existing folder
			// as new project" doesn't have subfolders imported by
			// time add project comes in (also we miss marking it as
			// rails project)
			final IContainer rootFolder = (IContainer) resource;
			final IProject project = rootFolder.getProject();
			if (RailsPlugin.hasRailsNature(project))
				return;
			if (hasRailsLayout(rootFolder))
			{
				Job job = new RailsNatureAdder(project);
				job.schedule(500);
				return;
			}
		}
		IResourceDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; i++)
		{
			checkDelta(children[i]);
		}

	}

	private boolean hasRailsLayout(final IContainer folder)
	{
		return folderExists(folder, "app") && folderExists(folder, "lib") && folderExists(folder, "script")
				&& folderExists(folder, "db") && folderExists(folder, "vendor") && folderExists(folder, "config")
				&& folderExists(folder, "public") && folderExists(folder, "test");
	}

	private boolean folderExists(IContainer rootFolder, String name)
	{
		IFolder folder = rootFolder.getFolder(new Path(name));
		return folder != null && folder.exists();
	}

	private static class RailsNatureAdder extends Job
	{
		private IProject project;

		public RailsNatureAdder(IProject project)
		{
			super("Add Rails Nature");
			this.project = project;
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor)
		{
			try
			{
				RailsPlugin.addRailsNature(project, monitor);
				IRubyProject rubyProject = RubyCore.create(project);
				if (!RailsUIPlugin.hasRailsFrozenInVendor(rubyProject)) // only add gems to loadpath if
					// rails isn't frozen to vendor
					RailsUIPlugin.addDefaultRailsLoadpaths(rubyProject, monitor);
				RailsUIPlugin.overrideDocumentRoot(project);
			}
			catch (CoreException e)
			{
				RailsLog.log(e);
			}
			return Status.OK_STATUS;
		}
	}
}