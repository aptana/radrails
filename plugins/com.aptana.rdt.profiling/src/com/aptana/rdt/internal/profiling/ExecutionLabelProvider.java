package com.aptana.rdt.internal.profiling;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.rubypeople.rdt.ui.ISharedImages;
import org.rubypeople.rdt.ui.RubyUI;

public class ExecutionLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return RubyUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
		case 1:
			return RubyUI.getSharedImages().getImage(ISharedImages.IMG_MISC_PUBLIC_METHOD);
		default:
			return null;
		}
	}

	public String getColumnText(Object element, int columnIndex) {
		MethodCall stat = (MethodCall) element;
		switch (columnIndex) {
		case 0:
			return stat.getClassName();
		case 1:
			return stat.getMethodName();
		case 2:
			return Float.toString(stat.selfTimePercent()) + "%";
		case 3:
			return Integer.toString(stat.callCount());
		case 4:
			return Float.toString(stat.selfTime());
		case 5:
			return Float.toString(stat.waitTime());
		case 6:
			return Float.toString(stat.childTime());
		case 7:
			return Float.toString(stat.timePerInvocation());
		default:
			return null;
		}
	}

	public void addListener(ILabelProviderListener listener) {}

	public void dispose() {}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {}

}
