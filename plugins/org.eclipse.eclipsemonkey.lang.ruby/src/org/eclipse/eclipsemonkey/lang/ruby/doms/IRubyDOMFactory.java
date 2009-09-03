/**
 * Copyright (c) 2007 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */package org.eclipse.eclipsemonkey.lang.ruby.doms;

import org.jruby.Ruby;

/**
 * IRubyDOMFactory
 * @author Christopher Williams
 *
 */
public interface IRubyDOMFactory
{
	/**
	 * getDOMroot
	 * @param runtime The JRuby runtime
	 * @return The root object
	 */
	public Object getDOMroot(Ruby runtime);
}
