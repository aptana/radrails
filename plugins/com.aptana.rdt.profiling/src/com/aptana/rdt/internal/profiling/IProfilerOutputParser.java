package com.aptana.rdt.internal.profiling;

import java.io.Reader;
import java.util.List;

public interface IProfilerOutputParser {

	public abstract List<ProfileThread> parse(Reader reader);

}