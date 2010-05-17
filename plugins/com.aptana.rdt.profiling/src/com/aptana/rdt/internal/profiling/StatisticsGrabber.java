package com.aptana.rdt.internal.profiling;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import com.aptana.rdt.profiling.IProfileUIConstants;
import com.aptana.rdt.profiling.ProfilingPlugin;

public class StatisticsGrabber implements IDebugEventSetListener {

	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			if (events[i].getKind() == DebugEvent.TERMINATE) {
				Object source = events[i].getSource();
				if (source instanceof IProcess) {
					IProcess process = (IProcess) source;
					String profilePath = process.getLaunch().getAttribute(IProfileUIConstants.ATTR_PROFILE_OUTPUT);
					if (profilePath != null) {
						File file = new File(profilePath);
						IProfilerOutputParser parser = createStatsParser();
						try {
							Reader reader = new FileReader(file);
							final List<ProfileThread> input = parser.parse(reader);
							Display.getDefault().asyncExec(new Runnable() {							
								public void run() {		
									openProfilingPerspective();
									ProfilingPlugin.getDefault().profilingEnded(input);					
								}							
							});
							
						} catch (Exception e) {
							ProfilingPlugin.log(e);
						}
						
					}					
				}
			}
		}
		
	}

	private IProfilerOutputParser createStatsParser() {
		return new CallGraphParser();
//		return new StatisticsParser();
	}

	private void openProfilingPerspective() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {
			PlatformUI.getWorkbench().showPerspective(IProfileUIConstants.ID_PERSPECTIVE, window);
		} catch (WorkbenchException e) {
			// ignore
		}		
	}
}
