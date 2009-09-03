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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import org.radrails.server.core.IServerConstants;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;

import com.aptana.ide.server.core.IServer;
import com.aptana.ide.server.core.IServerManagerListener;
import com.aptana.ide.server.core.ServerManagerEvent;
import com.aptana.ide.server.core.model.IServerProviderDelegate;

/**
 * @author Pavel Petrochenko
 */
public class RailsServerProvider implements IServerProviderDelegate
{
	private HashSet<IServerManagerListener> listeners = new HashSet<IServerManagerListener>();
	private HashMap<Server, IServer> map = new HashMap<Server, IServer>();

	/**
	 * 
	 */
	public RailsServerProvider()
	{
		final ServerManager instance = ServerManager.getInstance();
		instance.addServerObserver(new Observer()
		{

			public void update(Observable o, Object arg)
			{

				Server s = (Server) o;
				if (arg.equals(IServerConstants.REMOVE))
				{
					IServer r = map.remove(s);
					fireEvent(new ServerManagerEvent(r, ServerManagerEvent.KIND_REMOVED));		
					return;
				}
				if (arg.equals(IServerConstants.ADD))
				{					
					RailsServer railsServer = new RailsServer(s);
					map.put(s, railsServer);
					fireEvent(new ServerManagerEvent(railsServer, ServerManagerEvent.KIND_ADDED));
					return;
				}
				IServer server = map.get(s);
				if (server != null)
					fireEvent(new ServerManagerEvent(server, ServerManagerEvent.KIND_CHANGED));
			}

		});

		Collection<Server> servers = ServerManager.getInstance().getServers();
		for (Server s : servers)
		{
			map.put(s, new RailsServer(s));
		}
	}

	/**
	 * @param serverManagerEvent
	 */
	protected void fireEvent(ServerManagerEvent serverManagerEvent)
	{
		for (IServerManagerListener l : listeners)
		{
			l.serversChanged(serverManagerEvent);
		}
	}

	/**
	 * @see com.aptana.ide.server.core.model.IServerProviderDelegate#addServerChangeListener(com.aptana.ide.server.core.IServerManagerListener)
	 */
	public void addServerChangeListener(IServerManagerListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * @see com.aptana.ide.server.core.model.IServerProviderDelegate#getServers()
	 */
	public IServer[] getServers()
	{
		IServer[] result = new IServer[map.size()];
		map.values().toArray(result);
		return result;
	}

	/**
	 * @see com.aptana.ide.server.core.model.IServerProviderDelegate#removeServerChangeListener(com.aptana.ide.server.core.IServerManagerListener)
	 */
	public void removeServerChangeListener(IServerManagerListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * @see com.aptana.ide.server.core.model.IServerProviderDelegate#isRemovable(com.aptana.ide.server.core.IServer)
	 */
	public boolean isRemovable(IServer server)
	{
		return true;
	}

	/**
	 * @see com.aptana.ide.server.core.model.IServerProviderDelegate#removeServer(com.aptana.ide.server.core.IServer)
	 */
	public void removeServer(IServer server)
	{
		ServerManager.getInstance().removeServer(((RailsServer) server).getServer());
	}

}
