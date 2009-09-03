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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.rails.ui.test.TestLauncher;
import org.rubypeople.rdt.internal.testunit.ui.TestUnitView;
import org.rubypeople.rdt.internal.testunit.ui.TestunitPlugin;
import org.rubypeople.rdt.testunit.ITestRunListener;

/**
 * Listens for resource change events. When Ruby files are saved, launches
 * associated Test::Unit suites based on preferences. For example, launches
 * corresponding unit test for models, functional tests for controllers, and so
 * forth. Exactly which tests are run for which classes is configurable via
 * preferences.
 * 
 * @author mkent
 * 
 */
public class AutotestManager implements IResourceChangeListener,
		IPropertyChangeListener, ITestRunListener {

	private Collection<IAutotestRunListener> fRunListeners;
	
	private AutotestThread fAutotestThread;
	
	private TestUnitView view;

	private IResourceDeltaVisitor fDeltaVisitor = new IResourceDeltaVisitor() {
		public boolean visit(IResourceDelta delta) throws CoreException {
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// only fire on content change events on the active editor file
				if((delta.getFlags() & IResourceDelta.CONTENT) == IResourceDelta.CONTENT) {
					if(delta.getResource() != null && delta.getResource() instanceof IFile) {
						IFile changedFile = (IFile) delta.getResource();
						IFile activeEditorFile = AutotestHelper.getActiveEditorFile();
						if(changedFile.getLocation().equals(activeEditorFile.getLocation())
								&& changedFile.getFileExtension().equals("rb")) {
							attemptTestLaunch(delta.getResource());
						}
					}
				}
				break;
			}
			return true;
		}
	};

	private boolean fTestsPass;
	
	public AutotestManager() {
		fRunListeners = new ArrayList<IAutotestRunListener>();
		if(AutotestHelper.runAutotestOnInterval()) {
			fAutotestThread = new AutotestThread();
			fAutotestThread.start();
		}
	}

	public void resourceChanged(IResourceChangeEvent event) {
		if (event == null) return;
		boolean runOnSave = AutotestHelper.runAutotestOnSave();
		try {
			if (runOnSave) {
				IResourceDelta delta = event.getDelta();
				if (delta != null) delta.accept(fDeltaVisitor);
			}
		} catch (CoreException e) {
			RailsUILog.logError("Error launching tests", e);
		}
	}

	public void attemptTestLaunch(final IResource file) {
		Job j = new Job("Autotest suite") {
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Running autotest suite", 3);
				monitor.worked(1);

				final List<String> tests = AutotestHelper.getTests(file.getProject(),
						file.getProjectRelativePath());
				monitor.worked(1);

				if (!tests.isEmpty()) {					
					launchTests(file.getProject(), tests);
				}
				monitor.worked(1);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		j.schedule();
	}
	
	private void launchTests(IProject project, List<String> testFiles) {
		try {
			TestLauncher launcher = new TestLauncher();
			IPath path = RailsPlugin.getInstance().getStateLocation();
			path = path.append("run_auto2.rb");
			
			File temp = path.toFile();
			FileWriter writer = new FileWriter(temp);
			writer.write("require 'test/unit'\n");
			for (String string : testFiles) {
				writer.write("require '" + string + "'\n");
			}
			writer.close();
			
			Display.getDefault().syncExec(new Runnable() {
			
				public void run() {
					view = TestunitPlugin.getDefault().findTestUnitViewInActivePage();			
				}
			
			});
			boolean oldShowOnErrorOnly = false;
			if (view != null) {
				oldShowOnErrorOnly = view.getShowOnErrorOnly();
				view.setShowOnErrorOnly(true);
			}
			TestunitPlugin.getDefault().addTestRunListener(this);
			ILaunch launch = launcher.goLaunch(project, ILaunchManager.RUN_MODE, "run_auto2.rb");
			if (launch == null)
				return;
			IProcess iproc = launch.getProcesses()[0];
			while (!iproc.isTerminated()) {
				Thread.yield();
				// XXX add a timeout so we don't loop infinitely
			}
			if (view != null) {
				view.setShowOnErrorOnly(oldShowOnErrorOnly);
			}
			TestunitPlugin.getDefault().removeTestRunListener(this);
		} catch (IOException e) {
			RailsUILog.log(e);
		}
	}

	public void addRunListener(IAutotestRunListener listener) {
		fRunListeners.add(listener);
	}

	public void removeRunListener(IAutotestRunListener listener) {
		fRunListeners.remove(listener);
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(
				IAutotestPreferenceConstants.RUN_ON_INTERVAL)) {
			boolean enabled = ((Boolean) event.getNewValue()).booleanValue();
			if (enabled) {
				if(fAutotestThread != null) {
					fAutotestThread.stopRunning();
				}
				fAutotestThread = new AutotestThread();
				fAutotestThread.start();
			}
			else {
				fAutotestThread.stopRunning();
				fAutotestThread = null;
			}
		}
	}

	private class AutotestThread extends Thread {
		private boolean keepRunning;

		public AutotestThread() {
			keepRunning = true;
		}

		public void run() {
			while (keepRunning) {
				int intervalMinutes = AutotestHelper
						.getAutotestIntervalLength();
				long intervalMs = intervalMinutes * 60 * 1000;
				try {
					sleep(intervalMs);
				} catch (InterruptedException e) {
					RailsUILog.logError(
							"Error sleeping during autotest interval", e);
				}

				// Run autotest suite
				IFile activeEditorFile = AutotestHelper.getActiveEditorFile();
				if (activeEditorFile != null) {
					attemptTestLaunch(activeEditorFile);
				}
			}
		}

		public void stopRunning() {
			keepRunning = false;
		}
	}

	public void testEnded(String testId, String testName) {}

	public void testFailed(int status, String testId, String testName, String trace) {
		
		for (IAutotestRunListener listener : fRunListeners) {
			if (status == STATUS_ERROR) {
				listener.suiteError();
			} else {
				listener.suiteFail();
			}		
		}
		fTestsPass = false;
	}

	public void testReran(String testId, String testClass, String testName, int status, String trace) {}

	public void testRunEnded(long elapsedTime) {
		if (!fTestsPass) return;
		for (IAutotestRunListener listener : fRunListeners) {
			listener.suitePass();
		}
	}

	public void testRunStarted(int testCount) {
		fTestsPass = true;
		
	}

	public void testRunStopped(long elapsedTime) {}

	public void testRunTerminated() {}

	public void testStarted(String testId, String testName) {}

}
