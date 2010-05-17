package org.radrails.rails.internal.ui.console;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

public class RailsShellPageParticipant implements IConsolePageParticipant {

	private RailsShell console;
	private Action stopAction;

	public void activated() {		
	}

	public void deactivated() {		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IPageBookViewPage page, IConsole console) {
		this.console = (RailsShell) console;
		stopAction = new RailsShellProjectSelectionAction(this.console);
		
		// Add the actions to the toolbar
		IActionBars bars = page.getSite().getActionBars();
		bars.getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP, stopAction);
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

}
