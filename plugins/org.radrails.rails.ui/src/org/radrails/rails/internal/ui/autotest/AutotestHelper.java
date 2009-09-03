/*******************************************************************************
 * Copyright (c) 2006 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.autotest;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.PlatformUI;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.rails.ui.RailsUIPlugin;

/**
 * Helper methods to gather test suites for autotest runs. The given source file path will be inspected, as well as the
 * autotest preferences. Based on the results, a list of test suites will be gathered.
 * 
 * @author mkent
 */
public class AutotestHelper
{

	/**
	 * runAutotestOnSave
	 * 
	 * @return - true if running on save
	 */
	public static boolean runAutotestOnSave()
	{
		return RailsUIPlugin.getInstance().getPreferenceStore().getBoolean(IAutotestPreferenceConstants.RUN_ON_SAVE);
	}

	/**
	 * runAutotestOnInterval
	 * 
	 * @return - true if running on interval
	 */
	public static boolean runAutotestOnInterval()
	{
		return RailsUIPlugin.getInstance().getPreferenceStore()
				.getBoolean(IAutotestPreferenceConstants.RUN_ON_INTERVAL);
	}

	/**
	 * getAutotestIntervalLength
	 * 
	 * @return - interval length
	 */
	public static int getAutotestIntervalLength()
	{
		return RailsUIPlugin.getInstance().getPreferenceStore().getInt(IAutotestPreferenceConstants.INTERVAL_LENGTH);
	}

	// For the helper method only
	private static IFile editorFile;

