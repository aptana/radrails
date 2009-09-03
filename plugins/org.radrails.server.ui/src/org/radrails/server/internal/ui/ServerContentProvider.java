package org.radrails.server.internal.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.radrails.server.core.ServerManager;

public class ServerContentProvider implements IStructuredContentProvider {

	public Object[] getElements(Object inputElement) {
		Object[] res = null;
		if (inputElement instanceof ServerManager) {
			res = ((ServerManager) inputElement).getServers().toArray();
		}
		
		return res;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}
}
