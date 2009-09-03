/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.server.internal.ui.console;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.Action;
import org.radrails.server.core.IServerConstants;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.radrails.server.internal.ui.ActionUtil;

/**
 * Stop action for ServerConsole.
 * 
 * @author Kyle
 * 
 */
public class ServerStopAction extends Action implements Observer {

	private IProcess process;
	private Server server;

	/**
	 * Constructor. Initializes action with icon and tooltip.
	 * 
	 * @param console
	 */
	public ServerStopAction(IProcess process) {
		this.process = process;
		ActionUtil.initAction(this, "stop.gif", "Stop server");		
		ServerManager.getInstance().addServerObserver(this);
		setEnablement(getServer());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		getServer().stop();
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
			if (!s.equals(getServer())) return;
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
