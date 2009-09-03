/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.rails.internal.ui.tail;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * Page participant for the Tail console. Adds a stop action and a remove all
 * terminated action to the toolbar.
 * 
 * @author mkent
 * 
 */
public class TailConsolePageParticipant implements IConsolePageParticipant {

	private TailConsole console;

	private TailStopAction stopAction;

	private TailRemoveAction removeAction;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.console.IConsolePageParticipant#init(org.eclipse.ui.part.IPageBookViewPage,
	 *      org.eclipse.ui.console.IConsole)
	 */
	public void init(IPageBookViewPage page, IConsole console) {
		this.console = (TailConsole) console;
		stopAction = new TailStopAction(this.console);
		removeAction = new TailRemoveAction();
		
		// Add the actions to the toolbar
		IActionBars bars = page.getSite().getActionBars();
		bars.getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP,
				removeAction);
		bars.getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP,
				stopAction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.console.IConsolePageParticipant#dispose()
	 */
	public void dispose() {
		stopAction = null;
		removeAction = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.console.IConsolePageParticipant#activated()
	 */
	public void activated() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.console.IConsolePageParticipant#deactivated()
	 */
	public void deactivated() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

}
