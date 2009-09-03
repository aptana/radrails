package org.radrails.rails.internal.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.IHyperlink;
import org.radrails.rails.internal.ui.generators.GeneratorsConsoleLine;
import org.radrails.rails.ui.RailsUILog;
import org.rubypeople.rdt.internal.debug.ui.console.RubyConsoleTracker;

public class RailsConsoleTracker extends RubyConsoleTracker
{

	public void lineAppended(IRegion line)
	{
		try
		{
			int prefix = 0;
			String text = getText(line);
			while (GeneratorsConsoleLine.isMatching(text))
			{
				IProject project = getProject();
				if (project == null)
					return;
				RailsConsoleLine consoleLine = new GeneratorsConsoleLine(text, project);
				IHyperlink link = new RailsConsoleHyperlink(fConsole, consoleLine);
				fConsole.addLink(link, line.getOffset() + prefix + consoleLine.getOffset(), consoleLine.getLength());

				prefix = consoleLine.getOffset() + consoleLine.getLength();
				int substring = consoleLine.getOffset() + consoleLine.getLength();
				if (substring >= text.length())
				{
					text = "";
				}
				else if (substring > 0)
				{
					text = text.substring(substring);
					if (text.startsWith(":in `require':"))
					{
						text = text.substring(14);
						prefix += 14;
					}
				}
			}
		}
		catch (BadLocationException e)
		{
			RailsUILog.logError("Error hyperlinking console line", e);
		}
	}

}
