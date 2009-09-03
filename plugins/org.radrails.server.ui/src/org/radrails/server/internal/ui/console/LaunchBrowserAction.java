package org.radrails.server.internal.ui.console;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.progress.UIJob;
import org.radrails.rails.ui.browser.BrowserUtil;
import org.radrails.server.core.IServerConstants;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.radrails.server.internal.ui.ActionUtil;
import org.radrails.server.internal.ui.ServerUIPlugin;

public class LaunchBrowserAction extends Action implements Observer {
	
	private IProcess process;
	private Server server;

	/**
	 * Constructor. Initializes action with icon and tooltip.
	 * 
	 * @param process
	 */
	public LaunchBrowserAction(IProcess process) {
		this.process = process;
		ServerManager.getInstance().addServerObserver(this);
		ActionUtil.initAction(this, "browser.gif", "Launch Browser");
		Server server = getServer();
		if (server != null && server.isStarted()) {
			setEnablement(server);
		} else {
			setEnabled(false);
		}
	}
	
	private Server getServer() {
		if (this.server != null) return this.server;
		Server server = ServerManager.getInstance().findByProcess(process);
		if (server != null) this.server = server;
		return server;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		UIJob job = new UIJob("Opening browser") {
			
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					Server s = getServer();
					if (s == null) return Status.CANCEL_STATUS;
					String port = String.valueOf(s.getPort());		
					BrowserUtil.openBrowser("http://" + s.getBrowserHost() + ":" + port);
				} catch (Exception e) {
					return new Status(Status.ERROR, ServerUIPlugin.getUniqueIdentifier(), -1, e.getMessage(), e);
				}
				return Status.OK_STATUS;
			}
		
		};
		job.schedule();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		if (getServer() == null) {
			setEnabled(false);
			return;
		}
		String action = (String) arg;
		if (action.equals(IServerConstants.UPDATE)) {
			Server s = (Server) o;
			if (!s.equals(getServer()))
				return;
			setEnablement(s);
		}
	}

	private void setEnablement(Server s) {
		if (s == null) {
			setEnabled(false);
			return;
		}
		if (s.isStopped() || s.isStopping()) {
			setEnabled(false);
			final Observer self = this;
			Job job = new Job("Remove server observer"){
			
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					ServerManager.getInstance().deleteServerObserver(self);
					return Status.OK_STATUS;
				}
			
			};
			job.setSystem(true);
			job.schedule();			
		} else {
			setEnabled(true);
		}
	}
}