	/**
	 * Gets the active editor file
	 * 
	 * @return - ifile object for active editor if obtained from editor input
	 */
	public static IFile getActiveEditorFile()
	{
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable()
		{
			public void run()
			{

				IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.getActiveEditor();
				if (part != null)
				{
					IEditorInput input = part.getEditorInput();
					if (input instanceof IFileEditorInput)
					{
						IFileEditorInput fei = (IFileEditorInput) part.getEditorInput();
						editorFile = fei.getFile();
					}
					else if (input instanceof IPathEditorInput)
					{
						IPathEditorInput pei = (IPathEditorInput) part.getEditorInput();
						try
						{
							editorFile = ResourcesPlugin.getWorkspace().getRoot().getFile(pei.getPath());
						}
						catch (Exception e)
						{
							RailsUILog.logError("Error obtaining file based on path editor input", e);
						}
					}
				}
			}
		});
		return editorFile;
	}

	/**
	 * Gather all test suites an autotest run on srcFile.
	 * 
	 * @param project
	 *            the project of srcFile
	 * @param srcFile
	 *            project-relative path of the source file
	 * @return a list of Strings, project-relative paths to the test suites
	 */
	public static List<String> getTests(IProject project, IPath srcFile)
	{
		List<String> tests = null;

		tests = testsForModel(project, srcFile);
		if (tests != null)
		{
			return tests;
		}

		tests = testsForController(project, srcFile);
		if (tests != null)
		{
			return tests;
		}

		tests = testsForPlugin(project, srcFile);
		if (tests != null)
		{
			return tests;
		}

		return new ArrayList<String>();
	}

	private static List<String> testsForModel(IProject project, IPath file)
	{
		// FIXME What if user edited the test file? We should still launch autotest!
		List<String> tests = new ArrayList<String>();

		String srcDir = "app/models";
		String destDir = "test/unit";
		String destSuffix = "_test";

		String filepath = file.toString();

		Matcher m = Pattern.compile("^((?:.+?/)?)" + srcDir + "/(.+)" + "\\.rb$").matcher(filepath);
		if (!m.matches())
		{

			m = Pattern.compile("^((?:.+?/)?)" + destDir + "/(.+)" + destSuffix + "\\.rb$").matcher(filepath);
			if (!m.matches())
				return null;
		}

		IPreferenceStore prefStore = RailsUIPlugin.getInstance().getPreferenceStore();

		if (prefStore.getBoolean(IAutotestPreferenceConstants.MODEL_ASSOC_UNIT))
		{
			IPath thePath = project.getProjectRelativePath().append(
					(m.group(1).length() == 0 ? "" : "/") + m.group(1) + "/" + destDir + "/" + m.group(2) + destSuffix
							+ ".rb");

			if (project.getLocation().append(thePath).toFile().exists())
			{
				tests.add(thePath.toOSString());
			}
		}

		if (prefStore.getBoolean(IAutotestPreferenceConstants.MODEL_ALL_UNIT))
		{
			tests.add(RailsPlugin.getInstance().getRubyScriptPath("run_unit.rb"));
		}
		if (prefStore.getBoolean(IAutotestPreferenceConstants.MODEL_ALL_FUNCTIONAL))
		{
			tests.add(RailsPlugin.getInstance().getRubyScriptPath("run_functional.rb"));
		}
		if (prefStore.getBoolean(IAutotestPreferenceConstants.MODEL_ALL_INTEGRATION))
		{
			tests.add(RailsPlugin.getInstance().getRubyScriptPath("run_integration.rb"));
		}

		return tests;
	}

	private static List<String> testsForController(IProject project, IPath file)
	{
		List<String> tests = new ArrayList<String>();

		String srcDir = "app/controllers";
		String destDir = "test/functional";
		String destSuffix = "_test";

		String filepath = file.toString();

		Matcher m = Pattern.compile("^((?:.+?/)?)" + srcDir + "/(.+)" + "\\.rb$").matcher(filepath);
		if (!m.matches())
		{
			m = Pattern.compile("^((?:.+?/)?)" + destDir + "/(.+)" + destSuffix + "\\.rb$").matcher(filepath);
			if (!m.matches())
				return null;
		}

		IPreferenceStore prefStore = RailsUIPlugin.getInstance().getPreferenceStore();

		if (prefStore.getBoolean(IAutotestPreferenceConstants.CONTROLLER_ASSOC_FUNCTIONAL))
		{
			IPath thePath = project.getProjectRelativePath().append(
					(m.group(1).length() == 0 ? "" : "/") + m.group(1) + "/" + destDir + "/" + m.group(2) + destSuffix
							+ ".rb");

			if (project.getLocation().append(thePath).toFile().exists())
			{
				tests.add(thePath.toOSString());
			}
		}

		if (prefStore.getBoolean(IAutotestPreferenceConstants.CONTROLLER_ALL_UNIT))
		{
			tests.add(RailsPlugin.getInstance().getRubyScriptPath("run_unit.rb"));
		}
		if (prefStore.getBoolean(IAutotestPreferenceConstants.CONTROLLER_ALL_FUNCTIONAL))
		{
			tests.add(RailsPlugin.getInstance().getRubyScriptPath("run_functional.rb"));
		}
		if (prefStore.getBoolean(IAutotestPreferenceConstants.CONTROLLER_ALL_INTEGRATION))
		{
			tests.add(RailsPlugin.getInstance().getRubyScriptPath("run_integration.rb"));
		}

		return tests;
	}

	private static List<String> testsForPlugin(IProject project, IPath file)
	{
		List<String> tests = new ArrayList<String>();

		String srcDir = "vendor/plugins";
		String destDir = "vendor/plugins";
		String destSuffix = "_test";

		String filepath = file.toString();

		Matcher m = Pattern.compile("^((?:.+?/)?)" + srcDir + "/(.+?/)lib/(.+)" + "\\.rb$").matcher(filepath);
		if (!m.matches())
		{
			m = Pattern.compile("^((?:.+?/)?)" + destDir + "/(.+?/)lib/(.+)" + destSuffix + "\\.rb$").matcher(filepath);
			if (!m.matches())
				return null;
		}

		IPreferenceStore prefStore = RailsUIPlugin.getInstance().getPreferenceStore();

		if (prefStore.getBoolean(IAutotestPreferenceConstants.PLUGIN_ASSOC))
		{
			IPath thePath = project.getProjectRelativePath().append(
					(m.group(1).length() == 0 ? "" : "/") + m.group(1) + "/" + destDir + "/" + m.group(2) + "test"
							+ "/" + m.group(3) + destSuffix + ".rb");

			if (project.getLocation().append(thePath).toFile().exists())
			{
				tests.add(thePath.toOSString());
			}
		}

		if (prefStore.getBoolean(IAutotestPreferenceConstants.PLUGIN_ALL))
		{
			IPath thePath = project.getLocation().append(
					(m.group(1).length() == 0 ? "" : "/") + m.group(1) + "/" + destDir + "/" + m.group(2) + "test"
							+ "/");

			System.out.println("plugin test path=" + thePath.toString());

			File[] testFiles = thePath.toFile().listFiles(new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					return name.endsWith("_test.rb");
				}
			});

			if (testFiles != null)
			{
				for (int i = 0; i < testFiles.length; i++)
				{
					tests.add(testFiles[i].toString());
				}
			}
		}

		if (prefStore.getBoolean(IAutotestPreferenceConstants.PLUGIN_ALL_UNIT))
		{
			tests.add(RailsPlugin.getInstance().getRubyScriptPath("run_unit.rb"));
		}
		if (prefStore.getBoolean(IAutotestPreferenceConstants.PLUGIN_ALL_FUNCTIONAL))
		{
			tests.add(RailsPlugin.getInstance().getRubyScriptPath("run_functional.rb"));
		}
		if (prefStore.getBoolean(IAutotestPreferenceConstants.PLUGIN_ALL_INTEGRATION))
		{
			tests.add(RailsPlugin.getInstance().getRubyScriptPath("run_integration.rb"));
		}

		return tests;
	}
}
