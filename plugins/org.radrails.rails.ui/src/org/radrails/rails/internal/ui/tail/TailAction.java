/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.tail;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

/**
 * Action that creates a console and tails a file.
 *
 * @author	mbaumbach
 *
 * @version	0.3.1
 */
public class TailAction implements IActionDelegate {

	private ISelection selection;
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (!(selection instanceof StructuredSelection)) return;
		StructuredSelection sSelection = (StructuredSelection) selection;
		if (!(sSelection.getFirstElement() instanceof IFile)) return;
		IFile file = (IFile) sSelection.getFirstElement();

		TailConsole console = new TailConsole(file, "Tail [" + file.getProjectRelativePath().toString() + "]", null);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console } );
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
