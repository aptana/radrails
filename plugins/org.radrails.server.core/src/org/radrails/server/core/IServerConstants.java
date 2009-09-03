/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.server.core;

/**
 * Constants for the server plugin.
 * 
 * @author mkent
 * 
 */
public interface IServerConstants {

	// Status contstants
	public static final String STARTING = "Starting...";

	public static final String STARTED = "Started";

	public static final String STOPPED = "Stopped";
	
	public static final String STOPPING = "Stopping...";

	// Server type constants
	public static final String TYPE_WEBRICK = "WEBrick";
	
	public static final String TYPE_LIGHTTPD = "LightTPD";
	
	public static final String TYPE_MONGREL = "Mongrel";

	// Port constants
	public static final String DEFAULT_WEBRICK_PORT = "3000";

	// Lifecycle constants
	public static final String ADD = "add";

	public static final String REMOVE = "remove";

	public static final String UPDATE = "update";

}
