package org.eclipse.eclipsemonkey.lang.ruby;
/**
 * Copyright (c) 2007 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author Chris Williams
 */
public class RubyScriptConsole extends MessageConsole {

	private static final String CONSOLE_NAME = "Ruby Scripting Console"; //$NON-NLS-1$
	private static final String CONSOLE_STARTED_MESSAGE = "Eclipse Monkey Ruby Console Started"; //$NON-NLS-1$
	
	private static RubyScriptConsole _console;
	private static MessageConsoleStream _consoleStream;

	public RubyScriptConsole(String name, ImageDescriptor imageDescriptor) {
		super(name, imageDescriptor);
	}
	
	/**
	 * Returns a reference to the current console, initializing it if it's not created
	 * 
	 * @return A console stream
	 */
	public static MessageConsoleStream getConsoleStream()
	{
		if (_console == null)
		{
			_console = new RubyScriptConsole(CONSOLE_NAME, null);
			_consoleStream = _console.newMessageStream();

			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable()
			{
				public void run()
				{
					_consoleStream.setColor(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLUE));
				}
			});

			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { _console });
			_consoleStream.println(CONSOLE_STARTED_MESSAGE);
		}

		return _consoleStream;
	}

}
