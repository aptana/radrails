package org.radrails.rails.internal.ui.railsplugins;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.radrails.rails.core.railsplugins.RailsPluginDescriptor;

public class RailsPluginsLabelProvider extends LabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		RailsPluginDescriptor plugin = (RailsPluginDescriptor) element;
		switch (columnIndex) {	
		case 0:
			return plugin.getRawName();
		case 1:
			return plugin.getRawRating();
		case 2:
			return plugin.getLicense();
		case 3:
			return plugin.getHome();
		}
		return null;
	}

}
