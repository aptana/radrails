package com.aptana.rdt.internal.profiling;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StandardOutputParser extends AbstractProfileOutputParser {

	private ProfileThread thread;

	/* (non-Javadoc)
	 * @see com.aptana.rdt.internal.profiling.IStatisticsParser#parse(java.io.Reader)
	 */
	public List<ProfileThread> parse(Reader reader) {
		thread = null;
		List<String> lines = getLines(reader);
		lines.remove(0); // Thread ID
		lines.remove(0); // Total time
		lines.remove(0); // blank
		lines.remove(0); // column names
		List<ProfileThread> threads = new ArrayList<ProfileThread>();
		thread = new ProfileThread("fake_id", 0.0f);
		List<MethodCall> list = new ArrayList<MethodCall>();
		for (String line : lines) {
			if (line == null || line.trim().length() == 0) continue;
			list.add(getStatistic(line));
		}		
		
		thread.addMethodCalls(list);
		threads.add(thread);
		return threads;
	}

	private MethodCall getStatistic(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);
		float selfPercent = Float.parseFloat(stripLastChar(tokenizer.nextToken()));
		float total = Float.parseFloat(tokenizer.nextToken());
		float self = Float.parseFloat(tokenizer.nextToken());
		float wait = Float.parseFloat(tokenizer.nextToken());
		float child = Float.parseFloat(tokenizer.nextToken());
		int calls = Integer.parseInt(tokenizer.nextToken());
		String methodName = tokenizer.nextToken();
		return new MethodCall(thread, methodName, calls, selfPercent, self, wait, child);
	}
}
