package com.aptana.rdt.internal.profiling;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class CallGraphLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof MethodCall) {
			MethodCall call = (MethodCall) element;
			switch (columnIndex) {
			case 0:
				return call.getFullMethodName();
			case 1:
				return Float.toString(call.totalTimePercent()) + "%";
			case 2:
				return Float.toString(call.selfTime());
			case 3:
				return Integer.toString(call.callCount());
			default:
				break;
			}
		} else if (element instanceof ProfileThread) {
			ProfileThread thread = (ProfileThread) element;
			switch (columnIndex) {
			case 0:
				return thread.getID();
			case 1:
				return "100%";
			case 2:
				return Float.toString(thread.getTotalTime());
			case 3:
				return "1";
			default:
				break;
			}
		}
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}


}
