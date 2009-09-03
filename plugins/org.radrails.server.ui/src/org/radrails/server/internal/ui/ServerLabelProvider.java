/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.server.internal.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.radrails.server.core.IServerConstants;
import org.radrails.server.core.Server;

/**
 * Label provider for the server view.
 * 
 * @author mkent
 * 
 */
public class ServerLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	private static final int SERVER_TYPE_COLUMN = 0;
	private static final int SERVER_NAME_COLUMN = 1;
	private static final int PROJECT_NAME_COLUMN = 2;
	private static final int STATUS_COLUMN = 3;
	private static final int PORT_COLUMN = 4;
	private static final int ENVIRONMENT_COLUMN = 5;

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
	 *      int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		Server server = (Server) element;
		Image image = null;
		ImageDescriptor imageDesc = null;

		if (columnIndex == SERVER_TYPE_COLUMN) {
			if (server.getType().equals(IServerConstants.TYPE_WEBRICK)) {
				imageDesc = getDescriptor("icons/webrick.gif");
			}
			else if (server.getType().equals(IServerConstants.TYPE_LIGHTTPD)) {
				imageDesc = getDescriptor("icons/lighttpd.gif");
			}
			else if (server.getType().equals(IServerConstants.TYPE_MONGREL)) {
				imageDesc = getDescriptor("icons/mongrel.gif");
			}
		}
		if (imageDesc != null) {
			image = imageDesc.createImage();
		}
		return image;
	}

	private ImageDescriptor getDescriptor(String name) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(ServerUIPlugin.getInstance().getBundle().getSymbolicName(), name);
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
	 *      int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		Server server = (Server) element;

		switch (columnIndex) {
		case SERVER_NAME_COLUMN:
			return server.getName();
		case PROJECT_NAME_COLUMN:
			return server.getProject().getName();
		case STATUS_COLUMN:
			return server.getStatus();
		case PORT_COLUMN:
			return server.getPort();
		case ENVIRONMENT_COLUMN:
			return server.getEnvironment();
		default:
			return "";
		}
	}
}
