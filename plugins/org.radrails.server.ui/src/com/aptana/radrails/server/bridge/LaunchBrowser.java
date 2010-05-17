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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;
import org.radrails.rails.ui.browser.BrowserUtil;
import org.radrails.server.core.Server;

import com.aptana.ide.server.core.IServer;

/**
 * @author Pavel Petrochenko
 */
public class LaunchBrowser implements IObjectActionDelegate
{
	private IStructuredSelection sel;

	/**
	 * LaunchBrowser default constructor
	 */
	public LaunchBrowser()
	{
	}

	/**
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{

	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action)
	{
		if (sel == null || sel.getFirstElement() == null)
		{
			return;
		}
		final Server server = (Server) ((RailsServer) sel.getFirstElement()).getServer();
		UIJob job = new UIJob("Opening browser")
		{

			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				try
				{
					if (server == null)
					{
						return Status.CANCEL_STATUS;
					}
					String port = String.valueOf(server.getPort());
					BrowserUtil.openBrowser("http://" + server.getBrowserHost() + ":" + port);
				}
				catch (Exception e)
				{
					return new Status(Status.ERROR, "com.aptana.radrails.server.bridge", -1, e.getMessage(), e);
				}
				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection)
	{
		if (selection instanceof IStructuredSelection)
		{
			sel = (IStructuredSelection) selection;
			IServer server = (IServer) ((IStructuredSelection) selection).getFirstElement();
			if (server != null)
			{
				action.setEnabled(server.getServerState() == IServer.STATE_STARTED);
			}
			else
			{
				action.setEnabled(false);
			}
		}
	}

}
