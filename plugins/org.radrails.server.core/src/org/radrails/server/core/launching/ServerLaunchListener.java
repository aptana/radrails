package org.radrails.server.core.launching;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.server.core.IServerConstants;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerLog;
import org.radrails.server.core.ServerManager;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;

/**
 * This class listens to launches and tries to hook up launches of servers to the Server object in the ServerManager.
 * This de-coupling allows us to handle servers launched form the Run or Debug menu equivalent to those launched from 
 * the Servers view.
 * 
 * TODO Still need to handle a server launched via Run/Debug menus that has no corresponding server in the ServerManager. Create one?
 * @author Chris Williams
 *
 */
public class ServerLaunchListener implements ILaunchListener {

	public void launchRemoved(ILaunch launch) {		
	}

	public void launchChanged(final ILaunch launch) {		
		final Server matching = grabLaunchedServer(launch);
		if (matching == null) return;
		// a server's launch now has it's processes, attach the server to the process
		Job job = new Job("")
		{
			protected IStatus run(IProgressMonitor monitor)
			{
				matching.started(launch.getProcesses()[0]);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	public void launchAdded(ILaunch launch) {
		Server matching = grabLaunchedServer(launch);
		if (matching == null) return;
		// a server has been launched, set it's status to started and alert listeners
		matching.updateStatus(IServerConstants.STARTING);
	}
	
	private Server grabLaunchedServer(ILaunch launch) {
		try {
			ILaunchConfiguration config = launch.getLaunchConfiguration();
			if (config == null) return null;
			String typeId = config.getType().getIdentifier();
			if (typeId.equals(IRailsAppLaunchConfigurationConstants.LAUNCH_TYPE_ID)) {
				return null;
			}
			String filename = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, (String)null);		
			if (!looksLikeServerLaunchFile(filename)) return null;
			String projectName = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			Collection<Server> servers = ServerManager.getInstance().getServersForProject(project);
			return findMatchingServer(config, servers);
		} catch (CoreException e) {
			ServerLog.log(e);
		}
		return null;
	}

	/**
	 * Determine if a filename corresponds to file that launches Rails servers...
	 * @param filename
	 * @return
	 */
	private boolean looksLikeServerLaunchFile(String filename) {	
		if (filename == null) return false;
		if (filename.endsWith("gem")) return false; // Specifically avoid doing any more work if this is a launch of rubygems!
		if (filename.endsWith("script/server") || filename.endsWith("script\\server"))return true; // normal rails script/server
		// If we do a pure comparison with mongrel path we can get into an infinite loop if this launch is a rubygems one to get the rubygems installation path
		// we should narrow the comparison more to avoid that
		return filename.endsWith("mongrel_rails") && filename.equals(RailsPlugin.getInstance().getMongrelPath());
	}

	private Server findMatchingServer(ILaunchConfiguration config, Collection<Server> servers) {
		if (servers == null || servers.isEmpty()) return null;
		if (servers.size() == 1) return servers.iterator().next();
		try {
			String args = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
			for (Server server : servers) {
				// Check the args and find the one who's environment/port/type match
				if (args.equals(server.getProgramArguments())) {
					return server;
				}
			}
		} catch (CoreException e) {
			ServerLog.log(e);
		}
		return null;
	}

}
