/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.ide.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.update.ui.UpdateJob;
import org.eclipse.update.ui.UpdateManagerUI;

/**
 * Provides the hook to the update manager.
 * 
 * @author	mbaumbach
 * 
 * @version	0.6.1
 */
public class UpdateAction extends Action implements IAction {

	private IWorkbenchWindow window;
	
	public UpdateAction(IWorkbenchWindow window) {
		this.window = window;
		setId("org.radrails.newUpdates");
		setText("&Update RadRails...");
		setToolTipText("Search for updates to RadRails");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.radrails.ide.ui", "icons/rails.gif"));
		window.getWorkbench().getHelpSystem().setHelp(this, "org.radrails.updates");
	}
	
	public void run() {
		BusyIndicator.showWhile(window.getShell().getDisplay(), new Runnable() {
			public void run () {
				UpdateJob job = new UpdateJob("Searching for updates", false, false);
				UpdateManagerUI.openInstaller(window.getShell(), job);
			}
		});
	}
	
} // UpdateAction