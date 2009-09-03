/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.server.internal.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.radrails.rails.ui.IRailsUIConstants;
import org.rubypeople.rdt.internal.ui.RubyPlugin;

/**
 * This view displays the existing web servers in the workspace. Users can
 * start, stop and edit the servers from this view.
 * 
 * @author mkent
 * 
 */
public class ServersView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout());
		GridData data = new GridData();
		data.widthHint = 150;
		container.setLayoutData(data);
		
		Label label = new Label(container, SWT.NULL);
		label.setText("This view has been replaced by the new generic Servers view.");
		
		Button button = new Button(container, SWT.NULL);
		button.setText("Open New Servers view");
		final IViewPart view = this;
		button.addSelectionListener(new SelectionAdapter() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					IWorkbenchWindow dw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (dw == null) return;
					IWorkbenchPage page = dw.getActivePage();
					if (page == null) return;
					page.showView(IRailsUIConstants.ID_SERVERS_VIEW);
					page.hideView(view);
				} catch (PartInitException e1) {
					RubyPlugin.log(e1);
				}
			}
		
		});
	}

	@Override
	public void setFocus() {
	}
}
