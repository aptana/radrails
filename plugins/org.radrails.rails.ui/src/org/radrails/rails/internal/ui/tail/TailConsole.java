/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.tail;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.MessageConsole;

/**
 * Console used for tailing output.
 *
 * @author	mbaumbach
 *
 * @version	0.3.1
 */
public class TailConsole extends MessageConsole {

	private Tail tail;
	
	/**
	 * Creates a new TailConsole for the specified file.
	 * 
	 * @param file	The file to tail.
	 * @param name	The name for the console.
	 * @param imageDescriptor	The descriptor of the image
	 */
	public TailConsole(IFile file, String name, ImageDescriptor imageDescriptor) {
		super(name, imageDescriptor);
		
		TailConsoleManager.getInstance().addConsole(this);

		// Start tailing the file
		tail = new Tail(file.getLocation().toFile());
		tail.start(this);
	}
	
	public void stopTail() {
		tail.stop();
		setName("<terminated> " + getName());
	}
	
	public boolean isStopped() {
		return tail.isStopped();
	}
	
	/**
	 * @see org.eclipse.ui.console.AbstractConsole#dispose()
	 */
	protected void dispose() {
		try {
			tail.stop();
		} finally {
			super.dispose();
		}
		
	}

} // TailConsole