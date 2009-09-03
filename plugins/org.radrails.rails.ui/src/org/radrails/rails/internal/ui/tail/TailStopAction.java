/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.rails.internal.ui.tail;

import org.eclipse.jface.action.Action;

/**
 * Stops a Tail.
 * 
 * @author mkent
 * 
 */
public class TailStopAction extends Action {

	private TailConsole console;

	/**
	 * Constructor. Initializes the action with icon and tooltip.
	 * 
	 * @param console
	 */
	public TailStopAction(TailConsole console) {
		ActionUtil.initAction(this, "nav_stop.gif", "Stop tail");
		this.console = console;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		console.stopTail();
		setEnabled(false);
	}
}
