/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.ui;

import org.radrails.rails.core.IRailsConstants;

/**
 * Constants for the UI plugin.
 * 
 * @author mkent
 * 
 */
public interface IRailsUIConstants {

	public static final String ID_SERVERS_VIEW = "com.aptana.ide.server.ui.serversView";
	
	public static final String ID_GENERATORS_VIEW = "org.radrails.rails.ui.ViewGenerators";
	
	/**
	 * @deprecated Please use {@link IRakeUIConstants#ID_RAKE_VIEW}
	 */
	public static final String ID_RAKE_VIEW = "org.radrails.rails.ui.ViewRakeTasks";
	
	public static final String ID_BROWSER_VIEW = "org.radrails.rails.ui.browser.BrowserView";
	
	public static final String ID_RAILS_PLUGINS_VIEW = "org.radrails.rails.ui.ViewRailsPlugins";
	
	public static final String ID_RAILS_PERSPECTIVE = "org.radrails.rails.ui.PerspectiveRails";
	
	public static final String RAILS_PROJECT_NATURE = IRailsConstants.RAILS_PROJECT_NATURE;

    /**
     * @deprecated This view has been removed!
     */
	public static final String ID_RAILS_NAVIGATOR = "org.radrails.rails.ui.ViewRailsNavigator";
}
