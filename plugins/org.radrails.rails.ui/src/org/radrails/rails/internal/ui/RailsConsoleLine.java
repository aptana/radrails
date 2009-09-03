package org.radrails.rails.internal.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.radrails.rails.internal.core.RailsPlugin;

public class RailsConsoleLine {
	
	protected String fFilename;
	protected int fLength;
	protected int fOffset;
	protected int fLineNumber;
	
	public String getFilename() {
		return fFilename;
	}

	public int getLength() {
		return fLength;
	}

	public int getLineNumber() {
		return fLineNumber;
	}

	public int getOffset() {
		return fOffset;
	}
	
	protected void makeRelativeToWorkspace(IProject launchedProject) {
		try {
			if (fFilename == null || fFilename.trim().length() == 0) return;
			String filename = fFilename;
			if (fFilename.startsWith("./")) {
				filename = fFilename.substring(1);
			} else {
				filename ='/' + fFilename;
			}
			IFile file = launchedProject.getFile(filename);
			if (!file.exists()) {
				IPath railsRoot = RailsPlugin.findRailsRoot(launchedProject);
				file = launchedProject.getFile(railsRoot.append(filename));
			}
			
			fFilename = file.getFullPath().toPortableString();
		} catch (RuntimeException e) {
			// ignore
		}
		
	}

	protected boolean isRelativePath() {
		if (fFilename.startsWith("./")) return true;
		int index = fFilename.indexOf('/');
		if (index != -1 && !fFilename.startsWith("/") && fFilename.charAt(index - 1) != ':' ) return true;
		return false;
	}
}
