/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.generators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Generator implements Comparable {
	
	private String name;
	private String location;
	
	public Generator(String name, String location) {
		this.location = location;
		this.name = name;
	}
	
	public Generator(String location) {
		this(parseName(location), location);
	}

	public String getName() {
		return name;
	}
	
	public String getLocation() {
		return location;
	}	

	private static String parseName( String location ) {
		String patternStr = ".*/(\\w+)_generator.rb\\Z";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(location);
		
		if(matcher.matches()) {
			return matcher.group(1);
		} else {
			return "";
		}
	}

	public int compareTo(Object arg0) {
		Generator g = (Generator) arg0;		
		return this.name.compareTo( g.getName() );
	}

}
