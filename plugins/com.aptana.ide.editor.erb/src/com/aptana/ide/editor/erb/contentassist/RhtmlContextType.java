/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.aptana.ide.editor.erb.contentassist;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * Simple rails file context type.
 *
 * @author	mbaumbach
 *
 * @version	0.3.1
 */
public class RhtmlContextType extends TemplateContextType {

	/** This context's id */
	public static final String ID = "org.radrails.rails.ui.templateContextType.rhtml"; //$NON-NLS-1$

	/**
	 * Creates a new context type. 
	 */
	public RhtmlContextType() {
		super(ID);
		addGlobalResolvers();
	}

	/**
	 * Adds the global resolvers.
	 */
	private void addGlobalResolvers() {
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());
	}
	
} // RailsContextType
