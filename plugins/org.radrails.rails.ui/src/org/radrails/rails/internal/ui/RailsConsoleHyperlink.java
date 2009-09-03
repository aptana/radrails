package org.radrails.rails.internal.ui;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.ide.IDE;
import org.radrails.rails.ui.RailsUILog;

public class RailsConsoleHyperlink implements IHyperlink
{

	private IConsole fConsole;
	private RailsConsoleLine fLine;

	public RailsConsoleHyperlink(IConsole console, RailsConsoleLine line)
	{
		fConsole = console;
		fLine = line;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IHyperlink#linkEntered()
	 */
	public void linkEntered()
	{
	}

	/**
	 * @see org.eclipse.debug.ui.console.IHyperlink#linkExited()
	 */
	public void linkExited()
	{
	}

	/**
	 * @see org.eclipse.debug.ui.console.IHyperlink#linkActivated()
	 */
	public void linkActivated()
	{
		String filename = this.getFilename();
		try
		{
			IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), ResourcesPlugin
					.getWorkspace().getRoot().getFile(new Path(filename)));
		}
		catch (CoreException e)
		{
			RailsUILog.logError("Could not open editor or set line in editor", e);
		}
	}

	/**
	 * Returns the line number associated with the stack trace
	 * 
	 * @exception CoreException
	 *                if unable to parse the number
	 */
	public int getLineNumber()
	{
		return fLine.getLineNumber();
	}

	public String getFilename()
	{
		return fLine.getFilename();
	}

	/**
	 * Returns the console this link is contained in.
	 * 
	 * @return console
	 */
	protected IConsole getConsole()
	{
		return fConsole;
	}

	/**
	 * Returns this link's text
	 * 
	 * @exception CoreException
	 *                if unable to retrieve the text
	 */
	protected String getLinkText() throws BadLocationException
	{
		IRegion region = getConsole().getRegion(this);
		return getConsole().getDocument().get(region.getOffset(), region.getLength());
	}

}
