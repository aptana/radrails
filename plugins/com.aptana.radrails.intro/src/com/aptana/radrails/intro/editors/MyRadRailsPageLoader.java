package com.aptana.radrails.intro.editors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.aptana.ide.core.FileUtils;
import com.aptana.ide.core.online.OnlineDetectionService;
import com.aptana.radrails.intro.Activator;

public class MyRadRailsPageLoader {

    private static final String REMOTE_FILE_URL = "http://content.aptana.com/radrails/my_radrails"; //$NON-NLS-1$
    private static final String LOCAL_URL = "/content/radrails_index.html"; //$NON-NLS-1$
    private static final String CACHED_FILENAME = "cached_my_radrails.html"; //$NON-NLS-1$

    public static String getURLLocation() {
        try {
            if (OnlineDetectionService.isAvailable(new URL(REMOTE_FILE_URL))) {
                Job job = new Job("Caching My RadRails Page") { //$NON-NLS-1$

                    protected IStatus run(IProgressMonitor monitor) {
                        try {
                            InputStream in = (InputStream) getRemoteFileURL()
                                    .getContent();
                            saveCache(in);
                        } catch (IOException e) {
                            error(e);
                        }
                        return Status.OK_STATUS;
                    }

                };
                job.setSystem(true);
                job.setPriority(Job.BUILD);
                job.schedule();

                return REMOTE_FILE_URL;
            }
        } catch (MalformedURLException e) {
        }

        // falls back to the local cache file
        File file = getLocalCacheFile();
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        // falls back to the local copy shipped in the plugin
        URL rootPath = Activator.getDefault().getBundle().getEntry(LOCAL_URL);
        try {
            rootPath = FileLocator.toFileURL(rootPath);
            return rootPath.getPath();
        } catch (IOException e) {
            error(e);
        }
        // should not get here
        return REMOTE_FILE_URL;
    }

    private static URL getRemoteFileURL() throws MalformedURLException {
        return new URL(REMOTE_FILE_URL);
    }

    /**
     * @return the local cache file
     */
    private static File getLocalCacheFile() {
        IPath statePath = Activator.getDefault().getStateLocation().append(
                CACHED_FILENAME);
        return statePath.toFile();
    }

    /**
     * Copy contents from an InputStream to the local cache file.
     * 
     * @param in
     *            the input stream
     */
    private static void saveCache(InputStream in) throws IOException {
        File file = getLocalCacheFile();
        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStream writer = null;
        try {
            writer = new FileOutputStream(file);
            FileUtils.pipe(in, new FileOutputStream(file), false, null, null);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // ignore
            }
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static void error(Exception e) {
        Activator.log(IStatus.ERROR, e.getMessage(), e);
    }

}
