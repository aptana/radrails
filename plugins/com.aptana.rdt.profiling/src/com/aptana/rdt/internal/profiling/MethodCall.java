package com.aptana.rdt.internal.profiling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodCall {
	
	private String methodName;
	private int calls;
	private float selfPercent;
	private float self;
	private float wait;
	private float child;
	private float totalTimePercent = 0.0f;
	
	private String parent;
	private Set<String> callees;
	private ProfileThread thread;		

	public MethodCall(ProfileThread thread, String methodName, int calls, float selfPercent, float self, float wait, float child) {
		this.thread = thread;
		this.methodName = methodName;
		this.calls = calls;
		this.selfPercent = selfPercent;
		this.self = self;
		this.wait = wait;
		this.child = child;
		this.callees = new HashSet<String>();
	}

	public MethodCall(ProfileThread thread, String methodName, int calls, float totalPercent, float selfPercent, float self, float wait, float child) {
		this(thread, methodName, calls, selfPercent, self, wait, child);
		this.totalTimePercent = totalPercent;
	}

	public int callCount() {
		return calls;
	}

	public float childTime() {
		return child;
	}

	public String getMethodName() {
		if (getClassName().equals("Global")) return "";
		String[] parts = getFullMethodName().split("#");
		if (parts == null || parts.length == 0) return "";
		if (parts.length == 1) return parts[0];
		return parts[1];	
	}
	
	String getFullMethodName() {
		return methodName;
	}

	public String getClassName() {
		String[] parts = getFullMethodName().split("#");
		if (parts == null || parts.length == 0) return "";
		return parts[0];
	}

	public float selfTime() {
		return self;
	}
	
	public float selfTimePercent() {
		return selfPercent;
	}
	
	public float timePerInvocation() {
		return selfTime() / callCount();
	}

	public float waitTime() {
		return wait;
	}
	
	void addCaller(String parent) {
		this.parent = parent;
	}
	
	public String getParent() {
		return parent;
	}
	
	void addCallee(String call) {
		callees.add(call);
	}
	
	public Set<String> getCallees() {
		return callees;
	}

	public MethodCall[] getChildren() {
		List<MethodCall> calls = new ArrayList<MethodCall>();
		for (String methodName : callees) {
			calls.add(thread.findMethodCall(methodName));
		}
		return calls.toArray(new MethodCall[calls.size()]);
	}

	public MethodCall getParentMethodCall() {
		return thread.findMethodCall(getParent());
	}

	public boolean hasChildren() {
		return !callees.isEmpty();
	}
	
	public float totalTimePercent() {
		return totalTimePercent;
	}
	
	@Override
	public String toString() {
		return getFullMethodName() + ", self: " + selfTime() + ", wait: " + waitTime() + ", child: " + childTime();
	}

	boolean isRoot() {
		return getParent() == null;
	}

	public ProfileThread getThread() {
		return thread;
	}
}
