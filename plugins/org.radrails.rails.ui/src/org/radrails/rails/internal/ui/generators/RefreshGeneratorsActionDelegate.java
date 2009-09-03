/*******************************************************************************
 * Copyright (c) 2006 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.generators;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Triggers a refresh of the list of available generators in the GeneratorsView.
 * 
 * @author mkent
 * 
 */
public class RefreshGeneratorsActionDelegate implements IViewActionDelegate {

	private IViewPart fView;
	
	public void init(IViewPart view) {
		fView = view;
	}

	public void run(IAction action) {
		GeneratorsView gv = (GeneratorsView) fView;
		gv.refreshGenerators();
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

}
