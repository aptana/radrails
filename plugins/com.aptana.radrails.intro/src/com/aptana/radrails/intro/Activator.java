package com.aptana.radrails.intro;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin
{

    static {
        UIJob job = new UIJob("Hide My Aptana actionSet") { //$NON-NLS-1$

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                // remove actionSet from the existing windows
                IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
                        .getWorkbenchWindows();
                for (IWorkbenchWindow window : windows) {
                    processWindow(window);
                }

                // listens on future windows
                PlatformUI.getWorkbench().addWindowListener(
                        new IWindowListener() {

                            public void windowActivated(IWorkbenchWindow window) {
                            }

                            public void windowClosed(IWorkbenchWindow window) {
                            }

                            public void windowDeactivated(
                                    IWorkbenchWindow window) {
                            }

                            public void windowOpened(IWorkbenchWindow window) {
                                processWindow(window);
                            }
                        });
                return Status.OK_STATUS;
            }

        };
        job.setSystem(true);
        job.schedule();
    }

    private static void processWindow(IWorkbenchWindow window) {
        IWorkbenchPage[] pages = window.getPages();
        for (IWorkbenchPage page : pages) {
            processPage(page);
        }
        window.addPerspectiveListener(new IPerspectiveListener() {

			public void perspectiveActivated(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
			}

			public void perspectiveChanged(IWorkbenchPage page,
					IPerspectiveDescriptor perspective, String changeId) {
				if (changeId == IWorkbenchPage.CHANGE_RESET_COMPLETE) {
					processPage(page);
				}
			}

        });
    }

    private static void processPage(IWorkbenchPage page) {
    	page.hideActionSet("a.com.aptana.ide.core.ui.actionSet.myaptana"); //$NON-NLS-1$
    }

	private static Activator instance;

	public Activator()
	{
		instance = this;
	}

	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception
	{
	    instance = null;
		super.stop(context);
	}

	public static Activator getDefault()
	{
		return instance;
	}

    public static void log(int status, String message, Throwable exception)
    {
        instance.getLog().log(
                new Status(status, instance.getBundle().getSymbolicName(),
                        IStatus.OK, message, exception));
    }

}
