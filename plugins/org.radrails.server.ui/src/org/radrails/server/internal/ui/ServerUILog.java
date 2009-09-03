/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.server.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.radrails.rails.core.RailsLog;

/**
 * Logs Server plugin messages
 * 
 * @author mkent
 */
public class ServerUILog extends RailsLog {

	/**
	 * Creates a status object to log.
	 * 
	 * @param severity -
	 *            the severity level
	 * @param code -
	 *            the status code
	 * @param message -
	 *            the message to log
	 * @param exception -
	 *            the exception to log
	 * 
	 * @return - a new status object
	 */
	protected static IStatus createStatus(int severity, int code, String message,
			Throwable exception) {
		return new Status(severity, ServerUIPlugin.getInstance().getBundle()
				.getSymbolicName(), code, message, exception);
	}

	/**
	 * Logs the status to ILog.log(status).
	 * 
	 * @param status -
	 *            the status to log
	 */
	protected static void log(IStatus status) {
		ServerUIPlugin.getInstance().getLog().log(status);
	}
}
