package org.radrails.rails.internal.ui.railsplugins.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.radrails.rails.core.railsplugins.RailsPluginDescriptor;
import org.radrails.rails.core.railsplugins.RailsPluginsManager;
import org.radrails.rails.internal.ui.RailsUIMessages;
import org.radrails.rails.internal.ui.railsplugins.RailsPluginsView;
import org.radrails.rails.ui.RailsUIPlugin;

/**
 * Removes a rails plugin
 * 
 * @author cwilliams
 */
public class RemovePluginActionDelegate implements IViewActionDelegate
{

	private RailsPluginDescriptor plugin;
	private RailsPluginsView view;

	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view)
	{
		plugin = null;
		this.view = (RailsPluginsView) view;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action)
	{
		if (plugin == null)
			return;
		final IProject project = RailsUIPlugin.getSelectedOrOnlyRailsProject();
		if (project == null || !project.exists())
		{
			MessageDialog.openError(view.getSite().getShell(), "No rails project selected",
					RailsUIMessages.SelectRailsProject_message);
			this.view.projectSelected(null);
			return;
		}
		RailsPluginsManager.removePlugin(project, plugin);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection)
	{
		if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)
				|| view.installTabSelected() || view.getProject() == null)
		{
			action.setEnabled(false);
			return;
		}
		IStructuredSelection struct = (IStructuredSelection) selection;
		plugin = (RailsPluginDescriptor) struct.getFirstElement();
		if (plugin != null)
			action.setEnabled(true);
	}

}
