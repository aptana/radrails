/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.aptana.rdt.internal.profiling.menu;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.AbstractLaunchToolbarAction;
import org.eclipse.jface.action.IAction;

/**
 * This action delegate is responsible for producing the
 * Run > Profiling sub menu contents, which includes
 * an items to run last tool, favorite tools, and show the
 * profiling launch configuration dialog.
 */
public class ProfilingMenuDelegate extends AbstractLaunchToolbarAction {
	
	/**
	 * Creates the action delegate
	 */
	public ProfilingMenuDelegate() {
		super(IDebugUIConstants.ID_PROFILE_LAUNCH_GROUP);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.AbstractLaunchToolbarAction#getOpenDialogAction()
	 */
	protected IAction getOpenDialogAction() {
		IAction action= new OpenProfilingConfigurations();
		action.setActionDefinitionId("com.aptana.rdt.profiling.commands.OpenProfilingConfigurations"); //$NON-NLS-1$
		return action;
	}
}
