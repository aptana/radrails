/*******************************************************************************
 * Copyright (c) 2006 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.autotest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.OverlayIcon;
import org.radrails.rails.core.RailsLog;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.RailsUIMessages;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.rails.ui.RailsUIPlugin;

/**
 * Toolbar action to run the selected autotest suite.
 * 
 * @author mkent
 * 
 */
public class RunAutotestSuiteActionDelegate implements
		IWorkbenchWindowActionDelegate, IAutotestRunListener {

	private static final String AUTOTEST_TEMPORARY_FILE = "autotest_listing.rb";

	private IWorkbenchWindow fWindow;

	private IAction fAction;

	private BlinkThread fBlinkThread;

	public void dispose() {
		fWindow = null;
		RailsUIPlugin.getInstance().getAutotestManager()
				.removeRunListener(this);
	}

	public void init(IWorkbenchWindow window) {
		fWindow = window;
		RailsUIPlugin.getInstance().getAutotestManager().addRunListener(this);
	}

	public void run(IAction action) {
		// Create a new job
		Job j = new Job("Autotest") {
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Running autotest suite", 3);
				monitor.worked(1);

				IFile activeEditorFile = AutotestHelper.getActiveEditorFile();
				if (activeEditorFile != null) {
					if(fBlinkThread != null) {
						fBlinkThread.stopBlinking();
					}
					AutotestManager manager = new AutotestManager();
					manager.attemptTestLaunch(activeEditorFile);
				} else{
					openErrorDialog(RailsUIMessages.OpenRubyEditor_message);
				}
				monitor.worked(1);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		j.setUser(true);
		j.schedule();
	}
	
	private void openErrorDialog(final String message) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(fWindow.getShell(),
						"Error running tests", message);
			}
		});
	}

	

	public void selectionChanged(IAction action, ISelection selection) {

	}

	public void suiteError() {
		getAction().setToolTipText("(Error) Manually Run Autotest Suite");
		notifySuite("at_error.gif");
	}

	public void suiteFail() {
		getAction().setToolTipText("(Failed) Manually Run Autotest Suite");
		notifySuite("at_fail.gif");
	}

	public void suitePass() {
		getAction().setToolTipText("(Passed) Manually Run Autotest Suite");
		notifySuite("at_pass.gif");
	}
	
	private void notifySuite(String iconName) {
		ImageDescriptor icon = RailsUIPlugin.getImageDescriptor("icons/" + iconName);
		fBlinkThread = new BlinkThread(icon);
		fBlinkThread.start();
	}
	
	private class BlinkThread extends Thread {
		private boolean keepBlinking;
		private ImageDescriptor fIcon;
		private ImageDescriptor fBlinkIcon;
		
		public BlinkThread(ImageDescriptor icon) {
			keepBlinking = true;
			fIcon = icon;
			ImageDescriptor blink = RailsUIPlugin.getImageDescriptor("icons/blink_ovr.gif");
			fBlinkIcon = new OverlayIcon(icon, blink, new Point(16, 16));
		}
		
		public void run() {
			// blink 5 times and return to normal
			for(int i = 0; (i < 11) && keepBlinking; i++) {
				if(i % 2 == 0) {
					getAction().setImageDescriptor(fIcon);
				}
				else {
					getAction().setImageDescriptor(fBlinkIcon);
				}
				try {
					sleep(500);
				} catch (InterruptedException e) {
					RailsUILog.logError("Error sleeping during icon blink", e);
				}
			}
		}
		
		public void stopBlinking() {
			keepBlinking = false;
			getAction().setImageDescriptor(fIcon);
		}
	}

	private IAction getAction() {
		if (fAction == null) {
			ApplicationWindow aw = (ApplicationWindow) fWindow;

			ToolBarContributionItem tbcItem = (ToolBarContributionItem) aw
					.getCoolBarManager().find(
							"org.radrails.rails.ui.railsActions");
			ActionContributionItem acItem = (ActionContributionItem) tbcItem
					.getToolBarManager().find(
							"org.radrails.rails.ui.test.RunAutotestSuite");
			if (acItem != null) {
				fAction = acItem.getAction();
			}
		}
		return fAction;
	}

	private String writeTemporaryTestListing(List tests) {
		String rubyFile = AUTOTEST_TEMPORARY_FILE;
		String directoryFile = RailsPlugin.getInstance().getStateLocation()
				.toOSString()
				+ File.separator + rubyFile;
		File pluginDirFile = new File(directoryFile);

		try {
			pluginDirFile.createNewFile();
			FileWriter output = new FileWriter(pluginDirFile);
			Iterator i = tests.iterator();
			while (i.hasNext()) {
				output.write("require '" + i.next().toString() + "'");
				output.write('\n');
			}
			output.flush();
			output.close();
		} catch (IOException e) {
			RailsLog.logError("Error writing plugin script to metadata", e);
		}

		String path = "";
		try {
			path = pluginDirFile.getCanonicalPath();
		} catch (IOException e) {
			RailsLog.logError("Error getting file path", e);
		}
		return path;
	}

}
