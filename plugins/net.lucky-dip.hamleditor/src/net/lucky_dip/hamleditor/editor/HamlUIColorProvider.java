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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Color provider for rails ui plugin.
 * 
 * @author mkent
 * 
 */
public class HamlUIColorProvider implements IColorManager {

	private Map<RGB, Color> colorMap;

	public HamlUIColorProvider() {
		colorMap = new HashMap<RGB, Color>();
	}

	/**
	 * Dispose the color resources held by the provider.
	 */
	public synchronized void dispose() {
		Iterator<Color> i = colorMap.values().iterator();
		while (i.hasNext())
			i.next().dispose();
		colorMap.clear();
	}

	/**
	 * Creates a <code>Color</code> from the given <code>RGB</code> value.
	 * 
	 * @param rgb
	 *            the <code>RGB</code> value of the color
	 * @return a <code>Color</code> object
	 */
	public Color getColor(RGB rgb) {
		Color color = colorMap.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			colorMap.put(rgb, color);
		}
		return color;
	}

	public Color getColor(String colorKey) {
		return getColorFromPreference(colorKey);
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
