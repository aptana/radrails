/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.rails.internal.ui.tail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

/**
 * Manages the active Tail consoles.
 * 
 * @author mkent
 * 
 */
public class TailConsoleManager {

	private static TailConsoleManager instance;

	private List<TailConsole> consoles;

	private TailConsoleManager() {
		consoles = new ArrayList<TailConsole>();
	}

	/**
	 * @return the singleton instance of the manager
	 */
	public static TailConsoleManager getInstance() {
		if (instance == null) {
			instance = new TailConsoleManager();
		}
		return instance;
	}

	/**
	 * Adds a console to the console manager.
	 * 
	 * @param console
	 *            the console to add
	 */
	public void addConsole(TailConsole console) {
		consoles.add(console);
	}

	/**
	 * Removes all terminated Tail consoles from the ConsolePlugin console
	 * manager and from this manager.
	 */
	public void removeTerminatedConsoles() {
		List<TailConsole> toRemove = new ArrayList<TailConsole>();
		for (TailConsole c : consoles) {			
			if (!c.isStopped()) continue;
			ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] { c });
			toRemove.add(c);
		}
		// Remove the tail if it's stopped
		for (TailConsole console : toRemove) {
			consoles.remove(console);
		}
	}
	
	public void stopAll() {
		for (TailConsole console : consoles) {
			console.stopTail();
		}
	}

}
