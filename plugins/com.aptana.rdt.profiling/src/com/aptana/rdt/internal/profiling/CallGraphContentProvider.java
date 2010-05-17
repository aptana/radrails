package com.aptana.rdt.internal.profiling;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class CallGraphContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ProfileThread) {
			ProfileThread thread= (ProfileThread) parentElement;
			return thread.getRoot().getChildren();
		}
		MethodCall call = (MethodCall) parentElement;
		return call.getChildren();
	}

	public Object getParent(Object element) {
		if (element instanceof ProfileThread) {
			return null;
		}
		MethodCall call = (MethodCall) element;
		if (call.isRoot()) return call.getThread();
		return call.getParentMethodCall();
	}

	public boolean hasChildren(Object element) {
		if (element == null) return false;
		if (element instanceof ProfileThread) {
			return true;
		}
		MethodCall call = (MethodCall) element;
		return call.hasChildren();
	}

	public Object[] getElements(Object inputElement) {
		List<ProfileThread> threads = (List<ProfileThread>)inputElement;
		return threads.toArray(new ProfileThread[threads.size()]);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
