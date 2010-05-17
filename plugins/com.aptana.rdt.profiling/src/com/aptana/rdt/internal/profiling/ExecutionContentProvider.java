package com.aptana.rdt.internal.profiling;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ExecutionContentProvider implements IStructuredContentProvider {

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		viewer.refresh();		
	}

	public Object[] getElements(Object inputElement) {
		List<ProfileThread> list = (List<ProfileThread>) inputElement;
		List<MethodCall> calls = new ArrayList<MethodCall>();
		for (ProfileThread thread : list) {
			calls.addAll(thread.getMethodCalls());
			// TODO Combine counts/times etc for all method calls with same method name across threads?
		}
		return calls.toArray(new MethodCall[calls.size()]);
	}

}
