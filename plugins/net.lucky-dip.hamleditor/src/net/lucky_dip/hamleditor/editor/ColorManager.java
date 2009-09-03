package net.lucky_dip.hamleditor.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * @deprecated Combine with {@link HamlUIColorProvider}
 * @author cwilliams
 *
 */
public class ColorManager {

	protected Map fColorTable = new HashMap(10);

	private IPreferenceStore store;

	public ColorManager(IPreferenceStore store) {
		this.store = store;
	}

	public void dispose() {
		Iterator e = fColorTable.values().iterator();
		while (e.hasNext())
			((Color) e.next()).dispose();
	}

	public Color getColor(RGB rgb) {
		Color color = (Color) fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}

	public Color getColor(String key) {
		return getColor(StringConverter.asRGB(store.getString(key)));
	}
}
