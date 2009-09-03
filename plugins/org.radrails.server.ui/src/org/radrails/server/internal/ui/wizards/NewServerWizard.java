/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.server.internal.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.radrails.rails.ui.wizards.NewProjectBasedResourceWizard;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.radrails.server.internal.ui.ServerUIPlugin;
import org.radrails.server.internal.ui.wizards.pages.WizardNewServerPage;

/**
 * Wizard to create a new Rails server.
 * 
 * @author mbaumbach
 * 
 */
public class NewServerWizard extends NewProjectBasedResourceWizard {

	WizardNewServerPage page1;

	/**
	 * Constructor.
	 */
	public NewServerWizard() {
		setWindowTitle("New Rails Server");
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		Server s = new Server(getProjectForName(page1.getProjectName()), // FIXME Have page just give us an IProject, not name!
				page1.getServerName(), page1.getServerType(), page1.getPort());
		ServerManager.getInstance().addServer(s);
		return true;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		ImageDescriptor image = AbstractUIPlugin.imageDescriptorFromPlugin(
				ServerUIPlugin.getInstance().getBundle().getSymbolicName(),
				"icons/server.gif");

		page1 = new WizardNewServerPage("page1");
		page1.setImageDescriptor(image);

		addPage(page1);
	}

}
