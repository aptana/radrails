package org.eclipse.eclipsemonkey.lang.ruby.doms.editors;
/**
 * Copyright (c) 2007 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

import org.eclipse.eclipsemonkey.lang.ruby.doms.IRubyDOMFactory;
import org.jruby.Ruby;

/**
 * @author Chris Williams
 */
public class EditorsDOMFactory implements IRubyDOMFactory {

	public Object getDOMroot(Ruby runtime) {
		return new Editors(runtime);
	}

}
