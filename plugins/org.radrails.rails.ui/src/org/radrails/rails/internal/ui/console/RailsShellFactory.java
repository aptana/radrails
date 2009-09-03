package org.radrails.rails.internal.ui.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;

public class RailsShellFactory implements IConsoleFactory {

	private RailsShell console;
	
	public void openConsole() {
		if (console == null) {
			console = tryToFindInstance();
			if (console == null) { // if it's still null, create a new one
				console = RailsShell.open();
			}
		}
		// If already open, bring it to top
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
	}

	private RailsShell tryToFindInstance() {
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		for (int i = 0; i < consoles.length; i++) {
			if (consoles[i] instanceof RailsShell) {
				return (RailsShell) consoles[i];
			}
		}
		return null;
	}

}
