/**
 * This file Copyright (c) 2005-2007 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.radrails.server.bridge;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.radrails.rails.ui.RailsUIPlugin;
import org.radrails.server.core.IServerConstants;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.radrails.server.internal.ui.ServerUIPlugin;
import org.radrails.server.ui.dialogs.EditServerDialog;

import com.aptana.ide.core.IdeLog;
import com.aptana.ide.core.StringUtils;
import com.aptana.ide.server.ServerCore;
import com.aptana.ide.server.core.ILog;
import com.aptana.ide.server.core.IModule;
import com.aptana.ide.server.core.IServer;
import com.aptana.ide.server.core.impl.Configuration;
import com.aptana.ide.server.core.impl.servers.AbstractServer;
import com.aptana.ide.server.ui.views.actions.ICanEdit;

/**
 * @author Pavel Petrochenko
 */
public class RailsServer extends AbstractServer
{

	private static final IProcess[] NO_PROCESS = new IProcess[0];

	/**
	 * @see com.aptana.ide.server.core.impl.servers.AbstractServer#getServerState()
	 */
	@Override
	public int getServerState()
	{
		String status = srv.getStatus();
		if (status.equals(IServerConstants.STARTING))
		{
			return IServer.STATE_STARTING;
		}
		if (status.equals(IServerConstants.STARTED))
		{
			return IServer.STATE_STARTED;
		}
		if (status.equals(IServerConstants.STOPPING))
		{
			return IServer.STATE_STOPPING;
		}
		if (status.equals(IServerConstants.STOPPED))
		{
			return IServer.STATE_STOPPED;
		}
		return super.getServerState();
	}

	private Server srv;
	private ILog log;

	/**
	 * @see com.aptana.ide.server.core.impl.servers.AbstractServer#getName()
	 */
	@Override
	public String getName()
	{
		return srv.getName();
	}

	/**
	 * @param s
	 */
	public RailsServer(Server s)
	{
		super(new RailsServerType(s), new RailsServerConfiguration(s));
		this.srv = s;
		this.log = new RailsServerLog(s);
	}

	/**
	 * @see com.aptana.ide.server.core.impl.servers.AbstractServer#restart(java.lang.String,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus restart(final String mode, IProgressMonitor monitor)
	{
		Display.getDefault().syncExec(new Runnable()
		{

			public void run()
			{
				// srv.updateRunMode(mode);
				srv.restart();
			}

		});
		return Status.OK_STATUS;
	}

	/**
	 * @see com.aptana.ide.server.core.impl.servers.AbstractServer#start(java.lang.String,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus start(final String mode, IProgressMonitor monitor)
	{
		Display.getDefault().syncExec(new Runnable()
		{

			public void run()
			{
				srv.updateRunMode(mode);
				srv.start();
			}

		});
		return Status.OK_STATUS;
	}

	/**
	 * @see com.aptana.ide.server.core.impl.servers.AbstractServer#stop(boolean,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus stop(boolean force, IProgressMonitor monitor)
	{
		srv.stop();
		return Status.OK_STATUS;
	}

	/**
	 * @see com.aptana.ide.server.core.IServer#canHaveModule(com.aptana.ide.server.core.IModule)
	 */
	public IStatus canHaveModule(IModule module)
	{
		return new Status(IStatus.ERROR, ServerCore.PLUGIN_ID, IStatus.ERROR, Messages.RailsServer_ERROR, null);
	}

	/**
	 * @see com.aptana.ide.server.core.impl.servers.AbstractServer#getConfigurationDescription()
	 */
	public String getConfigurationDescription()
	{
		return StringUtils.format(Messages.RailsServer_DESCRIPTION, new Object[] { srv.getProject().getName(),
				srv.getEnvironment() });
	}

	/**
	 * @see com.aptana.ide.server.core.IServer#getLog()
	 */
	public ILog getLog()
	{
		return log;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter)
	{
		if (adapter == ICanEdit.class)
		{
			return new ICanEdit()
			{

				public void doEdit()
				{
					if (srv.isStopped())
					{
						// Display a dialog to edit server options: name, port,
						// environment
						EditServerDialog dialog = new EditServerDialog(Display.getCurrent().getActiveShell(), srv
								.getName(), srv.getType(), srv.getHost(), srv.getPort(), srv.getEnvironment());
						int returnCode = dialog.open();

						if (returnCode == Window.OK)
						{
							if (!srv.getType().equals(dialog.getType()))
								srv.updateType(dialog.getType());
							if (!srv.getHost().equals(dialog.getHost()))
								srv.updateHost(dialog.getHost());
							if (!srv.getName().equals(dialog.getName()))
								srv.updateName(dialog.getName());
							if (!srv.getPort().equals(dialog.getPort()))
								srv.updatePort(dialog.getPort());
							if (!srv.getEnvironment().equals(dialog.getEnvironment()))
								srv.updateEnvironment(dialog.getEnvironment());
							ServerManager.getInstance().saveServers();
						}
					}

				}

			};
		}
		return Platform.getAdapterManager().getAdapter(srv, adapter);
	}

	/**
	 * @return underlying server
	 */
	public Server getServer()
	{
		return srv;
	}

	/**
	 * @see com.aptana.ide.server.core.impl.servers.AbstractServer#isExternal()
	 */
	public boolean isExternal()
	{
		return true;
	}

	/**
	 * @see com.aptana.ide.server.core.impl.servers.AbstractServer#getProcesses()
	 */
	public IProcess[] getProcesses()
	{
		IProcess process = srv.getProcess();
		if (process != null)
		{
			return new IProcess[] { process };
		}
		return NO_PROCESS;
	}

	/**
	 * @see com.aptana.ide.server.core.IServer#getHost()
	 */
	public String getHost()
	{
		return StringUtils.format("{0}:{1}", new String[] { srv.getHost(), srv.getPort() }); //$NON-NLS-1$
	}

	/**
	 * @see com.aptana.ide.server.core.IServer#getPort()
	 */
	public int getPort()
	{
		try
		{
			return Integer.parseInt(srv.getPort());
		}
		catch (NumberFormatException e)
		{
			IdeLog.logInfo(ServerUIPlugin.getInstance(), e.getMessage(), e);
			return 3000;
		}
	}

	/**
	 * @see com.aptana.ide.server.core.IServer#getDocumentRoot()
	 */
	public IPath getDocumentRoot()
	{
		return null;
	}

	/**
	 * @see com.aptana.ide.server.core.IServer#getHostname()
	 */
	public String getHostname()
	{
		return getHost();
	}

	public String fetchStatistics()
	{
		return null;
	}

	private static class RailsServerConfiguration extends Configuration
	{
		public RailsServerConfiguration(Server s)
		{
			setStringAttribute(IServer.KEY_NAME, s.getName());
			setStringAttribute(IServer.KEY_ID, com.aptana.ide.server.core.impl.servers.ServerManager.getFreeId());
			setIntAttribute(IServer.KEY_PORT, s.getPortInt());
			setStringAttribute(IServer.KEY_ASSOCIATION_SERVER_ID, "");
		}
	}
}
