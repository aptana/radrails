/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.radrails.org/legal/cpl-v10.html
 *******************************************************************************/

package net.lucky_dip.hamleditor.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.lucky_dip.hamleditor.Activator;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Color provider for rails ui plugin.
 * 
 * @author mkent
 * 
 */
public class HamlUIColorProvider {

	private static HamlUIColorProvider instance;

	private Map colorMap;

	private HamlUIColorProvider() {
		colorMap = new HashMap();
	}

	/**
	 * @return the singleton instance of the color provider
	 */
	public static HamlUIColorProvider getInstance() {
		if (instance == null) {
			instance = new HamlUIColorProvider();
		}
		return instance;
	}

	/**
	 * Dispose the color resources held by the provider.
	 */
	public void dispose() {
		Iterator i = colorMap.values().iterator();
		while (i.hasNext())
			((Color) i.next()).dispose();
	}

	/**
	 * Creates a <code>Color</code> from the given <code>RGB</code> value.
	 * 
	 * @param rgb
	 *            the <code>RGB</code> value of the color
	 * @return a <code>Color</code> object
	 */
	public Color getColor(RGB rgb) {
		Color color = (Color) colorMap.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			colorMap.put(rgb, color);
		}
		return color;
	}

	/**
	 * Creates a <code>Color</code> from the given string RGB value.
	 * 
	 * @param rgb
	 *            the string RGB value
	 * @return a <code>Color</code> object
	 */
	public Color getColor(String rgb) {
		return getColor(StringConverter.asRGB(rgb));
	}

	/**
	 * Creates a <code>Color</code> from a preference located in the
	 * <code>RailsUIPlugin</code> preference store.
	 * 
	 * @param prefId
	 *            the identifier of the preference
	 * @return a <code>Color</code> object
	 */
	public Color getColorFromPreference(String prefId) {
		RGB rgb = PreferenceConverter.getColor(Activator.getDefault().getPreferenceStore(), prefId);
		return getColor(rgb);
	}
}
