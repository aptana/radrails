package com.aptana.rdt.internal.profiling;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CallGraphParser extends AbstractProfileOutputParser {

	private ProfileThread thread;
	public List<ProfileThread> parse(Reader reader) {
		thread = null;
		List<String> lines = getLines(reader);
		String id = getId(lines.remove(0));
		float totalTime = getTotalTime(lines.remove(0));
		thread = new ProfileThread(id, totalTime);
		lines.remove(0); // blank
		lines.remove(0); // column names
		List<MethodCall> calls = parseMethodCalls(lines);
		thread.addMethodCalls(calls);
		List<ProfileThread> threads = new ArrayList<ProfileThread>();
		threads.add(thread);
		return threads;
	}
	// FIXME Wow all of this code is very, very ugly
	
	private List<MethodCall> parseMethodCalls(List<String> lines) {
		List<List<String>> groups = createGroups(lines);	
		List<MethodCall> calls = new ArrayList<MethodCall>(groups.size());
		for (List<String> group : groups) {
			calls.add(createMethodCall(group));
		}
		return calls;
	}

	private MethodCall createMethodCall(List<String> group) {
		String caller = null;
		MethodCall call = null;
		for (String line : group) {
			if (line == null || line.trim().length() == 0) continue;
			String[] parts = split(line);
			if (!parts[0].endsWith("%")) {
				if (call == null) { // caller
					caller = parts[parts.length - 1];
				} else { // callee
					call.addCallee(parts[parts.length - 1]);					
				}
			} else {
				call = getMethodCall(line);
			}
		}
		if (caller != null) call.addCaller(caller);
		return call;
	}
	
	private String[] split(String line) {
		List<String> tokens = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(line);
		while(tokenizer.hasMoreTokens()) {
			tokens.add(tokenizer.nextToken());
		}
		return tokens.toArray(new String[tokens.size()]);
	}
	private MethodCall getMethodCall(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);
		// FIXME The method we're focusing on is the one that has values for percentage..
		// methods above it are the ones that call it
		// methods below are the methods it calls
		String rawTotalPercent = tokenizer.nextToken();
		if (!rawTotalPercent.endsWith("%")) {
			// FIXME Pull out the method name and add it as a caller or callee to the generated Statistic!
			return null;
		} else {
			rawTotalPercent = rawTotalPercent.substring(0, rawTotalPercent.length() - 1);
		}
		float totalPercent = Float.parseFloat(rawTotalPercent);
		float selfPercent = Float.parseFloat(stripLastChar(tokenizer.nextToken()));
		float total = Float.parseFloat(tokenizer.nextToken());
		float self = Float.parseFloat(tokenizer.nextToken());
		float wait = Float.parseFloat(tokenizer.nextToken());
		float child = Float.parseFloat(tokenizer.nextToken());
		String rawCalls = tokenizer.nextToken();
		
		if (rawCalls.indexOf('/') != -1) {
			rawCalls = rawCalls.substring(0, rawCalls.indexOf('/'));
		}
		int calls = Integer.parseInt(rawCalls); // FIXME not always just a number, but also XXXX/XXXX
		String methodName = tokenizer.nextToken();
		return new MethodCall(thread, methodName, calls, totalPercent, selfPercent, self, wait, child);
	}

	private List<List<String>> createGroups(List<String> lines) {
		List<List<String>> groups = new ArrayList<List<String>>();
		List<String> group = null;
		for (String line : lines) {
			if (line.startsWith("------")) {
				if (group != null) groups.add(group);
				// We have a new group
				group = new ArrayList<String>();
			} else {
				group.add(line);
			}
		}
		groups.add(group);
		return groups;
	}

	private float getTotalTime(String line) {
		String raw = line.substring(line.indexOf(": ") + 2);
		return Float.parseFloat(raw);
	}

	private String getId(String line) {
		return line.substring(line.indexOf(": ") + 2);
	}

}
