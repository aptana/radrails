/*******************************************************************************
 * Copyright (c) 2006 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.aptana.ide.editor.erb.contentassist;

import java.io.IOException;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.radrails.rails.ui.RailsUIPlugin;
import org.radrails.rails.ui.RailsUILog;

public class RhtmlTemplateManager {

	private static final String CUSTOM_TEMPLATES_KEY= "org.radrails.rails.internal.ui.rhtmltemplates";
	
	private static RhtmlTemplateManager fInstance;

	private ContributionContextTypeRegistry fRegistry;

	private TemplateStore fStore;

	private RhtmlTemplateManager() {

	}

	public static RhtmlTemplateManager getDefault() {
		if (fInstance == null) {
			fInstance = new RhtmlTemplateManager();
		}
		return fInstance;
	}

	public TemplateStore getTemplateStore() {
		if (fStore == null) {
			fStore = new ContributionTemplateStore(getContextTypeRegistry(),
					RailsUIPlugin.getInstance().getPreferenceStore(),
					CUSTOM_TEMPLATES_KEY);
			try {
				fStore.load();
			} catch (IOException e) {
				RailsUILog.logError("Unable to load template store", e);
			}
		}
		return fStore;
	}

	/**
	 * Returns this plug-in's context type registry.
	 * 
	 * @return the context type registry for this plug-in instance
	 */
	public ContextTypeRegistry getContextTypeRegistry() {
		if (fRegistry == null) {
			fRegistry = new ContributionContextTypeRegistry();
			fRegistry.addContextType(new RhtmlContextType());
		}
		return fRegistry;
	}
}
