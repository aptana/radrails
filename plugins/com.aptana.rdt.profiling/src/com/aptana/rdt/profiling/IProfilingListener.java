package com.aptana.rdt.profiling;

import java.util.List;

import com.aptana.rdt.internal.profiling.ProfileThread;

public interface IProfilingListener {
	
	public void profilingEnded(List<ProfileThread> results);

}
