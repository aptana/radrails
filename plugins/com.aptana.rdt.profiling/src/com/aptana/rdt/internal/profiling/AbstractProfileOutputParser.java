package com.aptana.rdt.internal.profiling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractProfileOutputParser implements IProfilerOutputParser {

	protected List<String> getLines(Reader reader) {		
		List<String> lines = new ArrayList<String>();
		try {
			BufferedReader buffered = new BufferedReader(reader);
			String line = null;
			while ((line = buffered.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lines;
	}
	
	protected String stripLastChar(String token) {
		return token.substring(0, token.length() - 1);
	}
}
