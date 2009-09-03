/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.RailsInstallDialog;
import org.radrails.rails.internal.ui.wizards.pages.WizardNewRailsProjectPage;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.rails.ui.RailsUIPlugin;

/**
 * This wizard creates a new Rails project.
 * 
 * @author mkent
 * @author cwilliams
 */
public class NewRailsProjectWizard extends BasicNewProjectResourceWizard
{

	private WizardNewRailsProjectPage page;
	private RailsProjectCreator creator;

	/**
	 * Default constructor.
	 */
	public NewRailsProjectWizard()
	{
		setNeedsProgressMonitor(true);
		setWindowTitle("New Rails project");
		creator = null;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish()
	{
		if (railsNotInstalled() && page.getGenerateButtonSelection()) // asking to generate rails project, but rails
																		// isn't installed
		{
			UIJob dialog = new RailsInstallDialog(RailsUIPlugin.getInstance().getGemManager());
			dialog.schedule(); // TODO clear out error message if/when job finishes successfully?
			return true;
		}
		creator = new RailsProjectCreator(page);
		IRunnableWithProgress newProjectOp = new WorkspaceModifyDelegatingOperation(creator);

		// Run the project creation operation
		try
		{
			getContainer().run(false, true, newProjectOp);
		}
		catch (Exception e)
		{
			RailsUILog.logError("Error creating project", e);
			return false;
		}

		if (getNewProject() == null)
		{
			return false;
		}

		updatePerspective();
		selectAndReveal(getNewProject());

		return true;
	}

	@Override
	public IProject getNewProject()
	{
		if (creator == null)
			return null;
		return creator.getProject();
	}

	private boolean railsNotInstalled()
	{
		return RailsPlugin.getInstance().getRailsPath() == null;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages()
	{
		page = new WizardNewRailsProjectPage("new.rails.project1");
		page.setTitle(WizardMessages.NewRailsProjectWizardAction_text);
		page.setDescription(WizardMessages.NewRailsProjectWizardAction_description);
		addPage(page);
	}

	@Override
	protected void initializeDefaultPageImageDescriptor()
	{
		ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(RailsUIPlugin.getInstance().getBundle()
				.getSymbolicName(), "icons/newproj_wiz.gif");//$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}
}
