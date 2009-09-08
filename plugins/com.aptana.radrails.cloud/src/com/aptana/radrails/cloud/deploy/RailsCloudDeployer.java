package com.aptana.radrails.cloud.deploy;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.console.RailsShellFactory;

import com.aptana.ide.core.model.user.AptanaUser;
import com.aptana.ide.core.model.user.User;
import com.aptana.ide.server.cloud.services.model.Event;
import com.aptana.ide.server.cloud.services.model.Site;
import com.aptana.ide.server.cloud.services.model.studio.StudioSite;
import com.aptana.ide.server.cloud.syncing.ICloudDeployer;
import com.aptana.radrails.cloud.Activator;
import com.aptana.radrails.cloud.internal.CloudUtil;
import com.aptana.radrails.cloud.internal.EndpointDialog;
import com.aptana.radrails.cloud.shell.AptanaCloudCommandProvider;

public class RailsCloudDeployer implements ICloudDeployer
{

	private static final String CAPFILE = CloudUtil.CAPFILE;

	public boolean supports(StudioSite site)
	{
		return site != null && site.isRails();
	}

	/**
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the user. It is the caller's responsibility to
	 *            call done() on the given monitor. Accepts <code>null</code>, indicating that no progress should be
	 *            reported and that the operation cannot be cancelled.
	 * @return IStatus indicating if the deployment was successful.
	 */
	public IStatus deploy(final StudioSite site, Endpoint target, IProgressMonitor monitor)
	{
		monitor.beginTask(Messages.RailsCloudDeployer_LBL_Deploying_job_title, 100);
		IProject project = site.getProject().getLocalProject();

		// Prompt user to make sure they want to deploy and to choose endpoint.
		final Endpoint defaultChoice = target;
		final int[] dialogExit = new int[1];
		final Endpoint[] endpointChoice = new Endpoint[1];
		final String[] comment = new String[1];
		Display.getDefault().syncExec(new Runnable()
		{

			public void run()
			{
				EndpointDialog dialog = new EndpointDialog(Display.getDefault().getActiveShell(), defaultChoice);
				dialogExit[0] = dialog.open();
				endpointChoice[0] = dialog.getEndpoint();
				comment[0] = dialog.getComment();
			}
		});
		if (dialogExit[0] != IDialogConstants.OK_ID)
			return Status.CANCEL_STATUS;

		target = endpointChoice[0];

		monitor.worked(2);

		// Now actually start the deploy process...
		IProgressMonitor sub = new SubProgressMonitor(monitor, 28);
		IStatus status = CloudUtil.installCloudGemIfNecessary(sub);
		sub.done();
		if (!status.isOK())
			return status;

		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;

		// Force open Rails Shell
		new RailsShellFactory().openConsole();
		monitor.worked(1);

		sub = new SubProgressMonitor(monitor, 19);
		status = checkProjectHasBeenApCloudified(site, project, sub);
		sub.done();
		if (!status.isOK())
			return status;

		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;

		String targetName = AptanaCloudCommandProvider.PUBLIC;
		if (target == Endpoint.STAGING)
			targetName = AptanaCloudCommandProvider.STAGING;
		try
		{
			ILaunch launch = CloudUtil.run(AptanaCloudCommandProvider.APCLOUD, targetName
					+ " " + AptanaCloudCommandProvider.APTANA_DEPLOY, //$NON-NLS-1$
					project, CloudUtil.getEnvMap(site), false);
			while (!launch.isTerminated())
			{
				if (monitor.isCanceled())
				{
					launch.terminate();
					return Status.CANCEL_STATUS;
				}

				Thread.yield();
			}
			monitor.worked(45);
			// Check exit status
			if (launch.getProcesses() != null && launch.getProcesses()[0] != null
					&& launch.getProcesses()[0].getExitValue() != 0)
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Deployment failed", null);
		}
		catch (CoreException e)
		{
			return e.getStatus();
		}
		createEvent(site, endpointChoice[0], comment[0]);
		monitor.done();
		return Status.OK_STATUS;
	}

	private IStatus checkProjectHasBeenApCloudified(StudioSite site, IProject project, IProgressMonitor monitor)
	{
		IPath railsRoot = RailsPlugin.findRailsRoot(project);
		IFile file = project.getFile(railsRoot.append(CAPFILE));
		if (file.exists())
			return Status.OK_STATUS;

		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
		try
		{
			ILaunch launch = CloudUtil.run(AptanaCloudCommandProvider.APCLOUDIFY,
					".", project, CloudUtil.getEnvMap(site), true); //$NON-NLS-1$
			// We need to wait until this process/launch finishes!
			while (!launch.isTerminated())
			{
				if (monitor.isCanceled())
				{
					launch.terminate();
					return Status.CANCEL_STATUS;
				}
				// TODO Should we ever time out? It does interactively ask for input...
				Thread.yield();
			}
			// Check exit status
			if (launch.getProcesses() != null && launch.getProcesses()[0] != null
					&& launch.getProcesses()[0].getExitValue() != 0)
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Running apcloudify on project failed", null);
		}
		catch (CoreException e)
		{
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

	/**
	 * Creates an event and sends it to cloud to let cloud know we've done the equivalent of a sync for the site.
	 * 
	 * @param site
	 * @param comment
	 */
	private void createEvent(StudioSite site, Endpoint target, String comment)
	{
		User user = AptanaUser.getSignedInUser();
		String domainName = site.getPrimaryDomainName();
		if (target == Endpoint.STAGING)
			domainName = site.getStagingDomainName();
		String subject = MessageFormat.format(Messages.RailsCloudDeployer_TTL_Deploy_event_subject, user.getUsername(),
				domainName);
		StringBuilder buffer = new StringBuilder(subject);
		if (comment != null && comment.length() > 0)
		{
			buffer.append("\n"); //$NON-NLS-1$
			buffer.append(Messages.RailsCloudDeployer_MSG_Deploy_event_comment_header);
			buffer.append("\n"); //$NON-NLS-1$
			buffer.append(comment);
			buffer.append("\n\n"); //$NON-NLS-1$
		}
		Event event = new Event(subject, buffer.toString());
		event.setId(Integer.toString(Site.SITE_SYNCED));
		event.setLocation(site.getEvents().getLocation());
		event.setRequestBuilder(site.getRequestBuilder());
		event.setServiceProvider(site.getServiceProvider());
		event.setLogger(site.getLogger());
		event.commit();
		site.updateLastSync();
		site.getEvents().updateMaxSizeWithSeverities();
	}
}
