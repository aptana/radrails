/*******************************************************************************
 * Copyright (c) 2006 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.PlatformUI;
import org.radrails.rails.internal.ui.RailsUIMessages;
import org.radrails.rails.ui.RailsUIPlugin;
import org.radrails.rails.ui.test.TestLauncher;

public class RunTestsPulldownDelegate implements IWorkbenchWindowPulldownDelegate2
{

	private IWorkbenchWindow fWindow;
	private Menu fMenu;

	public Menu getMenu(Control parent)
	{
		setMenu(new Menu(parent));
		fillMenu(fMenu);
		return fMenu;
	}
	
	public Menu getMenu(Menu parent)
	{
		setMenu(new Menu(parent));
		fillMenu(fMenu);
		return fMenu;
	}
	
	private void fillMenu(Menu m)
	{
		createUnitMenu(m);
		createFunctionalMenu(m);
		createIntegrationMenu(m);		
	}

	/**
	 * Sets this action's drop-down menu, disposing the previous menu.
	 * 
	 * @param menu the new menu
	 */
	private void setMenu(Menu menu) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fMenu = menu;
	}

	public void dispose()
	{

	}

	public void init(IWorkbenchWindow window)
	{
		fWindow = window;
	}

	public void run(IAction action)
	{
		runTests("all", "run_tests.rb");
	}

	public void selectionChanged(IAction action, ISelection selection)
	{
	}

	private void createUnitMenu(Menu parent)
	{
		MenuItem unit = new MenuItem(parent, SWT.PUSH);
		unit.setText("Run Unit Tests");
		unit.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

			public void widgetSelected(SelectionEvent e)
			{
				runTests("unit", "run_unit.rb");
			}
		});
		final Image uImage = RailsUIPlugin.getImageDescriptor("icons/testrununit.gif").createImage();
		unit.setImage(uImage);
		unit.addDisposeListener(new DisposeListener()
		{
			public void widgetDisposed(DisposeEvent e)
			{
				uImage.dispose();
			}
		});
	}

	private void createFunctionalMenu(Menu parent)
	{
		MenuItem func = new MenuItem(parent, SWT.PUSH);
		func.setText("Run Functional Tests");
		func.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

			public void widgetSelected(SelectionEvent e)
			{
				runTests("functional", "run_functional.rb");
			}
		});
		final Image fImage = RailsUIPlugin.getImageDescriptor("icons/testrunfunctional.gif").createImage();
		func.setImage(fImage);
		func.addDisposeListener(new DisposeListener()
		{
			public void widgetDisposed(DisposeEvent e)
			{
				fImage.dispose();
			}
		});
	}

	private void createIntegrationMenu(Menu parent)
	{
		MenuItem integ = new MenuItem(parent, SWT.PUSH);
		integ.setText("Run Integration Tests");
		integ.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

			public void widgetSelected(SelectionEvent e)
			{
				runTests("integration", "run_integration.rb");
			}
		});
		final Image iImage = RailsUIPlugin.getImageDescriptor("icons/testrunintegration.gif").createImage();
		integ.setImage(iImage);
		integ.addDisposeListener(new DisposeListener()
		{
			public void widgetDisposed(DisposeEvent e)
			{
				iImage.dispose();
			}
		});
	}

	private void runTests(final String type, final String file)
	{
		// Create a new job
		final IProject project = RailsUIPlugin.getSelectedOrOnlyRailsProject();
		if (project == null)
		{
			openErrorDialog(RailsUIMessages.SelectRailsProject_message);
			return;
		}
		Job j = new Job(type + " tests")
		{
			@Override
			public IStatus run(IProgressMonitor monitor)
			{
				monitor.beginTask("Running " + type + " tests", 6);
				TestLauncher t = new TestLauncher(monitor);
				t.goLaunch(project, ILaunchManager.RUN_MODE, file);
				monitor.worked(1);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		j.setUser(true);
		j.schedule();
	}

	private void openErrorDialog(final String message)
	{
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable()
		{
			public void run()
			{
				MessageDialog.openError(fWindow.getShell(), "Error running tests", message);
			}
		});
	}

}
