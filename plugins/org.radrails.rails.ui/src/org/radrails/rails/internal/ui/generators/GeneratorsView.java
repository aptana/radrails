/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.generators;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.RailsUIMessages;
import org.radrails.rails.internal.ui.actions.RailsProjectSelectionAction;
import org.radrails.rails.ui.RailsUIPlugin;
import org.rubypeople.rdt.ui.IPackagesViewPart;

import com.aptana.ide.editors.unified.UnifiedMessagePage;

/**
 * The Generators view provides an interface to the Rails generate script and the following generators.
 * <ul>
 * <li>controller</li>
 * <li>mailer</li>
 * <li>migration</li>
 * <li>model</li>
 * <li>plugin</li>
 * <li>scaffold</li>
 * <li>web_service</li>
 * 
 * @author mkent
 */
public class GeneratorsView extends PageBookView
{

	private GeneratorsPage generatorsPage;

	private UnifiedMessagePage messagePage;

	private RailsProjectSelectionAction selectProjectAction;

	/**
	 * @see org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part.PageBook)
	 */
	protected IPage createDefaultPage(PageBook book)
	{
		createMessagePage(book);
		createGeneratorsPage(book);

		IPage page = null;
		if (getSelectedProject() == null)
		{
			page = messagePage;
		}
		else
		{
			page = generatorsPage;
		}

		if (generatorsPage.pulldownEmpty())
		{
			refreshGenerators();
		}
		return page;
	}

	private static IProject getSelectedProject()
	{
		IProject proj = RailsUIPlugin.getSelectedOrOnlyRailsProject();
		if (proj != null) return proj;
		Set<IProject> projects = RailsPlugin.getRailsProjects();
		if (projects == null || projects.size() == 0) return null;
		return projects.iterator().next();
	}

	/**
	 * @see org.eclipse.ui.part.PageBookView#init(org.eclipse.ui.IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);
		selectProjectAction = new RailsProjectSelectionAction();
		site.getActionBars().getToolBarManager().add(selectProjectAction);
	}

	private void createMessagePage(PageBook book)
	{
		messagePage = new UnifiedMessagePage();
		messagePage.createControl(book);
		messagePage.setMessage(RailsUIMessages.SelectRailsProject_message);
	}

	private void createGeneratorsPage(PageBook book)
	{
		generatorsPage = new GeneratorsPage();
		initPage(generatorsPage);
		generatorsPage.createControl(book);
		selectProjectAction.setListener(generatorsPage);
	}

	/**
	 * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart)
	 */
	protected PageRec doCreatePage(IWorkbenchPart part)
	{
		if (part instanceof IPackagesViewPart)
		{
			createGeneratorsPage(getPageBook());
			return new PageRec(part, generatorsPage);
		}
		return null;
	}

	/**
	 * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.ui.part.PageBookView.PageRec)
	 */
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord)
	{
		pageRecord.page.dispose();
		pageRecord.dispose();
		selectProjectAction.setListener(null);
	}

	/**
	 * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
	 */
	protected IWorkbenchPart getBootstrapPart()
	{
		IWorkbenchPage page = getSite().getPage();
		if (page != null)
		{
			return page.getActiveEditor();
		}
		return null;
	}

	/**
	 * @see org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart)
	 */
	protected boolean isImportant(IWorkbenchPart part)
	{
		return part instanceof IPackagesViewPart;
	}

	/**
	 * Refreshes the generators
	 */
	public void refreshGenerators()
	{
		generatorsPage.refreshGenerators();
	}

	/**
	 * @see org.eclipse.ui.part.PageBookView#dispose()
	 */
	public void dispose()
	{
		super.dispose();
	}

}
