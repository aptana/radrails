/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.radrails.rails.internal.core.RailsPlugin;

/**
 * Logs operations for the Rails plugins.
 *
 * @author	mkent
 *
 * @version	0.3.1
 */
public class RailsLog {

	/**
	 * Logs a message and exception with severity INFO.
	 * 
	 * @param message -
	 *            the message to log
	 * @param e -
	 *            the exception to log
	 */
	public static void logInfo(String message, Throwable e) {
		log(createStatus(IStatus.INFO, IStatus.OK, message, e));
	}

	/**
	 * Logs a message and exception with severity WARNING.
	 * 
	 * @param message -
	 *            the message to log
	 * @param e -
	 *            the exception to log
	 */
	public static void logWarning(String message, Throwable e) {
		log(createStatus(IStatus.WARNING, IStatus.OK, message, e));
	}

	/**
	 * Logs a message and exception with severity ERROR.
	 * 
	 * @param message -
	 *            the message to log
	 * @param e -
	 *            the exception to log
	 */
	public static void logError(String message, Throwable e) {
		if (message == null) {
			if (e == null) {
				message = "";
			} else {
				message = e.getMessage();
				if (message == null) message = "";
			}
		}
		log(createStatus(IStatus.ERROR, IStatus.OK, message, e));
	}

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
		return new Status(severity, RailsPlugin.getInstance().getBundle()
				.getSymbolicName(), code, message, exception);
	}

	/**
	 * Logs the status to ILog.log(status).
	 * 
	 * @param status -
	 *            the status to log
	 */
	protected static void log(IStatus status) {
		RailsPlugin.getInstance().getLog().log(status);
	}

	public static void log(CoreException e) {
		RailsPlugin.getInstance().getLog().log(e.getStatus());
	}
	
	public static void log(String info) {
		RailsPlugin.getInstance().getLog().log(createStatus(IStatus.INFO, -1, info, null));
	}
	
	public static void log(Exception e) {
		logError(e.getLocalizedMessage(), e);
	}
	
} // RailsLog