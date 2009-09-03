/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.rails.internal.ui.tail;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Utility class with methods for programmatically initializing toolbar actions.
 * 
 * @author mkent
 * 
 */
public class ActionUtil {

	public static final String PLUGIN_ID = "org.radrails.rails.ui";

	public static final String ICON_PATH = "icons/";

	/**
	 * Initializes the given action with an image and a tooltip.
	 * 
	 * @param a
	 * @param image
	 * @param tooltip
	 */
	public static void initAction(IAction a, String image, String tooltip) {
		a.setToolTipText(tooltip);

		ImageDescriptor id = ImageDescriptor.createFromURL(getImageUrl(image));
		if (id != null)
			a.setImageDescriptor(id);
	}

	private static URL getImageUrl(String relative) {
		return FileLocator.find(Platform.getBundle(PLUGIN_ID), new Path(ICON_PATH + relative), null);
	}
}
