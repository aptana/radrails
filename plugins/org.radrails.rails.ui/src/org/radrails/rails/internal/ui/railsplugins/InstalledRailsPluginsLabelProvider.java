package org.radrails.rails.internal.ui.railsplugins;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.radrails.rails.core.railsplugins.RailsPluginDescriptor;

public class InstalledRailsPluginsLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof RailsPluginDescriptor) {
			RailsPluginDescriptor desc = (RailsPluginDescriptor) element;
			return desc.getProperty(RailsPluginDescriptor.NAME);
		}
		return null;
	}

}
