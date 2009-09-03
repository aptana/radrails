/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.browser;

import org.eclipse.swt.browser.Browser;
import org.eclipse.ui.internal.browser.WebBrowserEditor;

/**
 * Internal web browser. This class is an <code>EditorPart</code> only so it
 * will show up in the editor view area. No actual editing can be done in the
 * browser.
 * 
 * @author Kyle
 * 
 */
public class BrowserEditor extends WebBrowserEditor {
	
	public static final String ID = "org.radrails.rails.ui.browser.BrowserEditor";

	public Browser getBrowser() {
		return webBrowser.getBrowser();
	}

}
