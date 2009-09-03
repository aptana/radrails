package com.aptana.radrails.sqlite3.win32;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.ui.IStartup;

public class InstallSQLite3Job implements IStartup {
	
	private void copyFile(String src, String dest) {
		File file = new File(dest);
		if (file.exists()) {
			log("File already exists: " + dest);
			return;
		}
		
		log("Trying to copy included file: " + src + " to: " + dest);
		InputStream stream = null;
		FileOutputStream output = null;
		try {
			IPath path = new Path(src);
			stream = FileLocator.openStream(Activator.getDefault().getBundle(), path, false);
			output = new FileOutputStream(file);
			byte[] b = new byte[1024];
			while (stream.read(b) != -1) {
				output.write(b);
			}
		} catch (IOException e) {
			log(e);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
				// ignore
			}
			try {
				if (output != null)
					output.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	private void installSqlite3() {
		String[] files = new String[] {"sqlite3.exe", "sqlite3.dll", "sqlite3.def"};

		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			String path = getWindowsPath();
			log("Got path to Windows dir: " + path);
			// TODO If files already exist somewhere on path, don't copy them
			for (int i = 0; i < files.length; i++) {
				String dest = path + File.separator + files[i];				
				copyFile("vendor/" + files[i], dest);
			}
		}
	}

	private String getWindowsPath() {
		String winPath = System.getenv("Path");  // iterate through system path and try to find ":\Windows"
		log("Got system PATH: " + winPath);
		String[] paths = winPath.split(File.pathSeparator);
		for (int i = 0; i < paths.length; i++) {
			String path = paths[i];
			log("Checking path: " + path);
			if (path.endsWith(":" + File.separator + "Windows")) {
				return path;
			}
		}
		return paths[0];
	}

	public void earlyStartup() {
		installSqlite3();		
	}
	
	private void log(String msg) {
//		Activator.getDefault().getLog().log(new Status(Status.INFO, Activator.PLUGIN_ID, -1, msg, null));
	}
	
	private void log(Exception e) {
		Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, -1, e.getMessage(), e));
	}

}
