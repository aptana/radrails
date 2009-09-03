package com.aptana.radrails.server.bridge;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.radrails.server.ui.dialogs.EditServerDialog;

import com.aptana.ide.server.ui.views.actions.ICanAdd;

public class AddServer implements ICanAdd {

	public void doAdd() {
		EditServerDialog dialog = new EditServerDialog(Display.getDefault().getActiveShell());
		if (dialog.open() == Window.OK) {
			ServerManager.getInstance().addServer(new Server(dialog.getProject(), dialog.getName(), dialog.getType(), dialog.getHost(), dialog.getPort(), dialog.getEnvironment()));	
		}
	}

}
