/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.rails.core.railsplugins;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Simple Map wrapper for Rails plugin properties.
 * 
 * @author mkent
 * 
 */
public class RailsPluginDescriptor {
	
	public static final String REPOSITORY = "repository";
	public static final String RATING = "rating";
	public static final String NAME = "name";
	public static final String HOME = "home";
	public static final String LICENSE = "license";

	private Map<String, String> fContents;

	public RailsPluginDescriptor() {
		fContents = new HashMap<String, String>();
	}

	public String getProperty(String key) {
		return (String) fContents.get(key);
	}

	public void setProperty(String key, String value) {
		fContents.put(key, value);
	}
	
	public String getName() {
		String guessed = guessName(getRepository());
		if (guessed != null) return guessed;
		return getRawName();
	}

	String getRepository() {
		String raw = getProperty(RailsPluginDescriptor.REPOSITORY);
		if (raw != null && raw.startsWith("svn checkout ")) {
			raw = raw.substring("svn checkout ".length());
		}
		return raw;
	}
	
	private static String guessName(String url) {
		if (url == null || url.trim().length() == 0) return null;
		try {
			URI u = new URI(url);
			IPath path = new Path(u.getPath());
			String name = path.lastSegment();
			if (name == null) return null;
			if (name.equals("trunk") || name.trim().length() == 0) {
				name = path.removeLastSegments(1).lastSegment();
			}
			return normalize(name);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static String normalize(String string) {
		if (string == null) return string;
		return string.replace(' ', '_');
	}

	/**
	 * Cast teh ratign to a float. Return -1 if there is no rating value (null or empty).
	 * @return
	 */
	public float getRating() {
		String raw = getProperty(RATING);
		if (raw == null || raw.trim().length() == 0) return -1;
		try {
			return Float.parseFloat(raw);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private String getRawHome() {
		return getProperty(HOME);		
	}
	
	public String getHome() {
		String home = getRawHome();
		if (home == null) return "";
		if (home.trim().length() == 0) return home;
		if (home.startsWith("svn://")) {
			home = "http://" + home.substring("svn://".length());
		} else if (!home.startsWith("http")) {
			home = "http://" + home;
		}
		return home;
	}
	
	@Override
	public String toString() {
		return fContents.toString();
	}

	public String getRawName() {
		return getProperty(NAME);
	}

	public String getLicense() {
		return getProperty(LICENSE);
	}

	public String getRawRating() {
		return getProperty(RailsPluginDescriptor.RATING);
	}
}
