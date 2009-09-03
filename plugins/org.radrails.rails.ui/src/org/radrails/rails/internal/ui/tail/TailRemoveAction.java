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
 * Removes terminated tail windows from the console.
 * 
 * @author mkent
 * 
 */
public class TailRemoveAction extends Action {

	/**
	 * Constructor. Initializes the action with icon and tooltip.
	 */
	public TailRemoveAction() {
		ActionUtil.initAction(this, "removeall.gif",
				"Remove all terminated tails");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		TailConsoleManager.getInstance().removeTerminatedConsoles();
	}
}
