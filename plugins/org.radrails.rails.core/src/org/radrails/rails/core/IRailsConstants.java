/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.core;

import com.aptana.rdt.rake.PreferenceConstants;

/**
 * Provides constants for the Rails perspective.
 * 
 * @author mbaumbach
 * @version 0.3.1
 */
public interface IRailsConstants
{

	public static final String RAILS_PROJECT_NATURE = "org.radrails.rails.core.railsnature";

	public static final String[] OUTPUT_SYNC = { "-e STDOUT.sync=true", "-e STDERR.sync=true", "-e load(ARGV.shift)" };

	public static final String PREF_RAILS_PATH = "rails.core.path.rails";

	/**
	 * @deprecated Please use {@link PreferenceConstants.PREF_RAKE_PATH}
	 */
	public static final String PREF_RAKE_PATH = "rails.core.path.rake";

	public static final String PREF_MONGREL_PATH = "rails.core.path.mongrel";

	/**
	 * @since 1.3.0
	 */
	public static final String AUTO_OPEN_RAILS_SHELL = "autoOpenRailsShell"; //$NON-NLS-1$
} // IRailsConstants