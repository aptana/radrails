package org.radrails.rails.internal.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.ui.RailsUIPlugin;

public class RailsLightweightDecorator extends LabelProvider implements ILightweightLabelDecorator
{

	public void decorate(Object element, IDecoration decoration)
	{
		if (element == null || decoration == null)
			return;
		if (!(element instanceof IProject))
			return;
		IProject p = (IProject) element;
		if (!RailsPlugin.hasRailsNature(p))
			return;
		decoration.addOverlay(RailsUIPlugin.getImageDescriptor("icons/rails_ovr.gif"));
	}

}
