/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.core;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.radrails.rails.core.IRailsConstants;
import org.radrails.rails.core.RailsLog;
import org.rubypeople.rdt.launching.IVMInstall;

/**
 * Manages the installations of Rails on the user's system. Provides access to the currently selected install and the
 * ability to run commands relative to the Rails directory.
 * 
 * @author mkent
 */
public class RailsRuntime
{

	/**
	 * Runs a Ruby script on the command line. If a Ruby interpreter is selected in the RDT preferences, it will be
	 * used.
	 * 
	 * @param preCmds
	 *            Commands to be placed at the very beginning of the launch string
	 * @param cmds
	 *            the script and its arguments
	 * @param workingDir
	 *            the working directory
	 * @param cmdPrompt
	 *            if true, the Windows command prompt will be launched
	 * @return a system process handle
	 * @throws CoreException
	 *             if an error occurs launching the process
	 * @deprecated This code doesn't respect the VM type and OS properly. We should be using the launch config
	 *             infrastructure!
	 */
	public static Process rubyExec(String[] preCmds, String[] cmds, File workingDir, boolean cmdPrompt)
			throws CoreException
	{
		int allCmdsCount = 0;
		int cmdsCount = 0;
		int preCmdsCount = 0;
		int syncCmdsCount = IRailsConstants.OUTPUT_SYNC.length;
		if (preCmds != null)
			preCmdsCount = preCmds.length;
		if (cmds != null)
			cmdsCount = cmds.length;
		String[] allCmds = new String[preCmdsCount + syncCmdsCount + cmdsCount + 1];

		IVMInstall interp = org.rubypeople.rdt.launching.RubyRuntime.getDefaultVMInstall();
		// XXX Change this to use the launch configuration stuff to run!
		// If a Ruby interpreter is selected, use it
		if (interp != null)
		{
			String rubyPath = interp.getInstallLocation().getAbsolutePath();
			String command = null;
			if (interp.getVMInstallType().getId().equals("org.rubypeople.rdt.launching.JRubyVMType"))
			{
				command = "jruby";
				if (Platform.getOS().equals(Platform.OS_WIN32))
				{
					command += ".bat";
				}
			}
			else
			{
				command = "ruby";
			}
			File rubyParentFile = new File(rubyPath).getParentFile();
			if (rubyPath.substring(rubyParentFile.getAbsolutePath().length() + 1).equals("bin"))
				rubyPath = rubyPath + File.separator + command;
			else
				rubyPath = rubyPath + File.separator + "bin" + File.separator + command;
			allCmds[allCmdsCount] = rubyPath;
			allCmdsCount += 1;
		}
		else
		{ // Otherwise, run from the path
			// Make sure to launch the command prompt on Windows
			if (cmdPrompt && Platform.getOS().equals(Platform.OS_WIN32))
			{
				// Need a little more space. We could have tried to compute this
				// upfront but it didn't seem like it was worth the trouble
				// given how often we seem to take this path.
				allCmds = new String[preCmdsCount + syncCmdsCount + cmdsCount + 3];
				allCmds[allCmdsCount] = "cmd.exe";
				allCmdsCount += 1;
				allCmds[allCmdsCount] = "/C";
				allCmdsCount += 1;
			}
			allCmds[allCmdsCount] = "ruby";
			allCmdsCount += 1;
		}

		if (preCmdsCount > 0)
		{
			System.arraycopy(preCmds, 0, allCmds, allCmdsCount, preCmdsCount);
			allCmdsCount += preCmdsCount;
		}
		// Sync the output
		System.arraycopy(IRailsConstants.OUTPUT_SYNC, 0, allCmds, allCmdsCount, syncCmdsCount);
		allCmdsCount += syncCmdsCount;
		System.arraycopy(cmds, 0, allCmds, allCmdsCount, cmdsCount);
		// allCmdsCount += cmdsCount;

		RailsLog.logInfo("RailsRuntime.rubyExec: executing: " + Arrays.toString(allCmds), null);
		try
		{
			return DebugPlugin.exec(allCmds, workingDir);
		}
		catch (RuntimeException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Runs a Ruby script on the command line. If a Ruby interpreter is selected in the RDT preferences, it will be
	 * used.
	 * 
	 * @param cmds
	 *            the script and its arguments
	 * @param workingDir
	 *            the working directory
	 * @param cmdPrompt
	 *            if true, the Windows command prompt will be launched
	 * @return a system process handle
	 * @throws CoreException
	 *             if an error occurs launching the process
	 */
	public static Process rubyExec(String[] cmds, File workingDir, boolean cmdPrompt) throws CoreException
	{
		return rubyExec(null, cmds, workingDir, cmdPrompt);
	}

}
