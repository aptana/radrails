/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.server.internal.ui.console;

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.views.console.ConsoleTerminateAction;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;

/**
 * Page participant for the ServerConsole. Adds a remove all terminated action and a stop action.
 * 
 * @author Kyle
 */
public class ServerConsolePageParticipant implements IConsolePageParticipant
{

	private ServerStopAction consoleStopAction;
	private LaunchBrowserAction launchBrowserAction;
	private IToolBarManager toolbar;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#init(org.eclipse.ui.part.IPageBookViewPage,
	 * org.eclipse.ui.console.IConsole)
	 */
	public void init(IPageBookViewPage page, IConsole console)
	{
		if (!(console instanceof org.eclipse.debug.ui.console.IConsole))
			return;
		org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
		final IProcess process = processConsole.getProcess();
		if (!looksLikeServerStarting(process))
		{
			return;
		}
		this.consoleStopAction = new ServerStopAction(process);
		this.launchBrowserAction = new LaunchBrowserAction(process);

		// Remove the normal Terminate button
		IActionBars bars = page.getSite().getActionBars();
		toolbar = bars.getToolBarManager();

		removeTerminateAction(toolbar);
		// Add the Stop server button that replaces the terminate button
		bars.getToolBarManager().prependToGroup(IConsoleConstants.LAUNCH_GROUP, consoleStopAction);
		bars.getToolBarManager().prependToGroup(IConsoleConstants.LAUNCH_GROUP, launchBrowserAction);

		// Listen for Ctrl+C
		TextConsolePage consolePage = (TextConsolePage) page;
		consolePage.getControl().addKeyListener(new KeyListener()
		{

			public void keyReleased(KeyEvent e)
			{
				// ignore
			}

			public void keyPressed(KeyEvent e)
			{
				int keyCode = e.keyCode;
				if (keyCode == 'c' && e.stateMask == SWT.CONTROL) // 'Ctrl+c'
				{
					consoleStopAction.run();
				}

			}
		});
	}

	private void removeTerminateAction(IToolBarManager toolbar)
	{
		if (toolbar == null)
			return;
		IContributionItem[] items = toolbar.getItems();
		for (int i = 0; i < items.length; i++)
		{
			if (!(items[i] instanceof ActionContributionItem))
				continue;
			ActionContributionItem item = (ActionContributionItem) items[i];
			IAction action = item.getAction();
			if (!(action instanceof ConsoleTerminateAction))
				continue;
			toolbar.remove(item);
			toolbar.update(false);
			break;
		}

	}

	private boolean looksLikeServerStarting(IProcess process)
	{
		if (process == null || process.getLaunch() == null)
			return false;
		String fileName = process.getLaunch().getAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME);
		if (fileName == null || fileName.trim().length() == 0)
			return false;
		return fileName.endsWith("mongrel_rails") || fileName.endsWith("server");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#dispose()
	 */
	public void dispose()
	{
		this.consoleStopAction = null;
		this.launchBrowserAction = null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#activated()
	 */
	public void activated()
	{
		// Do nothing
		removeTerminateAction(toolbar);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#deactivated()
	 */
	public void deactivated()
	{
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter)
	{
		return null;
	}

}
