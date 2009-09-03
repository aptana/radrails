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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchManager;
import org.osgi.framework.Bundle;
import org.radrails.server.core.Server;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.ide.server.core.IAbstractConfiguration;
import com.aptana.ide.server.core.IServer;
import com.aptana.ide.server.core.IServerType;
import com.aptana.ide.server.ui.views.actions.ICanAdd;

/**
 * @author Pavel Petrochenko
 */
public final class RailsServerType implements IServerType
{
	private Server s;

	public RailsServerType(Server s) 
	{
		this.s = s;
	}

	/**
	 * @see com.aptana.ide.server.core.IServerType#create(com.aptana.ide.server.core.IAbstractConfiguration)
	 */
	public IServer create(IAbstractConfiguration configuration) throws CoreException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see com.aptana.ide.server.core.IServerType#getDescription()
	 */
	public String getDescription()
	{
		return "RadRails Server"; //$NON-NLS-1$
	}

	/**
	 * @see com.aptana.ide.server.core.IServerType#getId()
	 */
	public String getId()
	{
		return s.getType();
	}

	/**
	 * @see com.aptana.ide.server.core.IServerType#getName()
	 */
	public String getName()
	{
		return getId();
	}

	/**
	 * @see com.aptana.ide.server.core.IServerType#hasServerConfiguration()
	 */
	public boolean hasServerConfiguration()
	{
		return false;
	}

	/**
	 * @see com.aptana.ide.server.core.IServerType#supportsLaunchMode(java.lang.String)
	 */
	public boolean supportsLaunchMode(String launchMode)
	{
		if (launchMode.equals(ILaunchManager.DEBUG_MODE) || launchMode.equals(ILaunchManager.RUN_MODE)) {
			return true;
		}
		if (launchMode.equals(ILaunchManager.PROFILE_MODE)) {
			Bundle bundle = Platform.getBundle("com.aptana.rdt.profiling");
			if (bundle == null) return false;
			if (bundle.getState() == Bundle.ACTIVE && !RubyRuntime.currentVMIsJRuby()) return true;
		}
		return false;
	}

	/**
	 * @see com.aptana.ide.server.core.IServerType#supportsPublish()
	 */
	public boolean supportsPublish()
	{
		return false;
	}

	/**
	 * @see com.aptana.ide.server.core.IServerType#supportsRestart(java.lang.String)
	 */
	public boolean supportsRestart(String launchMode)
	{
		return true;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter)
	{
		if (adapter==ICanAdd.class){
			return new AddServer();
		}
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/**
	 * @see com.aptana.ide.server.core.IServerType#getCategory()
	 */
	public String getCategory()
	{
		return "Web"; //$NON-NLS-1$
	}
	
	public boolean isExternal()
	{
		return true;
	}
}