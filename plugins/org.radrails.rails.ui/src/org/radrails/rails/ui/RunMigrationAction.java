package org.radrails.rails.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.aptana.rdt.rake.IRakeHelper;
import com.aptana.rdt.rake.RakePlugin;

/**
 * @author matt
 */
public class RunMigrationAction implements IObjectActionDelegate
{

	private IWorkbenchPart fPart;

	public RunMigrationAction()
	{
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
		fPart = targetPart;
	}

	public void run(IAction action)
	{
		IStructuredSelection sel = (IStructuredSelection) fPart.getSite().getSelectionProvider().getSelection();
		if (sel == null)
			return;
		Object element = sel.getFirstElement();
		final IResource file = getFile(element);
		String fileName = file.getName();
		// get the migration version number from the file name
		String version = fileName.substring(0, fileName.indexOf('_'));
		try
		{
			int versionNum = Integer.parseInt(version);
			final String params = "VERSION=" + versionNum;
			Job job = new Job("Running db:migrate")
			{
				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					getRakeHelper().runRakeTask(file.getProject(), "db:migrate", params, monitor);
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
		catch (NumberFormatException e)
		{
			RailsUILog.logError("Invalid migration number", e);
			MessageDialog.openError(fPart.getSite().getShell(), "Error running migration", "Invalid migration number: "
					+ version);
		}
	}

	private IRakeHelper getRakeHelper()
	{
		return RakePlugin.getDefault().getRakeHelper();
	}

	private IResource getFile(Object element)
	{
		if (element instanceof IResource)
			return (IResource) element;
		if (element instanceof IAdaptable)
		{
			IAdaptable adapt = (IAdaptable) element;
			return (IResource) adapt.getAdapter(IResource.class);
		}
		return null;
	}

	public void selectionChanged(IAction action, ISelection selection)
	{

	}

}
