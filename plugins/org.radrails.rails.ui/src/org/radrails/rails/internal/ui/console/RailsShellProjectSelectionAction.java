package org.radrails.rails.internal.ui.console;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.ui.RailsUIPlugin;

/**
 * RailsShellProjectSelectionAction
 */
public class RailsShellProjectSelectionAction extends Action implements IMenuCreator
{

	private RailsShell console;
	private Menu fMenu;

	/**			
	 * @param console
	 */
	public RailsShellProjectSelectionAction(RailsShell console)
	{
		this.console = console;
		setEnabled(!RailsPlugin.getRailsProjects().isEmpty());
		setToolTipText("Change active project for Rails Shell");
		setImageDescriptor(RailsUIPlugin.getImageDescriptor("icons/rails_project.png"));
		setMenuCreator(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener()
		{

			public void resourceChanged(IResourceChangeEvent event)
			{
				IResource source = event.getResource();
				if (source != null)
					return;
				IResourceDelta[] deltas = event.getDelta().getAffectedChildren(
						IResourceDelta.ADDED | IResourceDelta.CHANGED | IResourceDelta.REMOVED, IResource.PROJECT);
				if (deltas != null && deltas.length > 0)
				{
					// project changed
					Display.getDefault().asyncExec(new Runnable()
					{

						public void run()
						{
							if (fMenu != null)
								fMenu.dispose();
							fMenu = null;
							setEnabled(!RailsPlugin.getRailsProjects().isEmpty());
						}

					});

				}
			}

		}, IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose()
	{
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent)
	{
		if (fMenu != null)
		{
			fMenu.dispose();
		}

		fMenu = new Menu(parent);
		int accel = 1;
		Set<IProject> projects = RailsPlugin.getRailsProjects();
		for (IProject project : projects)
		{
			String label = project.getName();
			ImageDescriptor image = null;
			addActionToMenu(fMenu, new RailsProjectAction(label, image, project), accel);
			accel++;
		}
		return fMenu;
	}

	private void addActionToMenu(Menu parent, Action action, int accelerator)
	{
		if (accelerator < 10)
		{
			StringBuffer label = new StringBuffer();
			// add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
			label.append(action.getText());
			action.setText(label.toString());
		}

		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent)
	{
		return null;
	}

	private class RailsProjectAction extends Action
	{
		private IProject project;

		public RailsProjectAction(String label, ImageDescriptor image, IProject project)
		{
			setText(label);
			if (image != null)
			{
				setImageDescriptor(image);
			}
			this.project = project;
		}

		public void run()
		{
			console.setProject(project);
		}

		public void runWithEvent(Event event)
		{
			run();
		}
	}
}
