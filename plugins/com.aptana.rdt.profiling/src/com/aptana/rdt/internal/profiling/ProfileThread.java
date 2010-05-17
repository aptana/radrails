package com.aptana.rdt.internal.profiling;

import java.util.Collection;
import java.util.List;

public class ProfileThread {
	
	private String id;
	private float totalTime;
	
	private List<MethodCall> calls;
	
	public ProfileThread(String id, float totalTime) {
		this.id = id;
		this.totalTime = totalTime;
	}
		
	public MethodCall getRoot() {
		return findRoot(calls);
	}
	
	private MethodCall findRoot(List<MethodCall> calls) {
		for (MethodCall call : calls) {
			if (call.getParent() == null) return call;
		}
		return null;
	}

	public void addMethodCalls(List<MethodCall> calls) {
		this.calls = calls;		
	}
	
	@Override
	public String toString() {
		return "Thread ID: " + id + ", Total time: " + totalTime;
	}

	MethodCall findMethodCall(String methodName) {
		for (MethodCall call : calls) {
			if (call.getFullMethodName().equals(methodName)) return call;
		}
		return null;
	}

	public Collection<? extends MethodCall> getMethodCalls() {
		return calls;
	}

	public String getID() {
		return id;
	}

	public float getTotalTime() {
		return totalTime;
	}

}
