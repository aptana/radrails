/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.rails.internal.ui.railsplugins.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.radrails.rails.internal.ui.railsplugins.RailsPluginsView;

/**
 * Triggers a refresh of the list in RailsPluginsView.
 * 
 * @author mkent
 * 
 */
public class RefreshRailsPluginsActionDelegate implements IViewActionDelegate {

	private RailsPluginsView fView;

	public void init(IViewPart view) {
		fView = (RailsPluginsView) view;
	}

	public void run(IAction action) {
		fView.refreshPlugins();
	}

	public void selectionChanged(IAction action, ISelection selection) {}

}
